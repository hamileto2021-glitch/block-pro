package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ProductionBatchEntity
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoBlueContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenContainer
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProductionMachineCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("production_machine_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_block_batch),
                contentDescription = "Vibro-Press Block Machine",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Slate900.copy(alpha = 0.95f),
                                Slate900.copy(alpha = 0.75f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AmberPrimary
                ) {
                    Text(
                        text = "VIBRO-PRESS LINE 1 & 2",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Automated Batch Molding",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "High-pressure compaction & hydraulic vibration for maximum compressive strength",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun DetailedBatchTrackingCard(
    batch: ProductionBatchEntity,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val batchDateStr = dateFormat.format(Date(batch.dateMillis))

    val isCuring = batch.notes.contains("curing", ignoreCase = true) ||
            batch.batchCode.endsWith("44") ||
            (System.currentTimeMillis() - batch.dateMillis < 7L * 24 * 60 * 60 * 1000)

    val daysSinceProduction = ((System.currentTimeMillis() - batch.dateMillis) / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(0)
    val curingProgress = (daysSinceProduction.toFloat() / 7f).coerceIn(0.1f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("batch_tracking_card_${batch.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Batch Code, Brick Type, Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AmberPrimary.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = batch.batchCode,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = AmberPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = batchDateStr,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = batch.blockProductName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Curing / Ready Status Chip
                if (isCuring && daysSinceProduction < 7) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WarningAmberContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = WarningAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CURING (Day $daysSinceProduction/7)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = WarningAmber
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ProfitGreenContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ProfitGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "READY (CURED)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = ProfitGreen
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Curing Progress Timeline if in curing phase
            if (isCuring && daysSinceProduction < 7) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate200.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "7-Day Water Curing Phase",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "${(curingProgress * 100).toInt()}% Done • ${7 - daysSinceProduction} days left",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = AmberPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { curingProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AmberPrimary,
                        trackColor = Slate200
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Output Quantities Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate200.copy(alpha = 0.5f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Molded", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Text(
                        text = "${batch.quantityMolded}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Accepted (Net)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Text(
                        text = "${batch.quantityMolded - batch.defectCount}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = ProfitGreen
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Defects", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Text(
                        text = "${batch.defectCount}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = if (batch.defectCount > 0) ExpenseRed else Slate700
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Labor Cost", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Text(
                        text = String.format(Locale.US, "ETB %,.0f", batch.laborCost),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Slate900
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Raw Material Ingredients Consumed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MaterialConsumptionPill(
                    label = "Cement",
                    value = "${batch.cementBagsUsed.toInt()} Bags",
                    modifier = Modifier.weight(1f)
                )
                MaterialConsumptionPill(
                    label = "Sand",
                    value = String.format(Locale.US, "%.1f m³", batch.sandUsedM3),
                    modifier = Modifier.weight(1f)
                )
                MaterialConsumptionPill(
                    label = "Aggregate",
                    value = String.format(Locale.US, "%.1f m³", batch.aggregateUsedM3),
                    modifier = Modifier.weight(1f)
                )
                MaterialConsumptionPill(
                    label = "Water",
                    value = "${batch.waterLitresUsed.toInt()} L",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Supervisor & Notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Engineering,
                        contentDescription = null,
                        tint = Slate700,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Supervisor: ${batch.supervisorName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                }

                if (batch.notes.isNotEmpty()) {
                    Text(
                        text = batch.notes,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate700,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialConsumptionPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Slate200.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 5.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate700)
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Slate900,
                maxLines = 1
            )
        }
    }
}
