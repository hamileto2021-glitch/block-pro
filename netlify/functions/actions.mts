import type { Config } from "@netlify/functions";
import { count, eq, sql } from "drizzle-orm";
import { db } from "../../db/index.js";
import { paymentTransactions, productionBatches, products, salesOrders } from "../../db/schema.js";

const PAYMENT_METHODS = ["CASH", "BANK_TRANSFER", "TELEBIRR", "CBE_BIRR"];

class BadRequest extends Error {}

const num = (value: unknown, field: string, { min = 0, integer = false } = {}) => {
  const n = Number(value);
  if (!Number.isFinite(n) || n < min || (integer && !Number.isInteger(n))) {
    throw new BadRequest(`${field} must be ${integer ? "a whole number" : "a number"} of at least ${min}`);
  }
  return n;
};

const str = (value: unknown, field: string) => {
  const s = typeof value === "string" ? value.trim() : "";
  if (!s) throw new BadRequest(`${field} is required`);
  return s.slice(0, 200);
};

async function findProduct(id: unknown) {
  const [product] = await db.select().from(products).where(eq(products.id, num(id, "Product", { min: 1, integer: true })));
  if (!product) throw new BadRequest("Unknown product");
  return product;
}

// Log a production batch and add accepted blocks to stock
async function logBatch(body: Record<string, unknown>) {
  const product = await findProduct(body.productId);
  const quantityMolded = num(body.quantityMolded, "Quantity molded", { min: 1, integer: true });
  const defectCount = num(body.defectCount ?? 0, "Defects", { integer: true });
  if (defectCount > quantityMolded) throw new BadRequest("Defects cannot exceed quantity molded");

  return db.transaction(async (tx) => {
    const [{ total }] = await tx.select({ total: count() }).from(productionBatches);
    const [batch] = await tx
      .insert(productionBatches)
      .values({
        batchCode: `PRD-ETH-${String(98 + total).padStart(3, "0")}`,
        productId: product.id,
        productName: product.name,
        quantityMolded,
        defectCount,
        cementBagsUsed: num(body.cementBagsUsed ?? 0, "Cement bags"),
        sandUsedM3: num(body.sandUsedM3 ?? 0, "Sand"),
        aggregateUsedM3: num(body.aggregateUsedM3 ?? 0, "Aggregate"),
        laborCost: num(body.laborCost ?? 0, "Labor cost"),
        supervisorName: typeof body.supervisorName === "string" ? body.supervisorName.trim().slice(0, 100) : "",
      })
      .returning();
    await tx
      .update(products)
      .set({
        currentStock: sql`${products.currentStock} + ${quantityMolded - defectCount}`,
        brokenCount: sql`${products.brokenCount} + ${defectCount}`,
      })
      .where(eq(products.id, product.id));
    return batch;
  });
}

// Create a sales order, deduct stock, and record any upfront payment
async function createSale(body: Record<string, unknown>) {
  const product = await findProduct(body.productId);
  const customerName = str(body.customerName, "Customer name");
  const quantity = num(body.quantity, "Quantity", { min: 1, integer: true });
  const unitPrice = num(body.unitPrice ?? product.unitPrice, "Unit price");
  const deliveryFee = num(body.deliveryFee ?? 0, "Delivery fee");
  const totalAmount = quantity * unitPrice + deliveryFee;
  const paidAmount = num(body.paidAmount ?? 0, "Amount paid");
  if (paidAmount > totalAmount) throw new BadRequest("Amount paid cannot exceed the order total");
  if (quantity > product.currentStock) throw new BadRequest(`Only ${product.currentStock} ${product.name} in stock`);
  const paymentMethod = PAYMENT_METHODS.includes(String(body.paymentMethod)) ? String(body.paymentMethod) : "CASH";

  return db.transaction(async (tx) => {
    const [{ total }] = await tx.select({ total: count() }).from(salesOrders);
    const [order] = await tx
      .insert(salesOrders)
      .values({
        orderNumber: `ORD-AA-${398 + total}`,
        customerName,
        productId: product.id,
        productName: product.name,
        quantity,
        unitPrice,
        deliveryFee,
        totalAmount,
        paidAmount,
        balanceAmount: totalAmount - paidAmount,
        paymentStatus: paidAmount >= totalAmount ? "PAID" : paidAmount > 0 ? "PARTIAL" : "UNPAID",
      })
      .returning();
    await tx
      .update(products)
      .set({ currentStock: sql`${products.currentStock} - ${quantity}` })
      .where(eq(products.id, product.id));
    if (paidAmount > 0) {
      await tx.insert(paymentTransactions).values({
        orderId: order.id,
        orderNumber: order.orderNumber,
        customerName,
        amount: paidAmount,
        paymentMethod,
      });
    }
    return order;
  });
}

// Record a customer payment against an open order
async function recordPayment(body: Record<string, unknown>) {
  const orderId = num(body.orderId, "Order", { min: 1, integer: true });
  const amount = num(body.amount, "Amount", { min: 0.01 });
  const paymentMethod = PAYMENT_METHODS.includes(String(body.paymentMethod)) ? String(body.paymentMethod) : "CASH";

  return db.transaction(async (tx) => {
    const [order] = await tx.select().from(salesOrders).where(eq(salesOrders.id, orderId)).for("update");
    if (!order) throw new BadRequest("Unknown order");
    if (amount > order.balanceAmount + 0.001) throw new BadRequest(`Balance due is only ETB ${order.balanceAmount}`);
    const paidAmount = order.paidAmount + amount;
    const balanceAmount = Math.max(0, order.totalAmount - paidAmount);
    await tx
      .update(salesOrders)
      .set({ paidAmount, balanceAmount, paymentStatus: balanceAmount === 0 ? "PAID" : "PARTIAL" })
      .where(eq(salesOrders.id, order.id));
    const [payment] = await tx
      .insert(paymentTransactions)
      .values({
        orderId: order.id,
        orderNumber: order.orderNumber,
        customerName: order.customerName,
        amount,
        paymentMethod,
        referenceNo: typeof body.referenceNo === "string" ? body.referenceNo.trim().slice(0, 100) : "",
      })
      .returning();
    return payment;
  });
}

const handlers: Record<string, (body: Record<string, unknown>) => Promise<unknown>> = {
  "/api/batches": logBatch,
  "/api/sales": createSale,
  "/api/payments": recordPayment,
};

export default async (req: Request) => {
  const handler = handlers[new URL(req.url).pathname];
  if (!handler) return new Response("Not found", { status: 404 });
  try {
    const body = await req.json().catch(() => {
      throw new BadRequest("Invalid JSON body");
    });
    return Response.json(await handler(body ?? {}), { status: 201 });
  } catch (err) {
    if (err instanceof BadRequest) return Response.json({ error: err.message }, { status: 400 });
    console.error(err);
    return Response.json({ error: "Something went wrong" }, { status: 500 });
  }
};

export const config: Config = {
  path: ["/api/batches", "/api/sales", "/api/payments"],
  method: "POST",
};
