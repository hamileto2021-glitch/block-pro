package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.viewmodel.DailyTrendPoint
import com.example.ui.viewmodel.ProductProductionShare
import java.util.Locale
import kotlin.math.max

enum class ChartType {
    BARS, AREA
}

/**
 * Modern interactive financial chart inspired by Recharts aesthetic:
 * - Interactive point inspection (tap to inspect exact day metrics)
 * - Grouped Bars or Area Gradient curve visualization
 * - Dual series: Revenue from Sales vs. Total Production Costs
 */
@Composable
fun DailyFinancialCanvasChart(
    trends: List<DailyTrendPoint>,
    modifier: Modifier = Modifier
) {
    var chartType by remember { mutableStateOf(ChartType.BARS) }
    var selectedIndex by remember { mutableIntStateOf(if (trends.isNotEmpty()) trends.size - 1 else -1) }

    val activePoint = trends.getOrNull(selectedIndex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_financial_chart_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Chart Type Switcher & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Financial Performance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sales Revenue vs. Production Costs",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                }

                // Type Toggle (Bars vs Area)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate200)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (chartType == ChartType.BARS) AmberPrimary else Color.Transparent)
                            .clickable { chartType = ChartType.BARS }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Bar Chart",
                            tint = if (chartType == ChartType.BARS) Color.White else Slate700,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (chartType == ChartType.AREA) AmberPrimary else Color.Transparent)
                            .clickable { chartType = ChartType.AREA }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Area Chart",
                            tint = if (chartType == ChartType.AREA) Color.White else Slate700,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Recharts-style Active Inspection Tooltip / Scrubber Pill
            activePoint?.let { point ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate900)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${point.dayLabel} Selected",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ProfitGreen))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Rev: " + String.format(Locale.US, "ETB %,.0f", point.revenue),
                                color = Color(0xFF4ADE80),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cost: " + String.format(Locale.US, "ETB %,.0f", point.expense),
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val profit = point.revenue - point.expense
                            Text(
                                text = "Net: " + String.format(Locale.US, "ETB %,.0f", profit),
                                color = if (profit >= 0) Color(0xFF86EFAC) else Color(0xFFF87171),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (trends.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No trend data available", color = Slate400, fontSize = 13.sp)
                }
            } else {
                val maxVal = max(
                    100.0,
                    trends.maxOf { max(it.revenue, it.expense) } * 1.18
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .pointerInput(trends) {
                            detectTapGestures { offset ->
                                val slotWidth = size.width / trends.size
                                val clickedIndex = (offset.x / slotWidth).toInt().coerceIn(0, trends.size - 1)
                                selectedIndex = clickedIndex
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val bottomPadding = 48f
                    val topPadding = 20f
                    val chartHeight = canvasHeight - bottomPadding - topPadding

                    val count = trends.size
                    val slotWidth = canvasWidth / count

                    // 1. Draw horizontal gridlines (Recharts CartesianGrid)
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = topPadding + (chartHeight * (i.toFloat() / gridSteps))
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1f
                        )
                    }

                    if (chartType == ChartType.BARS) {
                        // GROUPED BARS VIEW
                        val barWidth = (slotWidth * 0.32f).coerceAtMost(26f)

                        trends.forEachIndexed { index, point ->
                            val slotCenter = (index * slotWidth) + (slotWidth / 2f)

                            val revHeight = ((point.revenue / maxVal) * chartHeight).toFloat().coerceAtLeast(4f)
                            val expHeight = ((point.expense / maxVal) * chartHeight).toFloat().coerceAtLeast(4f)

                            val revLeft = slotCenter - barWidth - 2f
                            val expLeft = slotCenter + 2f

                            val revTop = topPadding + chartHeight - revHeight
                            val expTop = topPadding + chartHeight - expHeight

                            // Selection highlight column
                            if (index == selectedIndex) {
                                drawRect(
                                    color = Color(0xFFF1F5F9),
                                    topLeft = Offset(index * slotWidth, topPadding),
                                    size = Size(slotWidth, chartHeight)
                                )
                            }

                            // Revenue Bar (Green)
                            drawRoundRect(
                                color = ProfitGreen,
                                topLeft = Offset(revLeft, revTop),
                                size = Size(barWidth, revHeight),
                                cornerRadius = CornerRadius(4f, 4f)
                            )

                            // Expense / Cost Bar (Red)
                            drawRoundRect(
                                color = ExpenseRed,
                                topLeft = Offset(expLeft, expTop),
                                size = Size(barWidth, expHeight),
                                cornerRadius = CornerRadius(4f, 4f)
                            )

                            // Day Label
                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = if (index == selectedIndex) android.graphics.Color.BLACK else android.graphics.Color.rgb(30, 41, 59)
                                    textSize = 32f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                    isAntiAlias = true
                                }
                                drawText(
                                    point.dayLabel,
                                    slotCenter,
                                    canvasHeight - 10f,
                                    paint
                                )
                            }
                        }
                    } else {
                        // AREA / LINE GRADIENT VIEW (Classic Recharts AreaChart)
                        val revenuePath = Path()
                        val revenueFillPath = Path()
                        val expensePath = Path()
                        val expenseFillPath = Path()

                        val revPoints = mutableListOf<Offset>()
                        val expPoints = mutableListOf<Offset>()

                        trends.forEachIndexed { index, point ->
                            val slotCenter = (index * slotWidth) + (slotWidth / 2f)
                            val revY = (topPadding + chartHeight - ((point.revenue / maxVal) * chartHeight)).toFloat()
                            val expY = (topPadding + chartHeight - ((point.expense / maxVal) * chartHeight)).toFloat()

                            revPoints.add(Offset(slotCenter, revY))
                            expPoints.add(Offset(slotCenter, expY))
                        }

                        // Build Area curves
                        if (revPoints.isNotEmpty()) {
                            revenuePath.moveTo(revPoints.first().x, revPoints.first().y)
                            revenueFillPath.moveTo(revPoints.first().x, topPadding + chartHeight)
                            revenueFillPath.lineTo(revPoints.first().x, revPoints.first().y)

                            for (i in 1 until revPoints.size) {
                                val prev = revPoints[i - 1]
                                val curr = revPoints[i]
                                val midX = (prev.x + curr.x) / 2f
                                revenuePath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                revenueFillPath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                            revenueFillPath.lineTo(revPoints.last().x, topPadding + chartHeight)
                            revenueFillPath.close()

                            // Fill with green gradient
                            drawPath(
                                path = revenueFillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(ProfitGreen.copy(alpha = 0.35f), ProfitGreen.copy(alpha = 0.02f)),
                                    startY = topPadding,
                                    endY = topPadding + chartHeight
                                )
                            )
                            drawPath(
                                path = revenuePath,
                                color = ProfitGreen,
                                style = Stroke(width = 4f, cap = StrokeCap.Round)
                            )
                        }

                        if (expPoints.isNotEmpty()) {
                            expensePath.moveTo(expPoints.first().x, expPoints.first().y)
                            expenseFillPath.moveTo(expPoints.first().x, topPadding + chartHeight)
                            expenseFillPath.lineTo(expPoints.first().x, expPoints.first().y)

                            for (i in 1 until expPoints.size) {
                                val prev = expPoints[i - 1]
                                val curr = expPoints[i]
                                val midX = (prev.x + curr.x) / 2f
                                expensePath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                expenseFillPath.cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                            expenseFillPath.lineTo(expPoints.last().x, topPadding + chartHeight)
                            expenseFillPath.close()

                            drawPath(
                                path = expenseFillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(ExpenseRed.copy(alpha = 0.25f), ExpenseRed.copy(alpha = 0.01f)),
                                    startY = topPadding,
                                    endY = topPadding + chartHeight
                                )
                            )
                            drawPath(
                                path = expensePath,
                                color = ExpenseRed,
                                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
                            )
                        }

                        // Draw data points & day labels
                        trends.forEachIndexed { index, point ->
                            val slotCenter = (index * slotWidth) + (slotWidth / 2f)
                            val revY = revPoints[index].y
                            val expY = expPoints[index].y

                            // Dots
                            drawCircle(color = ProfitGreen, radius = 5f, center = Offset(slotCenter, revY))
                            drawCircle(color = Color.White, radius = 2.5f, center = Offset(slotCenter, revY))

                            drawCircle(color = ExpenseRed, radius = 5f, center = Offset(slotCenter, expY))
                            drawCircle(color = Color.White, radius = 2.5f, center = Offset(slotCenter, expY))

                            drawContext.canvas.nativeCanvas.apply {
                                val paint = android.graphics.Paint().apply {
                                    color = if (index == selectedIndex) android.graphics.Color.BLACK else android.graphics.Color.rgb(30, 41, 59)
                                    textSize = 32f
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                    isAntiAlias = true
                                }
                                drawText(point.dayLabel, slotCenter, canvasHeight - 10f, paint)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Breakdown of Total Production Costs:
 * Materials + Direct Labor + Machine Maintenance & Overheads
 */
@Composable
fun ProductionCostBreakdownCard(
    totalCost: Double,
    rawMaterialCost: Double,
    directLaborCost: Double,
    otherCosts: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("production_cost_breakdown_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Production Cost Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Cost factors to manufacture concrete blocks",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                }

                Text(
                    text = String.format(Locale.US, "ETB %,.2f", totalCost),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = ExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val safeTotal = max(1.0, totalCost)
            val matPct = ((rawMaterialCost / safeTotal) * 100.0).toFloat().coerceIn(0f, 100f)
            val laborPct = ((directLaborCost / safeTotal) * 100.0).toFloat().coerceIn(0f, 100f)
            val otherPct = ((otherCosts / safeTotal) * 100.0).toFloat().coerceIn(0f, 100f)

            // Segment 1: Raw Materials
            CostBreakdownItem(
                title = "Raw Materials (Cement, Sand, Gravel)",
                amount = rawMaterialCost,
                percentage = matPct,
                color = AmberPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Segment 2: Direct Labor & Wages
            CostBreakdownItem(
                title = "Labor, Stacking & Daily Wages",
                amount = directLaborCost,
                percentage = laborPct,
                color = InfoBlue
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Segment 3: Maintenance & Plant Overheads
            CostBreakdownItem(
                title = "Equipment Maintenance & Utilities",
                amount = otherCosts,
                percentage = otherPct,
                color = Slate700
            )
        }
    }
}

@Composable
private fun CostBreakdownItem(
    title: String,
    amount: Double,
    percentage: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = String.format(Locale.US, "ETB %,.2f (%.1f%%)", amount, percentage),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Slate900
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Slate200
        )
    }
}

@Composable
fun ProductionOutputDailyChart(
    trends: List<DailyTrendPoint>,
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
                Column {
                    Text(
                        text = "Daily Block Production Output",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Units molded per working day",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Units",
                        color = AmberPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val maxOutput = max(500, (trends.maxOfOrNull { it.outputBlocks } ?: 1000) + 200)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val bottomPadding = 40f
                val topPadding = 16f
                val chartHeight = canvasHeight - bottomPadding - topPadding

                val count = trends.size
                if (count == 0) return@Canvas
                val slotWidth = canvasWidth / count
                val barWidth = (slotWidth * 0.5f).coerceAtMost(36f)

                trends.forEachIndexed { index, point ->
                    val slotCenter = (index * slotWidth) + (slotWidth / 2f)
                    val barHeight = ((point.outputBlocks.toFloat() / maxOutput) * chartHeight).coerceAtLeast(6f)
                    val barLeft = slotCenter - (barWidth / 2f)
                    val barTop = topPadding + chartHeight - barHeight

                    drawRoundRect(
                        color = AmberPrimary,
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    if (point.outputBlocks > 0) {
                        drawContext.canvas.nativeCanvas.apply {
                            val valPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.DKGRAY
                                textSize = 28f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                                isAntiAlias = true
                            }
                            drawText(
                                point.outputBlocks.toString(),
                                slotCenter,
                                barTop - 6f,
                                valPaint
                            )
                        }
                    }

                    drawContext.canvas.nativeCanvas.apply {
                        val lblPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.rgb(30, 41, 59)
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                            isAntiAlias = true
                        }
                        drawText(
                            point.dayLabel,
                            slotCenter,
                            canvasHeight - 6f,
                            lblPaint
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductShareBreakdownCard(
    shares: List<ProductProductionShare>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Production Mix by Block Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Output share across product lines",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Slate700
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (shares.isEmpty()) {
                Text(
                    text = "No production batches recorded in this period.",
                    color = Slate700,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                shares.forEach { share ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = share.productName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${share.quantity} pcs (${String.format(Locale.US, "%.1f", share.percentage)}%)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Slate900
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (share.percentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AmberPrimary,
                            trackColor = Slate200
                        )
                    }
                }
            }
        }
    }
}
