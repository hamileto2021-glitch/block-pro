package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductionBatchEntity
import com.example.data.model.SalesOrderEntity
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.DailyReportState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailySalesReportingDashboard(
    dailyReport: DailyReportState,
    onLogBatchClick: () -> Unit,
    onNewSaleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var batchesExpanded by remember { mutableStateOf(true) }
    var salesExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_sales_reporting_dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Date & Live Operational Status Banner
        DailyReportDateHeader(
            formattedDate = dailyReport.formattedDate,
            ethiopianDate = dailyReport.ethiopianDate
        )

        // 2. Today's Total Sales Revenue Hero Card
        DailyRevenueHeroCard(
            todayRevenue = dailyReport.todayRevenue,
            todayInvoiced = dailyReport.todayInvoicedSales,
            todayPendingCredit = dailyReport.todayPendingCredit,
            todaySalesCount = dailyReport.todaySalesCount,
            todayBlocksSold = dailyReport.todayBlocksSold
        )

        // 3. Today's Total Production Output Hero Card
        DailyProductionHeroCard(
            totalMolded = dailyReport.todayProductionOutput,
            usableBlocks = dailyReport.todayNetUsableBlocks,
            defects = dailyReport.todayDefectCount,
            defectRatePct = dailyReport.todayDefectRatePct,
            batchesCount = dailyReport.todayBatchesCount,
            outputByType = dailyReport.outputByBlockType
        )

        // 4. Quick Action Shortcuts for Today
        DailyActionShortcutsRow(
            onLogBatchClick = onLogBatchClick,
            onNewSaleClick = onNewSaleClick
        )

        // 5. Daily Operations & Cost Efficiency Ribbon
        DailyEfficiencyRibbon(
            topSellingBlock = dailyReport.topSellingBlockName,
            todayLaborCost = dailyReport.todayLaborCost,
            totalBlocksMolded = dailyReport.todayProductionOutput
        )

        // 6. Payment Channels Breakdown (Ethiopian Context)
        if (dailyReport.paymentMethodsBreakdown.isNotEmpty()) {
            DailyPaymentBreakdownCard(breakdown = dailyReport.paymentMethodsBreakdown)
        }

        // 7. Today's Sales Orders List
        TodaySalesOrdersCard(
            orders = dailyReport.todaySalesOrders,
            isExpanded = salesExpanded,
            onToggleExpand = { salesExpanded = !salesExpanded },
            onNewSaleClick = onNewSaleClick
        )

        // 8. Today's Production Batches List
        TodayProductionBatchesCard(
            batches = dailyReport.todayProductionBatches,
            isExpanded = batchesExpanded,
            onToggleExpand = { batchesExpanded = !batchesExpanded },
            onLogBatchClick = onLogBatchClick
        )
    }
}

@Composable
fun DailyReportDateHeader(
    formattedDate: String,
    ethiopianDate: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_report_date_header"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(Slate900, Slate800)
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AmberPrimary.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = Color(0xFFFDBA74),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Daily Operations Dashboard",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Live status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ProfitGreen.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4ADE80))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TODAY'S LOG",
                            color = Color(0xFF4ADE80),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Gregorian Date
            Text(
                text = formattedDate.ifEmpty { "Today" },
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )

            // Ethiopian Calendar Date
            if (ethiopianDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ethiopianDate,
                    color = Color(0xFFFDE68A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DailyRevenueHeroCard(
    todayRevenue: Double,
    todayInvoiced: Double,
    todayPendingCredit: Double,
    todaySalesCount: Int,
    todayBlocksSold: Int,
    modifier: Modifier = Modifier
) {
    val collectionRate = if (todayInvoiced > 0) ((todayRevenue / todayInvoiced) * 100).coerceIn(0.0, 100.0) else 100.0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_sales_revenue_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF064E3B), Slate900)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Revenue",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TODAY'S TOTAL REVENUE",
                            color = Color(0xFFA7F3D0),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Collected Cash & Digital Payments",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "$todaySalesCount Orders",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Big Total Revenue Value
            Text(
                text = String.format(Locale.US, "ETB %,.2f", todayRevenue),
                color = Color(0xFF4ADE80),
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.testTag("today_metric_revenue")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Invoiced vs Collected Progress Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Collections Rate",
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%% of Invoiced", collectionRate),
                        color = Color(0xFF4ADE80),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (collectionRate / 100f).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFF4ADE80),
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub Breakdown Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DailySubMetric(
                    label = "Invoiced Total",
                    value = String.format(Locale.US, "ETB %,.0f", todayInvoiced),
                    icon = Icons.Default.ReceiptLong,
                    modifier = Modifier.weight(1f)
                )
                DailySubMetric(
                    label = "Credit Balance",
                    value = String.format(Locale.US, "ETB %,.0f", todayPendingCredit),
                    icon = Icons.Default.Warning,
                    tint = if (todayPendingCredit > 0) Color(0xFFFBBF24) else Color(0xFF4ADE80),
                    modifier = Modifier.weight(1f)
                )
                DailySubMetric(
                    label = "Blocks Sold",
                    value = "$todayBlocksSold pcs",
                    icon = Icons.Default.ShoppingCart,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DailyProductionHeroCard(
    totalMolded: Int,
    usableBlocks: Int,
    defects: Int,
    defectRatePct: Double,
    batchesCount: Int,
    outputByType: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_production_output_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF78350F), Slate900)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AmberPrimary.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PrecisionManufacturing,
                            contentDescription = "Production",
                            tint = Color(0xFFFDBA74),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TODAY'S PRODUCTION OUTPUT",
                            color = Color(0xFFFED7AA),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Molding & Vibro-Press Yield",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "$batchesCount Batches",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Big Total Production Output Value
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format(Locale.US, "%,d", totalMolded),
                    color = Color(0xFFFDBA74),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.testTag("today_metric_units")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "pieces molded",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Yield & Defect Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF065F46)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format(Locale.US, "%,d Usable Blocks", usableBlocks),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = if (defects > 0) Color(0xFF7F1D1D) else Color(0xFF1E293B)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (defects > 0) Color(0xFFF87171) else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format(Locale.US, "%d Defects (%.1f%%)", defects, defectRatePct),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Distribution Breakdown by Block Types (10, 15, 20)
            Text(
                text = "Today's Output by Size Spec (Ethiopian Standards)",
                color = Color(0xFFFEF3C7),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Proportional Multi-Segment Progress Bar
            if (totalMolded > 0) {
                val qty10 = outputByType["Blocket 10"] ?: 0
                val qty15 = outputByType["Blocket 15"] ?: 0
                val qty20 = outputByType["Blocket 20"] ?: 0

                val frac10 = (qty10.toFloat() / totalMolded).coerceIn(0f, 1f)
                val frac15 = (qty15.toFloat() / totalMolded).coerceIn(0f, 1f)
                val frac20 = (qty20.toFloat() / totalMolded).coerceIn(0f, 1f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    if (frac10 > 0) {
                        Box(
                            modifier = Modifier
                                .weight(frac10)
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(AmberPrimary)
                        )
                    }
                    if (frac15 > 0) {
                        Box(
                            modifier = Modifier
                                .weight(frac15)
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Color(0xFF38BDF8))
                        )
                    }
                    if (frac20 > 0) {
                        Box(
                            modifier = Modifier
                                .weight(frac20)
                                .fillMaxWidth()
                                .height(10.dp)
                                .background(Color(0xFFA78BFA))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Chips row for 10, 15, 20
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BlockTypeOutputChip(
                    typeName = "Blocket 10",
                    count = outputByType["Blocket 10"] ?: 0,
                    dotColor = AmberPrimary,
                    modifier = Modifier.weight(1f)
                )
                BlockTypeOutputChip(
                    typeName = "Blocket 15",
                    count = outputByType["Blocket 15"] ?: 0,
                    dotColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                BlockTypeOutputChip(
                    typeName = "Blocket 20",
                    count = outputByType["Blocket 20"] ?: 0,
                    dotColor = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BlockTypeOutputChip(
    typeName: String,
    count: Int,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = typeName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count pcs",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun DailySubMetric(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1E293B)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = tint,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun DailyActionShortcutsRow(
    onLogBatchClick: () -> Unit,
    onNewSaleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onNewSaleClick,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("action_record_sale_today"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ProfitGreen,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.PointOfSale,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Record Sale",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }

        Button(
            onClick = onLogBatchClick,
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .testTag("action_log_batch_today"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AmberPrimary,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Batch",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun DailyEfficiencyRibbon(
    topSellingBlock: String,
    todayLaborCost: Double,
    totalBlocksMolded: Int,
    modifier: Modifier = Modifier
) {
    val laborPerBlock = if (totalBlocksMolded > 0) todayLaborCost / totalBlocksMolded else 0.0

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Top Brick Demand",
                    fontSize = 12.sp,
                    color = Slate700,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = topSellingBlock,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Highest sales today",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfitGreen
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Labor Cost / Block",
                    fontSize = 12.sp,
                    color = Slate700,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (totalBlocksMolded > 0) String.format(Locale.US, "ETB %.2f", laborPerBlock) else "ETB 0.00",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = String.format(Locale.US, "ETB %,.0f total labor", todayLaborCost),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
            }
        }
    }
}

@Composable
fun DailyPaymentBreakdownCard(
    breakdown: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Payment Channels",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Slate900
                )
                Text(
                    text = "Ethiopian Banks & Wallets",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                breakdown.forEach { (channel, amount) ->
                    val (icon, color) = when {
                        channel.contains("Telebirr", ignoreCase = true) -> Pair(Icons.Default.Phone, Color(0xFF0284C7))
                        channel.contains("CBE", ignoreCase = true) -> Pair(Icons.Default.AccountBalance, Color(0xFF7C3AED))
                        channel.contains("Bank", ignoreCase = true) -> Pair(Icons.Default.AccountBalance, Color(0xFF2563EB))
                        else -> Pair(Icons.Default.AttachMoney, ProfitGreen)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate200.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = channel,
                                    tint = color,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = channel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        Text(
                            text = String.format(Locale.US, "ETB %,.2f", amount),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TodaySalesOrdersCard(
    orders: List<SalesOrderEntity>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNewSaleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("today_sales_orders_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ProfitGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = ProfitGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Today's Sales Bookings (${orders.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Slate700
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (orders.isEmpty()) {
                        EmptyDayState(
                            message = "No sales orders logged yet today.",
                            actionLabel = "Record Today's First Sale",
                            onClick = onNewSaleClick
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            orders.forEach { order ->
                                DailyOrderItemRow(order = order)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyOrderItemRow(
    order: SalesOrderEntity,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
    val timeString = timeFormat.format(Date(order.orderDateMillis))

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Slate200.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.customerName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${order.orderNumber} • $timeString",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                }

                PaymentStatusChip(status = order.paymentStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.quantity} pcs • ${order.blockProductName}",
                    fontSize = 13.sp,
                    color = Slate800,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = String.format(Locale.US, "ETB %,.0f", order.totalAmount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Slate900
                )
            }

            if (order.balanceAmount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Paid: " + String.format(Locale.US, "ETB %,.0f", order.paidAmount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfitGreen
                    )
                    Text(
                        text = "Credit Balance: " + String.format(Locale.US, "ETB %,.0f", order.balanceAmount),
                        fontSize = 12.sp,
                        color = Color(0xFFB45309),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun TodayProductionBatchesCard(
    batches: List<ProductionBatchEntity>,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onLogBatchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("today_production_batches_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AmberPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = AmberPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Today's Molded Batches (${batches.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Slate700
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (batches.isEmpty()) {
                        EmptyDayState(
                            message = "No production batches logged yet today.",
                            actionLabel = "Log Today's First Batch",
                            onClick = onLogBatchClick
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            batches.forEach { batch ->
                                DailyBatchItemRow(batch = batch)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyBatchItemRow(
    batch: ProductionBatchEntity,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.US)
    val timeString = timeFormat.format(Date(batch.dateMillis))

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Slate200.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = batch.batchCode,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $timeString",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AmberPrimary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = batch.blockProductName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${batch.quantityMolded} molded • ${batch.quantityMolded - batch.defectCount} usable",
                    fontSize = 13.sp,
                    color = Slate800,
                    fontWeight = FontWeight.Bold
                )

                if (batch.defectCount > 0) {
                    Text(
                        text = "${batch.defectCount} defects",
                        fontSize = 12.sp,
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "0 defects (100% yield)",
                        fontSize = 12.sp,
                        color = ProfitGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (batch.supervisorName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Supervisor: ${batch.supervisorName}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700
                )
            }
        }
    }
}

@Composable
private fun EmptyDayState(
    message: String,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Slate200.copy(alpha = 0.4f))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Slate800
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.height(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = actionLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
