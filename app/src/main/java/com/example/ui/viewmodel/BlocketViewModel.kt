package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceEntity
import com.example.data.model.BlockProductEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.PaymentTransactionEntity
import com.example.data.model.ProductionBatchEntity
import com.example.data.model.RawMaterialEntity
import com.example.data.model.SalesOrderEntity
import com.example.data.model.WagePaymentEntity
import com.example.data.repository.BlocketRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimeFilter(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("7 Days"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

data class DailyTrendPoint(
    val dayLabel: String,
    val revenue: Double,
    val expense: Double, // Total Production & Operating Costs
    val outputBlocks: Int,
    val materialCost: Double = 0.0,
    val laborCost: Double = 0.0,
    val netProfit: Double = revenue - expense
)

data class ProductProductionShare(
    val productName: String,
    val quantity: Int,
    val percentage: Float
)

data class FinancialDashboardMetrics(
    val totalRevenue: Double = 0.0,
    val invoicedAmount: Double = 0.0,
    val totalExpenses: Double = 0.0, // Total Production Costs
    val rawMaterialCostTotal: Double = 0.0,
    val directLaborCostTotal: Double = 0.0,
    val otherExpensesTotal: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitMarginPct: Double = 0.0,
    val totalBlocksMolded: Int = 0,
    val totalBlocksSold: Int = 0,
    val totalDefectBlocks: Int = 0,
    val defectRatePct: Double = 0.0,
    val totalReceivables: Double = 0.0,
    val totalInventoryValue: Double = 0.0,
    val dailyTrends: List<DailyTrendPoint> = emptyList(),
    val productShares: List<ProductProductionShare> = emptyList(),
    val lowStockAlertsCount: Int = 0
)

data class DailyReportState(
    val dateMillis: Long = System.currentTimeMillis(),
    val formattedDate: String = "",
    val ethiopianDate: String = "",
    // Revenue & Sales Metrics for Today
    val todayRevenue: Double = 0.0, // Total collected cash/transfer today in ETB
    val todayInvoicedSales: Double = 0.0, // Total invoice value for today in ETB
    val todayPendingCredit: Double = 0.0, // Uncollected balance for orders created today in ETB
    val todaySalesCount: Int = 0, // Total orders today
    val todayBlocksSold: Int = 0, // Total block units sold today
    val averageOrderValue: Double = 0.0,
    val paymentMethodsBreakdown: Map<String, Double> = emptyMap(),
    val topSellingBlockName: String = "None",
    val todaySalesOrders: List<SalesOrderEntity> = emptyList(),

    // Production Output Metrics for Today
    val todayProductionOutput: Int = 0, // Total blocks molded today
    val todayDefectCount: Int = 0, // Defective blocks today
    val todayNetUsableBlocks: Int = 0, // Molded - Defects
    val todayDefectRatePct: Double = 0.0, // Defect %
    val todayBatchesCount: Int = 0, // Number of batches today
    val outputByBlockType: Map<String, Int> = emptyMap(), // E.g. "Blocket 10" -> 2400, "Blocket 15" -> 1800, "Blocket 20" -> 0
    val todayLaborCost: Double = 0.0,
    val todayEstimatedMaterialCost: Double = 0.0,
    val todayProductionBatches: List<ProductionBatchEntity> = emptyList()
)

class BlocketViewModel(private val repository: BlocketRepository) : ViewModel() {

    val selectedTimeFilter = MutableStateFlow(TimeFilter.THIS_WEEK)

    val rawMaterials: StateFlow<List<RawMaterialEntity>> = repository.rawMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blockProducts: StateFlow<List<BlockProductEntity>> = repository.blockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val productionBatches: StateFlow<List<ProductionBatchEntity>> = repository.productionBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employees: StateFlow<List<EmployeeEntity>> = repository.employees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceRecords: StateFlow<List<AttendanceEntity>> = repository.attendanceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wagePayments: StateFlow<List<WagePaymentEntity>> = repository.wagePayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val salesOrders: StateFlow<List<SalesOrderEntity>> = repository.salesOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentTransactions: StateFlow<List<PaymentTransactionEntity>> = repository.paymentTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<com.example.data.model.Expense>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private data class StateGroupA(
        val filter: TimeFilter,
        val materials: List<RawMaterialEntity>,
        val products: List<BlockProductEntity>
    )

    private data class StateGroupB(
        val batches: List<ProductionBatchEntity>,
        val orders: List<SalesOrderEntity>,
        val txns: List<PaymentTransactionEntity>,
        val wages: List<WagePaymentEntity>,
        val expensesList: List<com.example.data.model.Expense>
    )

    // Aggregated Dashboard State
    val dashboardMetrics: StateFlow<FinancialDashboardMetrics> = combine(
        combine(selectedTimeFilter, rawMaterials, blockProducts) { filter, materials, products ->
            StateGroupA(filter, materials, products)
        },
        combine(productionBatches, salesOrders, paymentTransactions, wagePayments, expenses) { batches, orders, txns, wages, exp ->
            StateGroupB(batches, orders, txns, wages, exp)
        }
    ) { groupA, groupB ->
        calculateDashboardMetrics(
            groupA.filter,
            groupA.materials,
            groupA.products,
            groupB.batches,
            groupB.orders,
            groupB.txns,
            groupB.wages,
            groupB.expensesList
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialDashboardMetrics())

    val dailyReport: StateFlow<DailyReportState> = combine(
        salesOrders,
        productionBatches,
        paymentTransactions,
        rawMaterials,
        blockProducts
    ) { orders, batches, txns, materials, products ->
        calculateDailyReport(orders, batches, txns, materials, products)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyReportState())

    fun setTimeFilter(filter: TimeFilter) {
        selectedTimeFilter.value = filter
    }

    // --- Actions ---

    fun logProduction(
        batchCode: String,
        productId: Long,
        quantityMolded: Int,
        defectCount: Int,
        cementBags: Double,
        sandM3: Double,
        aggregateM3: Double,
        waterL: Double,
        laborCost: Double,
        supervisorName: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.logProductionBatch(
                batchCode = batchCode.ifEmpty { "PRD-${System.currentTimeMillis().toString().takeLast(5)}" },
                productId = productId,
                quantityMolded = quantityMolded,
                defectCount = defectCount,
                cementBagsUsed = cementBags,
                sandUsedM3 = sandM3,
                aggregateUsedM3 = aggregateM3,
                waterLitresUsed = waterL,
                laborCost = laborCost,
                supervisorName = supervisorName.ifEmpty { "Lead Supervisor" },
                notes = notes
            )
        }
    }

    fun restockMaterial(materialId: Long, quantity: Double, unitCost: Double) {
        viewModelScope.launch {
            repository.restockRawMaterial(materialId, quantity, unitCost)
        }
    }

    fun createSalesOrder(
        customerName: String,
        customerPhone: String,
        address: String,
        productId: Long,
        quantity: Int,
        unitPrice: Double,
        deliveryFee: Double,
        initialPayment: Double,
        paymentMethod: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.createSalesOrder(
                customerName = customerName,
                customerPhone = customerPhone,
                deliveryAddress = address,
                productId = productId,
                quantity = quantity,
                unitPrice = unitPrice,
                deliveryFee = deliveryFee,
                initialPaymentAmount = initialPayment,
                paymentMethod = paymentMethod,
                notes = notes
            )
        }
    }

    fun recordOrderPayment(
        orderId: Long,
        amount: Double,
        paymentMethod: String,
        referenceNo: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordPayment(orderId, amount, paymentMethod, referenceNo, notes)
        }
    }

    fun updateDeliveryStatus(orderId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateDeliveryStatus(orderId, newStatus)
        }
    }

    fun markAttendance(
        employeeId: Long,
        status: String,
        overtimeHours: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            repository.saveAttendance(employeeId, status, overtimeHours, notes, cal.timeInMillis)
        }
    }

    fun recordWagePayout(
        employeeId: Long,
        amount: Double,
        paymentType: String,
        paymentMethod: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordWagePayment(employeeId, amount, paymentType, paymentMethod, notes)
        }
    }

    fun addEmployee(
        fullName: String,
        phone: String,
        role: String,
        wageType: String,
        rateAmount: Double
    ) {
        viewModelScope.launch {
            repository.addEmployee(fullName, phone, role, wageType, rateAmount)
        }
    }

    fun updateProductCostAndPrice(
        productId: Long,
        customCost: Double,
        customPrice: Double
    ) {
        viewModelScope.launch {
            repository.updateProductCostAndPrice(productId, customCost, customPrice)
        }
    }

    fun addBlockProduct(
        name: String,
        sizeSpec: String,
        initialStock: Int,
        unitCost: Double,
        unitPrice: Double,
        minStockThreshold: Int
    ) {
        viewModelScope.launch {
            repository.addNewBlockProduct(name, sizeSpec, initialStock, unitCost, unitPrice, minStockThreshold)
        }
    }

    // --- Helper Calculations ---

    private fun calculateDashboardMetrics(
        filter: TimeFilter,
        materials: List<RawMaterialEntity>,
        products: List<BlockProductEntity>,
        batches: List<ProductionBatchEntity>,
        orders: List<SalesOrderEntity>,
        txns: List<PaymentTransactionEntity>,
        wages: List<WagePaymentEntity>,
        expensesList: List<com.example.data.model.Expense>
    ): FinancialDashboardMetrics {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24L * 60 * 60 * 1000

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = cal.timeInMillis

        val (startTime, endTime) = when (filter) {
            TimeFilter.TODAY -> Pair(todayStart, now + 1000)
            TimeFilter.YESTERDAY -> Pair(todayStart - oneDayMillis, todayStart - 1)
            TimeFilter.THIS_WEEK -> Pair(todayStart - (6 * oneDayMillis), now + 1000)
            TimeFilter.THIS_MONTH -> {
                val monthCal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                Pair(monthCal.timeInMillis, now + 1000)
            }
            TimeFilter.ALL_TIME -> Pair(0L, Long.MAX_VALUE)
        }

        // Filtered transactions & orders
        val filteredTxns = txns.filter { it.dateMillis in startTime..endTime }
        val filteredOrders = orders.filter { it.orderDateMillis in startTime..endTime }
        val filteredBatches = batches.filter { it.dateMillis in startTime..endTime }
        val filteredWages = wages.filter { it.dateMillis in startTime..endTime }
        val filteredExpenses = expensesList.filter { it.dateMillis in startTime..endTime }

        // Revenue = Total collected cash/transfer in period
        val totalRevenue = filteredTxns.sumOf { it.amount }
        val invoicedAmount = filteredOrders.sumOf { it.totalAmount }

        // Material cost per unit lookup (ETB context)
        val cementCost = materials.find { it.name.contains("Cement", ignoreCase = true) }?.costPerUnit ?: 1650.00
        val sandCost = materials.find { it.name.contains("Sand", ignoreCase = true) }?.costPerUnit ?: 1100.00
        val gravelCost = materials.find { it.name.contains("Gravel", ignoreCase = true) || it.name.contains("Aggregate", ignoreCase = true) }?.costPerUnit ?: 1250.00

        // Expenses from Expense table + batch consumption
        val rawMaterialExpense = filteredBatches.sumOf { b ->
            (b.cementBagsUsed * cementCost) + (b.sandUsedM3 * sandCost) + (b.aggregateUsedM3 * gravelCost)
        }
        val batchLaborExpense = filteredBatches.sumOf { it.laborCost }
        val directWageExpense = filteredWages.sumOf { it.amount }
        val directExpenseRecords = filteredExpenses.sumOf { it.amount }

        val totalExpenses = if (directExpenseRecords > 0) directExpenseRecords else (rawMaterialExpense + batchLaborExpense + directWageExpense)

        val netProfit = totalRevenue - totalExpenses
        val profitMargin = if (totalRevenue > 0) (netProfit / totalRevenue) * 100.0 else 0.0

        val totalMolded = filteredBatches.sumOf { it.quantityMolded }
        val totalDefects = filteredBatches.sumOf { it.defectCount }
        val defectRate = if (totalMolded > 0) (totalDefects.toDouble() / totalMolded) * 100.0 else 0.0
        val totalSold = filteredOrders.sumOf { it.quantity }

        val totalReceivables = orders.sumOf { it.balanceAmount }

        // Inventory Valuation
        val matValuation = materials.sumOf { it.currentStock * it.costPerUnit }
        val prodValuation = products.sumOf { it.currentStock * it.unitCost }
        val totalInventoryValue = matValuation + prodValuation

        // Low stock count
        val lowMatCount = materials.count { it.currentStock <= it.minAlertThreshold }
        val lowProdCount = products.count { it.currentStock <= it.minStockThreshold }
        val lowStockAlerts = lowMatCount + lowProdCount

        // Breakdown of production costs
        val rawMaterialCostTotal = filteredBatches.sumOf { b ->
            (b.cementBagsUsed * cementCost) + (b.sandUsedM3 * sandCost) + (b.aggregateUsedM3 * gravelCost)
        }
        val directLaborCostTotal = filteredBatches.sumOf { it.laborCost } + filteredWages.sumOf { it.amount }
        val otherExpensesTotal = (totalExpenses - (rawMaterialCostTotal + directLaborCostTotal)).coerceAtLeast(0.0)

        // 7-day trend calculation (for Canvas charts)
        val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())
        val dailyTrends = (6 downTo 0).map { dayOffset ->
            val dayStart = todayStart - (dayOffset * oneDayMillis)
            val dayEnd = dayStart + oneDayMillis - 1
            val label = dayFormatter.format(Date(dayStart))

            val dayRevenue = txns.filter { it.dateMillis in dayStart..dayEnd }.sumOf { it.amount }
            val dayBatches = batches.filter { it.dateMillis in dayStart..dayEnd }
            val dayWages = wages.filter { it.dateMillis in dayStart..dayEnd }.sumOf { it.amount }

            val dayMaterialCost = dayBatches.sumOf { b ->
                (b.cementBagsUsed * cementCost) + (b.sandUsedM3 * sandCost) + (b.aggregateUsedM3 * gravelCost)
            }
            val dayLaborCost = dayBatches.sumOf { it.laborCost }
            val dayLaborTotal = dayLaborCost + dayWages
            val dayExpense = dayMaterialCost + dayLaborTotal
            val dayOutput = dayBatches.sumOf { it.quantityMolded }

            DailyTrendPoint(
                dayLabel = label,
                revenue = dayRevenue,
                expense = dayExpense,
                outputBlocks = dayOutput,
                materialCost = dayMaterialCost,
                laborCost = dayLaborTotal,
                netProfit = dayRevenue - dayExpense
            )
        }

        // Product Production share
        val productShareMap = filteredBatches.groupBy { it.blockProductName }
        val totalBatchBlocks = filteredBatches.sumOf { it.quantityMolded }
        val productShares = productShareMap.map { (name, bList) ->
            val count = bList.sumOf { it.quantityMolded }
            val pct = if (totalBatchBlocks > 0) (count.toFloat() / totalBatchBlocks) * 100f else 0f
            ProductProductionShare(name, count, pct)
        }.sortedByDescending { it.quantity }

        return FinancialDashboardMetrics(
            totalRevenue = totalRevenue,
            invoicedAmount = invoicedAmount,
            totalExpenses = totalExpenses,
            rawMaterialCostTotal = rawMaterialCostTotal,
            directLaborCostTotal = directLaborCostTotal,
            otherExpensesTotal = otherExpensesTotal,
            netProfit = netProfit,
            profitMarginPct = profitMargin,
            totalBlocksMolded = totalMolded,
            totalBlocksSold = totalSold,
            totalDefectBlocks = totalDefects,
            defectRatePct = defectRate,
            totalReceivables = totalReceivables,
            totalInventoryValue = totalInventoryValue,
            dailyTrends = dailyTrends,
            productShares = productShares,
            lowStockAlertsCount = lowStockAlerts
        )
    }
}

fun calculateDailyReport(
    orders: List<SalesOrderEntity>,
    batches: List<ProductionBatchEntity>,
    txns: List<PaymentTransactionEntity>,
    materials: List<RawMaterialEntity>,
    products: List<BlockProductEntity>,
    nowMillis: Long = System.currentTimeMillis()
): DailyReportState {
    val cal = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayStart = cal.timeInMillis
    val todayEnd = todayStart + (24L * 60 * 60 * 1000) - 1

    val dayFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US)
    val formattedDate = dayFormat.format(Date(nowMillis))
    val ethiopianDate = com.example.util.EthiopianDateHelper.formatEthiopianDate(nowMillis)

    val todayOrders = orders.filter { it.orderDateMillis in todayStart..todayEnd }
    val todayBatches = batches.filter { it.dateMillis in todayStart..todayEnd }
    val todayTxns = txns.filter { it.dateMillis in todayStart..todayEnd }

    // Revenue calculations: cash/transfer actually collected today in ETB
    val txnsSum = todayTxns.sumOf { it.amount }
    val ordersPaidSum = todayOrders.sumOf { it.paidAmount }
    val todayRevenue = if (txnsSum > 0.0) txnsSum else ordersPaidSum

    val todayInvoicedSales = todayOrders.sumOf { it.totalAmount }
    val todayPendingCredit = todayOrders.sumOf { it.balanceAmount }
    val todaySalesCount = todayOrders.size
    val todayBlocksSold = todayOrders.sumOf { it.quantity }
    val averageOrderValue = if (todaySalesCount > 0) todayInvoicedSales / todaySalesCount else 0.0

    // Payment methods breakdown for today
    val pMethods = mutableMapOf<String, Double>()
    if (todayTxns.isNotEmpty()) {
        for (t in todayTxns) {
            val key = when (t.paymentMethod.uppercase()) {
                "TELEBIRR" -> "Telebirr"
                "CBE_BIRR" -> "CBE Birr"
                "BANK_TRANSFER" -> "Bank Transfer"
                "CASH" -> "Cash"
                else -> t.paymentMethod
            }
            pMethods[key] = (pMethods[key] ?: 0.0) + t.amount
        }
    } else {
        for (o in todayOrders) {
            if (o.paidAmount > 0) {
                pMethods["Direct Payment"] = (pMethods["Direct Payment"] ?: 0.0) + o.paidAmount
            }
        }
    }

    // Top selling block product today
    val topSellingBlockName = todayOrders.groupBy { it.blockProductName }
        .maxByOrNull { entry -> entry.value.sumOf { it.quantity } }
        ?.key ?: if (todayOrders.isNotEmpty()) todayOrders.first().blockProductName else "None"

    // Production Output calculations for today
    val todayProductionOutput = todayBatches.sumOf { it.quantityMolded }
    val todayDefectCount = todayBatches.sumOf { it.defectCount }
    val todayNetUsableBlocks = (todayProductionOutput - todayDefectCount).coerceAtLeast(0)
    val todayDefectRatePct = if (todayProductionOutput > 0) {
        (todayDefectCount.toDouble() / todayProductionOutput) * 100.0
    } else 0.0
    val todayBatchesCount = todayBatches.size

    // Block type distribution (ensuring standard types 10, 15, 20 are present)
    val outputMap = linkedMapOf<String, Int>(
        "Blocket 10" to 0,
        "Blocket 15" to 0,
        "Blocket 20" to 0
    )
    for (b in todayBatches) {
        val name = when {
            b.blockProductName.contains("10") -> "Blocket 10"
            b.blockProductName.contains("15") -> "Blocket 15"
            b.blockProductName.contains("20") -> "Blocket 20"
            else -> b.blockProductName
        }
        outputMap[name] = (outputMap[name] ?: 0) + b.quantityMolded
    }

    val todayLaborCost = todayBatches.sumOf { it.laborCost }

    val cementCost = materials.find { it.name.contains("Cement", ignoreCase = true) }?.costPerUnit ?: 1650.0
    val sandCost = materials.find { it.name.contains("Sand", ignoreCase = true) }?.costPerUnit ?: 1100.0
    val gravelCost = materials.find { it.name.contains("Gravel", ignoreCase = true) || it.name.contains("Aggregate", ignoreCase = true) }?.costPerUnit ?: 1250.0

    val todayEstimatedMaterialCost = todayBatches.sumOf { b ->
        (b.cementBagsUsed * cementCost) + (b.sandUsedM3 * sandCost) + (b.aggregateUsedM3 * gravelCost)
    }

    return DailyReportState(
        dateMillis = nowMillis,
        formattedDate = formattedDate,
        ethiopianDate = ethiopianDate,
        todayRevenue = todayRevenue,
        todayInvoicedSales = todayInvoicedSales,
        todayPendingCredit = todayPendingCredit,
        todaySalesCount = todaySalesCount,
        todayBlocksSold = todayBlocksSold,
        averageOrderValue = averageOrderValue,
        paymentMethodsBreakdown = pMethods,
        topSellingBlockName = topSellingBlockName,
        todaySalesOrders = todayOrders.sortedByDescending { it.orderDateMillis },
        todayProductionOutput = todayProductionOutput,
        todayDefectCount = todayDefectCount,
        todayNetUsableBlocks = todayNetUsableBlocks,
        todayDefectRatePct = todayDefectRatePct,
        todayBatchesCount = todayBatchesCount,
        outputByBlockType = outputMap,
        todayLaborCost = todayLaborCost,
        todayEstimatedMaterialCost = todayEstimatedMaterialCost,
        todayProductionBatches = todayBatches.sortedByDescending { it.dateMillis }
    )
}

class BlocketViewModelFactory(private val repository: BlocketRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlocketViewModel::class.java)) {
            return BlocketViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
