package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DailyFinancialCanvasChart
import com.example.ui.components.DailySalesReportingDashboard
import com.example.ui.components.FinancialOverviewHeroCard
import com.example.ui.components.MetricStatCard
import com.example.ui.components.ProductShareBreakdownCard
import com.example.ui.components.ProductionCostBreakdownCard
import com.example.ui.components.ProductionOutputDailyChart
import com.example.ui.components.QuickGlanceReceivablesAndValuation
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.BlocketViewModel
import com.example.ui.viewmodel.TimeFilter
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: BlocketViewModel,
    onNavigateToStocks: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToEmployees: () -> Unit,
    onOpenLogBatch: () -> Unit,
    onOpenNewSale: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyReport by viewModel.dailyReport.collectAsStateWithLifecycle()
    val metrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedTimeFilter.collectAsStateWithLifecycle()
    val products by viewModel.blockProducts.collectAsStateWithLifecycle()
    val batches by viewModel.productionBatches.collectAsStateWithLifecycle()

    var selectedDashboardTab by remember { mutableIntStateOf(0) }

    val totalFinishedBlocks = products.sumOf { it.currentStock }
    val activeBatchesCount = batches.count { 
        it.notes.contains("curing", ignoreCase = true) || 
        (System.currentTimeMillis() - it.dateMillis < 7L * 24 * 60 * 60 * 1000) 
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Factory Hero Banner with Real Yard Art & Instant Action Shortcuts
        item {
            com.example.ui.components.FactoryHeroBanner(
                totalInventoryCount = totalFinishedBlocks,
                inventoryValuationEtb = metrics.totalInventoryValue,
                activeBatchesCount = activeBatchesCount,
                onLogBatchClick = onOpenLogBatch,
                onNewSaleClick = onOpenNewSale
            )
        }

        // 2. High-Level Reporting Tab: Daily Sales & Output vs Trends & Analytics
        item {
            DashboardTabRow(
                selectedTab = selectedDashboardTab,
                onTabSelected = { selectedDashboardTab = it }
            )
        }

        if (selectedDashboardTab == 0) {
            // === TAB 0: TODAY'S DAILY SALES & PRODUCTION REPORTING DASHBOARD ===
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    DailySalesReportingDashboard(
                        dailyReport = dailyReport,
                        onLogBatchClick = onOpenLogBatch,
                        onNewSaleClick = onOpenNewSale
                    )
                }
            }
        } else {
            // === TAB 1: MULTI-DAY PERIOD TRENDS & FINANCIAL ANALYTICS ===
            // Time Filter Selector Bar
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(TimeFilter.values()) { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) AmberPrimary else Slate200.copy(alpha = 0.8f)
                                )
                                .clickable { viewModel.setTimeFilter(filter) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                                .testTag("filter_${filter.name}")
                        ) {
                            Text(
                                text = filter.label,
                                color = if (isSelected) Color.White else Slate700,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Financial Overview Hero Card
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    FinancialOverviewHeroCard(
                        revenue = metrics.totalRevenue,
                        expenses = metrics.totalExpenses,
                        netProfit = metrics.netProfit,
                        marginPct = metrics.profitMarginPct
                    )
                }
            }

            // Quick Glance Receivables & Valuation
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    QuickGlanceReceivablesAndValuation(
                        receivables = metrics.totalReceivables,
                        inventoryValue = metrics.totalInventoryValue
                    )
                }
            }

            // Quick Action Shortcuts
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Quick Factory Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            title = "Log Batch",
                            icon = Icons.Default.Build,
                            bgColor = Color(0xFFFFEDD5),
                            tint = AmberPrimary,
                            onClick = onOpenLogBatch,
                            modifier = Modifier.weight(1f).testTag("quick_action_log_batch")
                        )
                        QuickActionButton(
                            title = "New Sale",
                            icon = Icons.Default.PointOfSale,
                            bgColor = Color(0xFFDCFCE7),
                            tint = ProfitGreen,
                            onClick = onOpenNewSale,
                            modifier = Modifier.weight(1f).testTag("quick_action_new_sale")
                        )
                        QuickActionButton(
                            title = "Payments",
                            icon = Icons.Default.AttachMoney,
                            bgColor = Color(0xFFE0F2FE),
                            tint = InfoBlue,
                            onClick = onNavigateToPayments,
                            modifier = Modifier.weight(1f).testTag("quick_action_payments")
                        )
                    }
                }
            }

            // Operations KPI Grid (2x2)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricStatCard(
                            title = "Blocks Molded",
                            value = "${metrics.totalBlocksMolded} pcs",
                            subValue = "Defects: ${metrics.totalDefectBlocks} (${String.format(Locale.US, "%.1f", metrics.defectRatePct)}%)",
                            icon = Icons.Default.Category,
                            iconTint = AmberPrimary,
                            iconBgColor = Color(0xFFFFEDD5),
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Blocks Sold",
                            value = "${metrics.totalBlocksSold} pcs",
                            subValue = "Invoiced: " + String.format(Locale.US, "ETB %,.0f", metrics.invoicedAmount),
                            icon = Icons.Default.ShoppingCart,
                            iconTint = ProfitGreen,
                            iconBgColor = Color(0xFFDCFCE7),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricStatCard(
                            title = "Invoiced Sales",
                            value = String.format(Locale.US, "ETB %,.0f", metrics.invoicedAmount),
                            subValue = "Collected: " + String.format(Locale.US, "ETB %,.0f", metrics.totalRevenue),
                            icon = Icons.Default.Assignment,
                            iconTint = InfoBlue,
                            iconBgColor = Color(0xFFE0F2FE),
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Stock Health",
                            value = if (metrics.lowStockAlertsCount == 0) "Optimal" else "${metrics.lowStockAlertsCount} Low",
                            subValue = if (metrics.lowStockAlertsCount == 0) "All materials above min" else "Requires restocking",
                            icon = if (metrics.lowStockAlertsCount == 0) Icons.Default.CheckCircle else Icons.Default.ReportProblem,
                            iconTint = if (metrics.lowStockAlertsCount == 0) ProfitGreen else WarningAmber,
                            iconBgColor = if (metrics.lowStockAlertsCount == 0) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Interactive Dual-Bar / Area Canvas Chart
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    DailyFinancialCanvasChart(trends = metrics.dailyTrends)
                }
            }

            // Production Cost Breakdown (Materials, Labor, Maintenance)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    ProductionCostBreakdownCard(
                        totalCost = metrics.totalExpenses,
                        rawMaterialCost = metrics.rawMaterialCostTotal,
                        directLaborCost = metrics.directLaborCostTotal,
                        otherCosts = metrics.otherExpensesTotal
                    )
                }
            }

            // Daily Block Output Bar Chart
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    ProductionOutputDailyChart(trends = metrics.dailyTrends)
                }
            }

            // Production Mix by Block Type
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    ProductShareBreakdownCard(shares = metrics.productShares)
                }
            }
        }
    }
}

@Composable
private fun DashboardTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf("Daily Sales & Output", "Trends & Analytics")
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = Slate200.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 11.dp)
                        .testTag(if (index == 0) "tab_daily_report" else "tab_trends_analytics"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                        color = if (isSelected) AmberPrimary else Slate900
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    icon: ImageVector,
    bgColor: Color,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        }
    }
}
