import { pgTable, serial, text, integer, doublePrecision, timestamp } from "drizzle-orm/pg-core";

export const rawMaterials = pgTable("raw_materials", {
  id: serial().primaryKey(),
  name: text().notNull(),
  unit: text().notNull(),
  currentStock: doublePrecision("current_stock").notNull(),
  minAlertThreshold: doublePrecision("min_alert_threshold").notNull(),
  costPerUnit: doublePrecision("cost_per_unit").notNull(),
});

export const products = pgTable("products", {
  id: serial().primaryKey(),
  name: text().notNull(),
  sizeSpec: text("size_spec").notNull(),
  currentStock: integer("current_stock").notNull(),
  unitCost: doublePrecision("unit_cost").notNull(),
  unitPrice: doublePrecision("unit_price").notNull(),
  brokenCount: integer("broken_count").notNull().default(0),
  minStockThreshold: integer("min_stock_threshold").notNull().default(500),
});

export const productionBatches = pgTable("production_batches", {
  id: serial().primaryKey(),
  batchCode: text("batch_code").notNull(),
  producedAt: timestamp("produced_at", { withTimezone: true }).notNull().defaultNow(),
  productId: integer("product_id").notNull().references(() => products.id),
  productName: text("product_name").notNull(),
  quantityMolded: integer("quantity_molded").notNull(),
  defectCount: integer("defect_count").notNull().default(0),
  cementBagsUsed: doublePrecision("cement_bags_used").notNull().default(0),
  sandUsedM3: doublePrecision("sand_used_m3").notNull().default(0),
  aggregateUsedM3: doublePrecision("aggregate_used_m3").notNull().default(0),
  laborCost: doublePrecision("labor_cost").notNull().default(0),
  supervisorName: text("supervisor_name").notNull().default(""),
});

export const salesOrders = pgTable("sales_orders", {
  id: serial().primaryKey(),
  orderNumber: text("order_number").notNull(),
  customerName: text("customer_name").notNull(),
  orderedAt: timestamp("ordered_at", { withTimezone: true }).notNull().defaultNow(),
  productId: integer("product_id").notNull().references(() => products.id),
  productName: text("product_name").notNull(),
  quantity: integer().notNull(),
  unitPrice: doublePrecision("unit_price").notNull(),
  deliveryFee: doublePrecision("delivery_fee").notNull().default(0),
  totalAmount: doublePrecision("total_amount").notNull(),
  paidAmount: doublePrecision("paid_amount").notNull().default(0),
  balanceAmount: doublePrecision("balance_amount").notNull(),
  paymentStatus: text("payment_status").notNull(),
  deliveryStatus: text("delivery_status").notNull().default("PENDING"),
});

export const paymentTransactions = pgTable("payment_transactions", {
  id: serial().primaryKey(),
  orderId: integer("order_id").notNull().references(() => salesOrders.id),
  orderNumber: text("order_number").notNull(),
  customerName: text("customer_name").notNull(),
  paidAt: timestamp("paid_at", { withTimezone: true }).notNull().defaultNow(),
  amount: doublePrecision().notNull(),
  paymentMethod: text("payment_method").notNull(),
  referenceNo: text("reference_no").notNull().default(""),
});

export const wagePayments = pgTable("wage_payments", {
  id: serial().primaryKey(),
  employeeName: text("employee_name").notNull(),
  paidAt: timestamp("paid_at", { withTimezone: true }).notNull().defaultNow(),
  amount: doublePrecision().notNull(),
  paymentType: text("payment_type").notNull(),
  paymentMethod: text("payment_method").notNull(),
});
