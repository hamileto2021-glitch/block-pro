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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import com.example.ui.components.BatchDateFilter
import com.example.ui.components.BatchDateSort
import com.example.ui.components.BatchEmptyStateCard
import com.example.ui.components.BatchSearchAndFilterSection
import com.example.ui.components.BatchTypeFilter
import com.example.ui.components.filterBatches
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BlockProductEntity
import com.example.data.model.ProductionBatchEntity
import com.example.data.model.RawMaterialEntity
import com.example.ui.components.EditProductCostPriceDialog
import com.example.ui.components.LogProductionBatchDialog
import com.example.ui.components.RestockMaterialDialog
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenContainer
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer
import com.example.ui.viewmodel.BlocketViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StocksScreen(
    viewModel: BlocketViewModel,
    initialTabIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    val materials by viewModel.rawMaterials.collectAsStateWithLifecycle()
    val products by viewModel.blockProducts.collectAsStateWithLifecycle()
    val batches by viewModel.productionBatches.collectAsStateWithLifecycle()

    var selectedTabIndex by remember(initialTabIndex) { mutableIntStateOf(initialTabIndex) }
    val tabs = listOf("Finished Blocks", "Raw Materials", "Production Batches")

    var showBatchDialog by remember { mutableStateOf(false) }
    var restockTargetMaterial by remember { mutableStateOf<RawMaterialEntity?>(null) }
    var editTargetProduct by remember { mutableStateOf<BlockProductEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("stocks_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showBatchDialog = true },
                containerColor = AmberPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_log_batch")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Log Production")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Log Batch", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Header
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = AmberPrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Black else FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> FinishedBlocksTab(
                    products = products,
                    onEditCostPrice = { prod -> editTargetProduct = prod }
                )
                1 -> RawMaterialsTab(
                    materials = materials,
                    onRestockClick = { mat -> restockTargetMaterial = mat }
                )
                2 -> ProductionBatchesTab(batches = batches)
            }
        }
    }

    if (showBatchDialog) {
        LogProductionBatchDialog(
            products = products,
            onDismiss = { showBatchDialog = false },
            onConfirm = { batchCode, productId, qty, defects, cement, sand, gravel, water, labor, sup, notes ->
                viewModel.logProduction(batchCode, productId, qty, defects, cement, sand, gravel, water, labor, sup, notes)
            }
        )
    }

    restockTargetMaterial?.let { mat ->
        RestockMaterialDialog(
            material = mat,
            onDismiss = { restockTargetMaterial = null },
            onConfirm = { addedQty, unitCost ->
                viewModel.restockMaterial(mat.id, addedQty, unitCost)
            }
        )
    }

    editTargetProduct?.let { prod ->
        EditProductCostPriceDialog(
            product = prod,
            onDismiss = { editTargetProduct = null },
            onConfirm = { customCost, customPrice ->
                viewModel.updateProductCostAndPrice(prod.id, customCost, customPrice)
            }
        )
    }
}

@Composable
private fun FinishedBlocksTab(
    products: List<BlockProductEntity>,
    onEditCostPrice: (BlockProductEntity) -> Unit
) {
    var selectedTypeFilter by remember { mutableStateOf("ALL") }

    val filteredProducts = remember(products, selectedTypeFilter) {
        when (selectedTypeFilter) {
            "10" -> products.filter { it.name.contains("10") }
            "15" -> products.filter { it.name.contains("15") }
            "20" -> products.filter { it.name.contains("20") }
            else -> products
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = AmberPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Finished Concrete Blockets (10, 15 & 20)",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "10 cm (partition), 15 cm (standard), and 20 cm (load-bearing)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    }
                }
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("finished_blocks_filter_row")
            ) {
                val filterOptions = listOf(
                    "ALL" to "All Blocks (${products.size})",
                    "10" to "Blocket 10",
                    "15" to "Blocket 15",
                    "20" to "Blocket 20"
                )
                items(filterOptions) { (key, label) ->
                    val isSelected = selectedTypeFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTypeFilter = key },
                        label = {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AmberPrimary.copy(alpha = 0.18f),
                            selectedLabelColor = AmberPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) AmberPrimary else Slate200,
                            selectedBorderColor = AmberPrimary
                        ),
                        modifier = Modifier.testTag("filter_product_$key")
                    )
                }
            }
        }

        items(filteredProducts) { product ->
            val isLowStock = product.currentStock <= product.minStockThreshold

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Spec: ${product.sizeSpec}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isLowStock) WarningAmberContainer else ProfitGreenContainer)
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (isLowStock) "LOW STOCK" else "IN STOCK",
                                color = if (isLowStock) WarningAmber else ProfitGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Current Stock", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                            Text(
                                text = "${product.currentStock} pcs",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column {
                            Text("Unit Cost", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                            Text(
                                text = String.format(Locale.US, "ETB %,.2f", product.unitCost),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column {
                            Text("Selling Price", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                            Text(
                                text = String.format(Locale.US, "ETB %,.2f", product.unitPrice),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = ProfitGreen
                            )
                        }
                        Column {
                            Text("Defects", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                            Text(
                                text = "${product.brokenCount} pcs",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val margin = product.unitPrice - product.unitCost
                        val marginPct = if (product.unitPrice > 0) (margin / product.unitPrice) * 100 else 0.0
                        Text(
                            text = String.format(Locale.US, "Margin: ETB %,.2f/pc (%.1f%%)", margin, marginPct),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (margin >= 0) ProfitGreen else ExpenseRed
                        )

                        OutlinedButton(
                            onClick = { onEditCostPrice(product) },
                            modifier = Modifier.testTag("edit_rates_${product.id}"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Cost & Price", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RawMaterialsTab(
    materials: List<RawMaterialEntity>,
    onRestockClick: (RawMaterialEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(materials) { mat ->
            val isLow = mat.currentStock <= mat.minAlertThreshold

            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mat.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "Unit Cost: ETB ${mat.costPerUnit} / ${mat.unit}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                        }

                        OutlinedButton(
                            onClick = { onRestockClick(mat) },
                            modifier = Modifier.testTag("restock_${mat.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restock", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text("Available On Site", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                            Text(
                                text = String.format(Locale.US, "%.1f %s", mat.currentStock, mat.unit),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isLow) ExpenseRed else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Min Safety Threshold: ${mat.minAlertThreshold.toInt()} ${mat.unit}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val ratio = (mat.currentStock / (mat.minAlertThreshold * 2.5)).toFloat().coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isLow) WarningAmber else ProfitGreen,
                        trackColor = Slate200
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductionBatchesTab(batches: List<ProductionBatchEntity>) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(BatchTypeFilter.ALL) }
    var selectedDate by remember { mutableStateOf(BatchDateFilter.ALL) }
    var dateSort by remember { mutableStateOf(BatchDateSort.NEWEST_FIRST) }

    val filteredBatches = remember(batches, searchQuery, selectedType, selectedDate, dateSort) {
        filterBatches(batches, searchQuery, selectedType, selectedDate, dateSort)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("production_batches_list"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Vibro-Press Machinery Hero Card
        item {
            com.example.ui.components.ProductionMachineCard()
        }

        // 2. Interactive Search and Filter Section (Types 10, 15, 20 & Date Added)
        item {
            BatchSearchAndFilterSection(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                selectedType = selectedType,
                onTypeSelect = { selectedType = it },
                selectedDate = selectedDate,
                onDateSelect = { selectedDate = it },
                dateSort = dateSort,
                onToggleSort = {
                    dateSort = if (dateSort == BatchDateSort.NEWEST_FIRST) {
                        BatchDateSort.OLDEST_FIRST
                    } else {
                        BatchDateSort.NEWEST_FIRST
                    }
                },
                totalBatchesCount = batches.size,
                filteredBatchesCount = filteredBatches.size,
                onResetFilters = {
                    searchQuery = ""
                    selectedType = BatchTypeFilter.ALL
                    selectedDate = BatchDateFilter.ALL
                    dateSort = BatchDateSort.NEWEST_FIRST
                },
                batches = batches
            )
        }

        // 3. Batch Items or Contextual Empty State
        if (batches.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No production batches logged yet.", color = Slate400)
                }
            }
        } else if (filteredBatches.isEmpty()) {
            item {
                BatchEmptyStateCard(
                    query = searchQuery,
                    selectedType = selectedType,
                    selectedDate = selectedDate,
                    onResetFilters = {
                        searchQuery = ""
                        selectedType = BatchTypeFilter.ALL
                        selectedDate = BatchDateFilter.ALL
                        dateSort = BatchDateSort.NEWEST_FIRST
                    }
                )
            }
        } else {
            items(filteredBatches, key = { it.id }) { batch ->
                com.example.ui.components.DetailedBatchTrackingCard(batch = batch)
            }
        }
    }
}
