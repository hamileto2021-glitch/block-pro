const FILTERS = { TODAY: "Today", YESTERDAY: "Yesterday", THIS_WEEK: "7 Days", THIS_MONTH: "This Month", ALL_TIME: "All Time" };
const etb = (n) => "ETB " + Math.round(n).toLocaleString("en-US");
const esc = (s) => String(s).replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" })[c]);
let filter = "TODAY";

function renderFilters() {
  document.getElementById("filters").innerHTML = Object.entries(FILTERS)
    .map(([k, v]) => `<button data-f="${k}" class="${k === filter ? "on" : ""}">${v}</button>`).join("");
}
document.getElementById("filters").addEventListener("click", (e) => {
  const f = e.target.dataset?.f;
  if (f) { filter = f; load(); }
});

async function load() {
  renderFilters();
  const res = await fetch(`/api/dashboard?filter=${filter}&tz=${-new Date().getTimezoneOffset()}`);
  if (!res.ok) { document.getElementById("kpis").textContent = "Could not load dashboard data."; return; }
  const d = await res.json(); const m = d.metrics;
  const kpi = (t, v, s) => `<div class="card"><h3>${t}</h3><div class="v">${v}</div><small>${s}</small></div>`;
  document.getElementById("kpis").innerHTML = [
    kpi("Collected revenue", etb(m.totalRevenue), `Invoiced: ${etb(m.invoicedAmount)}`),
    kpi("Production costs", etb(m.totalExpenses), `Materials ${etb(m.rawMaterialCostTotal)} · Labor ${etb(m.directLaborCostTotal)}`),
    kpi("Net profit", etb(m.netProfit), `Margin ${m.profitMarginPct.toFixed(1)}%`),
    kpi("Blocks molded", `${m.totalBlocksMolded.toLocaleString()} pcs`, `Defects ${m.totalDefectBlocks} (${m.defectRatePct.toFixed(1)}%)`),
    kpi("Blocks sold", `${m.totalBlocksSold.toLocaleString()} pcs`, `Receivables ${etb(m.totalReceivables)}`),
    kpi("Stock health", m.lowStockAlertsCount ? `${m.lowStockAlertsCount} Low` : "Optimal", `Inventory value ${etb(m.totalInventoryValue)}`),
  ].join("");
  const max = Math.max(1, ...d.dailyTrends.map((t) => t.revenue));
  document.getElementById("trend").innerHTML = d.dailyTrends
    .map((t) => `<div title="${etb(t.revenue)}"><span style="height:${(t.revenue / max) * 130}px"></span>${t.dayLabel}</div>`).join("");
  document.getElementById("stock").innerHTML = "<tr><th>Product</th><th>Stock</th><th>Price</th></tr>" + d.products
    .map((p) => `<tr><td>${esc(p.name)}</td><td>${p.currentStock.toLocaleString()}${p.currentStock <= p.minStockThreshold ? " ⚠" : ""}</td><td>${etb(p.unitPrice)}</td></tr>`).join("");
  document.getElementById("orders").innerHTML = "<tr><th>Order</th><th>Customer</th><th>Balance</th></tr>" + (d.openOrders
    .map((o) => `<tr><td>${esc(o.orderNumber)}</td><td>${esc(o.customerName)}</td><td>${etb(o.balanceAmount)}</td></tr>`).join("") || "<tr><td colspan=3>No open balances</td></tr>");
}
load();
