import type { Config } from "@netlify/functions";
import { db } from "../../db/index.js";
import {
  paymentTransactions,
  productionBatches,
  products,
  rawMaterials,
  salesOrders,
  wagePayments,
} from "../../db/schema.js";

const DAY = 24 * 60 * 60 * 1000;
const FILTERS = ["TODAY", "YESTERDAY", "THIS_WEEK", "THIS_MONTH", "ALL_TIME"] as const;
type Filter = (typeof FILTERS)[number];

// Mirrors calculateDashboardMetrics in the Android BlocketViewModel
export default async (req: Request) => {
  const url = new URL(req.url);
  const filterParam = url.searchParams.get("filter") ?? "TODAY";
  const filter: Filter = (FILTERS as readonly string[]).includes(filterParam) ? (filterParam as Filter) : "TODAY";
  // Client's UTC offset in minutes, so "today" matches the viewer's local day
  const tzOffset = Math.max(-840, Math.min(840, Number(url.searchParams.get("tz")) || 0)) * 60 * 1000;

  const [materials, prods, batches, orders, txns, wages] = await Promise.all([
    db.select().from(rawMaterials).orderBy(rawMaterials.id),
    db.select().from(products).orderBy(products.id),
    db.select().from(productionBatches),
    db.select().from(salesOrders),
    db.select().from(paymentTransactions),
    db.select().from(wagePayments),
  ]);

  const now = Date.now();
  const localNow = new Date(now + tzOffset);
  const todayStart = Date.UTC(localNow.getUTCFullYear(), localNow.getUTCMonth(), localNow.getUTCDate()) - tzOffset;
  const monthStart = Date.UTC(localNow.getUTCFullYear(), localNow.getUTCMonth(), 1) - tzOffset;

  const [start, end] = {
    TODAY: [todayStart, Infinity],
    YESTERDAY: [todayStart - DAY, todayStart - 1],
    THIS_WEEK: [todayStart - 6 * DAY, Infinity],
    THIS_MONTH: [monthStart, Infinity],
    ALL_TIME: [-Infinity, Infinity],
  }[filter];
  const inRange = (d: Date, s = start, e = end) => d.getTime() >= s && d.getTime() <= e;

  const costOf = (match: RegExp, fallback: number) =>
    materials.find((m) => match.test(m.name))?.costPerUnit ?? fallback;
  const cementCost = costOf(/cement/i, 1650);
  const sandCost = costOf(/sand/i, 1100);
  const gravelCost = costOf(/gravel|aggregate/i, 1250);
  const materialCost = (b: typeof batches[number]) =>
    b.cementBagsUsed * cementCost + b.sandUsedM3 * sandCost + b.aggregateUsedM3 * gravelCost;
  const sum = <T,>(list: T[], fn: (x: T) => number) => list.reduce((acc, x) => acc + fn(x), 0);

  const fBatches = batches.filter((b) => inRange(b.producedAt));
  const fOrders = orders.filter((o) => inRange(o.orderedAt));
  const fTxns = txns.filter((t) => inRange(t.paidAt));
  const fWages = wages.filter((w) => inRange(w.paidAt));

  const totalRevenue = sum(fTxns, (t) => t.amount);
  const invoicedAmount = sum(fOrders, (o) => o.totalAmount);
  const rawMaterialCostTotal = sum(fBatches, materialCost);
  const directLaborCostTotal = sum(fBatches, (b) => b.laborCost) + sum(fWages, (w) => w.amount);
  const totalExpenses = rawMaterialCostTotal + directLaborCostTotal;
  const netProfit = totalRevenue - totalExpenses;
  const totalBlocksMolded = sum(fBatches, (b) => b.quantityMolded);
  const totalDefectBlocks = sum(fBatches, (b) => b.defectCount);

  const lowMaterials = materials.filter((m) => m.currentStock <= m.minAlertThreshold);
  const lowProducts = prods.filter((p) => p.currentStock <= p.minStockThreshold);

  const dayLabel = new Intl.DateTimeFormat("en-US", { weekday: "short", timeZone: "UTC" });
  const dailyTrends = [6, 5, 4, 3, 2, 1, 0].map((offset) => {
    const s = todayStart - offset * DAY;
    const e = s + DAY - 1;
    const dayBatches = batches.filter((b) => inRange(b.producedAt, s, e));
    const revenue = sum(txns.filter((t) => inRange(t.paidAt, s, e)), (t) => t.amount);
    const material = sum(dayBatches, materialCost);
    const labor = sum(dayBatches, (b) => b.laborCost) + sum(wages.filter((w) => inRange(w.paidAt, s, e)), (w) => w.amount);
    return {
      dayLabel: dayLabel.format(new Date(s + tzOffset)),
      revenue,
      expense: material + labor,
      outputBlocks: sum(dayBatches, (b) => b.quantityMolded),
      netProfit: revenue - material - labor,
    };
  });

  const shareMap = new Map<string, number>();
  for (const b of fBatches) shareMap.set(b.productName, (shareMap.get(b.productName) ?? 0) + b.quantityMolded);
  const productShares = [...shareMap.entries()]
    .map(([productName, quantity]) => ({
      productName,
      quantity,
      percentage: totalBlocksMolded > 0 ? (quantity / totalBlocksMolded) * 100 : 0,
    }))
    .sort((a, b) => b.quantity - a.quantity);

  const byNewest = <T,>(list: T[], key: (x: T) => Date) => [...list].sort((a, b) => key(b).getTime() - key(a).getTime());

  return Response.json({
    filter,
    metrics: {
      totalRevenue,
      invoicedAmount,
      totalExpenses,
      rawMaterialCostTotal,
      directLaborCostTotal,
      netProfit,
      profitMarginPct: totalRevenue > 0 ? (netProfit / totalRevenue) * 100 : 0,
      totalBlocksMolded,
      totalBlocksSold: sum(fOrders, (o) => o.quantity),
      totalDefectBlocks,
      defectRatePct: totalBlocksMolded > 0 ? (totalDefectBlocks / totalBlocksMolded) * 100 : 0,
      totalReceivables: sum(orders, (o) => o.balanceAmount),
      totalInventoryValue:
        sum(materials, (m) => m.currentStock * m.costPerUnit) + sum(prods, (p) => p.currentStock * p.unitCost),
      lowStockAlertsCount: lowMaterials.length + lowProducts.length,
    },
    dailyTrends,
    productShares,
    products: prods,
    materials,
    lowStock: [
      ...lowProducts.map((p) => ({ name: p.name, stock: p.currentStock, min: p.minStockThreshold, unit: "pcs" })),
      ...lowMaterials.map((m) => ({ name: m.name, stock: m.currentStock, min: m.minAlertThreshold, unit: m.unit })),
    ],
    openOrders: byNewest(orders.filter((o) => o.balanceAmount > 0), (o) => o.orderedAt),
    recentBatches: byNewest(batches, (b) => b.producedAt).slice(0, 5),
    recentPayments: byNewest(txns, (t) => t.paidAt).slice(0, 5),
  });
};

export const config: Config = {
  path: "/api/dashboard",
  method: "GET",
};
