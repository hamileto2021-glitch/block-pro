package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductionBatchEntity
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import java.util.Calendar

/**
 * Filter options for brick blocket types (10, 15, 20, Solid).
 */
enum class BatchTypeFilter(val label: String, val tag: String) {
    ALL("All Types", "filter_type_all"),
    BLOCKET_10("Blocket 10", "filter_type_10"),
    BLOCKET_15("Blocket 15", "filter_type_15"),
    BLOCKET_20("Blocket 20", "filter_type_20"),
    SOLID("Solid Brick", "filter_type_solid")
}

/**
 * Filter options for batch production date added.
 */
enum class BatchDateFilter(val label: String, val tag: String) {
    ALL("All Dates", "filter_date_all"),
    TODAY("Today", "filter_date_today"),
    LAST_7_DAYS("Last 7 Days", "filter_date_week"),
    THIS_MONTH("This Month", "filter_date_month")
}

/**
 * Sort options for batch list ordering.
 */
enum class BatchDateSort(val label: String, val tag: String) {
    NEWEST_FIRST("Newest First", "sort_newest"),
    OLDEST_FIRST("Oldest First", "sort_oldest")
}

/**
 * Pure filtering and sorting function for brick production batches.
 * Supports text search across batch codes, types, supervisors, and notes,
 * specific brick type filtering (Blocket 10, 15, 20), date range intervals,
 * and date sorting.
 */
fun filterBatches(
    batches: List<ProductionBatchEntity>,
    query: String,
    typeFilter: BatchTypeFilter,
    dateFilter: BatchDateFilter,
    dateSort: BatchDateSort = BatchDateSort.NEWEST_FIRST,
    nowMillis: Long = System.currentTimeMillis()
): List<ProductionBatchEntity> {
    val trimmed = query.trim().lowercase()

    // Precalculate calendar date thresholds
    val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfToday = cal.timeInMillis

    val sevenDaysAgo = nowMillis - (7L * 24 * 60 * 60 * 1000)

    cal.set(Calendar.DAY_OF_MONTH, 1)
    val startOfMonth = cal.timeInMillis

    return batches.filter { batch ->
        // 1. Text Search Query Filter
        val matchesQuery = if (trimmed.isEmpty()) {
            true
        } else {
            batch.batchCode.lowercase().contains(trimmed) ||
            batch.blockProductName.lowercase().contains(trimmed) ||
            batch.supervisorName.lowercase().contains(trimmed) ||
            batch.notes.lowercase().contains(trimmed)
        }

        // 2. Brick Type Filter (Blocket 10, 15, 20, Solid)
        val matchesType = when (typeFilter) {
            BatchTypeFilter.ALL -> true
            BatchTypeFilter.BLOCKET_10 -> batch.blockProductName.contains("10", ignoreCase = true)
            BatchTypeFilter.BLOCKET_15 -> batch.blockProductName.contains("15", ignoreCase = true)
            BatchTypeFilter.BLOCKET_20 -> batch.blockProductName.contains("20", ignoreCase = true)
            BatchTypeFilter.SOLID -> batch.blockProductName.contains("solid", ignoreCase = true)
        }

        // 3. Date Added Filter
        val matchesDate = when (dateFilter) {
            BatchDateFilter.ALL -> true
            BatchDateFilter.TODAY -> batch.dateMillis >= startOfToday
            BatchDateFilter.LAST_7_DAYS -> batch.dateMillis >= sevenDaysAgo
            BatchDateFilter.THIS_MONTH -> batch.dateMillis >= startOfMonth
        }

        matchesQuery && matchesType && matchesDate
    }.let { list ->
        when (dateSort) {
            BatchDateSort.NEWEST_FIRST -> list.sortedByDescending { it.dateMillis }
            BatchDateSort.OLDEST_FIRST -> list.sortedBy { it.dateMillis }
        }
    }
}

/**
 * Modern Material 3 Search and Filter control panel for brick batches.
 */
@Composable
fun BatchSearchAndFilterSection(
    query: String,
    onQueryChange: (String) -> Unit,
    selectedType: BatchTypeFilter,
    onTypeSelect: (BatchTypeFilter) -> Unit,
    selectedDate: BatchDateFilter,
    onDateSelect: (BatchDateFilter) -> Unit,
    dateSort: BatchDateSort,
    onToggleSort: () -> Unit,
    totalBatchesCount: Int,
    filteredBatchesCount: Int,
    onResetFilters: () -> Unit,
    batches: List<ProductionBatchEntity>,
    modifier: Modifier = Modifier
) {
    val isFiltered = query.isNotBlank() || selectedType != BatchTypeFilter.ALL || selectedDate != BatchDateFilter.ALL

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("batch_filter_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_batch_input"),
                placeholder = {
                    Text(
                        text = "Search batch #, block type, supervisor...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate500
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AmberPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier.testTag("clear_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = Slate700,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberPrimary,
                    unfocusedBorderColor = Slate200,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // 2. Brick Type Filter Chips (Blocket 10, 15, 20)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Brick Type (10, 15, 20):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(BatchTypeFilter.values()) { filter ->
                        val count = when (filter) {
                            BatchTypeFilter.ALL -> batches.size
                            BatchTypeFilter.BLOCKET_10 -> batches.count { it.blockProductName.contains("10", ignoreCase = true) }
                            BatchTypeFilter.BLOCKET_15 -> batches.count { it.blockProductName.contains("15", ignoreCase = true) }
                            BatchTypeFilter.BLOCKET_20 -> batches.count { it.blockProductName.contains("20", ignoreCase = true) }
                            BatchTypeFilter.SOLID -> batches.count { it.blockProductName.contains("solid", ignoreCase = true) }
                        }

                        val isSelected = selectedType == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { onTypeSelect(filter) },
                            label = {
                                Text(
                                    text = "${filter.label} ($count)",
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberPrimary.copy(alpha = 0.18f),
                                selectedLabelColor = AmberPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) AmberPrimary else Slate200,
                                selectedBorderColor = AmberPrimary,
                                borderWidth = if (isSelected) 1.5.dp else 1.dp
                            ),
                            modifier = Modifier.testTag(filter.tag)
                        )
                    }
                }
            }

            // 3. Date Added Filter Chips & Sort Toggle
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Slate700,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Date Added:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    // Sort Order Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clickable(onClick = onToggleSort)
                            .testTag("btn_sort_date")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Toggle sort",
                                tint = AmberPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dateSort.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(BatchDateFilter.values()) { filter ->
                        val isSelected = selectedDate == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { onDateSelect(filter) },
                            label = {
                                Text(
                                    text = filter.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberPrimary.copy(alpha = 0.18f),
                                selectedLabelColor = AmberPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) AmberPrimary else Slate200,
                                selectedBorderColor = AmberPrimary,
                                borderWidth = if (isSelected) 1.5.dp else 1.dp
                            ),
                            modifier = Modifier.testTag(filter.tag)
                        )
                    }
                }
            }

            // 4. Status Bar & Active Filters Reset
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "Showing $filteredBatchesCount of $totalBatchesCount batches",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isFiltered,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AmberPrimary.copy(alpha = 0.15f),
                        modifier = Modifier
                            .clickable(onClick = onResetFilters)
                            .testTag("clear_all_filters")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear all filters",
                                tint = AmberPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear Filters",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = AmberPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty state displayed when no batches match the search and filter query.
 */
@Composable
fun BatchEmptyStateCard(
    query: String,
    selectedType: BatchTypeFilter,
    selectedDate: BatchDateFilter,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("batch_empty_state_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AmberPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FilterListOff,
                    contentDescription = null,
                    tint = AmberPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No Brick Batches Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            val filterDescription = buildString {
                append("No batches match ")
                if (query.isNotBlank()) append("\"$query\" ")
                if (selectedType != BatchTypeFilter.ALL) append("for ${selectedType.label} ")
                if (selectedDate != BatchDateFilter.ALL) append("within ${selectedDate.label}")
            }.ifEmpty { "No batches logged in this system." }

            Text(
                text = filterDescription,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate700,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onResetFilters,
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_reset_filters")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Reset All Filters",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
