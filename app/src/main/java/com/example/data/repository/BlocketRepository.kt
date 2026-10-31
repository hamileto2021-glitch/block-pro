package com.example.data.repository

import com.example.data.local.BlocketDao
import com.example.data.model.AttendanceEntity
import com.example.data.model.BlockProductEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.PaymentTransactionEntity
import com.example.data.model.ProductionBatchEntity
import com.example.data.model.RawMaterialEntity
import com.example.data.model.SalesOrderEntity
import com.example.data.model.WagePaymentEntity
import kotlinx.coroutines.flow.Flow
import kotlin.math.max

class BlocketRepository(private val dao: BlocketDao) {

    val rawMaterials: Flow<List<RawMaterialEntity>> = dao.getAllRawMaterialsFlow()
    val blockProducts: Flow<List<BlockProductEntity>> = dao.getAllBlockProductsFlow()
    val productionBatches: Flow<List<ProductionBatchEntity>> = dao.getAllProductionBatchesFlow()
    val employees: Flow<List<EmployeeEntity>> = dao.getAllEmployeesFlow()
    val activeEmployees: Flow<List<EmployeeEntity>> = dao.getActiveEmployeesFlow()
    val attendanceRecords: Flow<List<AttendanceEntity>> = dao.getAllAttendanceFlow()
    val wagePayments: Flow<List<WagePaymentEntity>> = dao.getAllWagePaymentsFlow()
    val salesOrders: Flow<List<SalesOrderEntity>> = dao.getAllSalesOrdersFlow()
    val paymentTransactions: Flow<List<PaymentTransactionEntity>> = dao.getAllPaymentTransactionsFlow()
    val expenses: Flow<List<com.example.data.model.Expense>> = dao.getAllExpensesFlow()

    fun getAttendanceForDay(dayStartMillis: Long): Flow<List<AttendanceEntity>> =
        dao.getAttendanceForDayFlow(dayStartMillis)

    suspend fun logProductionBatch(
        batchCode: String,
        productId: Long,
        quantityMolded: Int,
        defectCount: Int,
        cementBagsUsed: Double,
        sandUsedM3: Double,
        aggregateUsedM3: Double,
        waterLitresUsed: Double,
        laborCost: Double,
        supervisorName: String,
        notes: String
    ) {
        val product = dao.getBlockProductById(productId) ?: return
        val netAdded = max(0, quantityMolded - defectCount)

        // 1. Insert Production Batch
        val batch = ProductionBatchEntity(
            batchCode = batchCode,
            dateMillis = System.currentTimeMillis(),
            blockProductId = productId,
            blockProductName = product.name,
            quantityMolded = quantityMolded,
            defectCount = defectCount,
            cementBagsUsed = cementBagsUsed,
            sandUsedM3 = sandUsedM3,
            aggregateUsedM3 = aggregateUsedM3,
            waterLitresUsed = waterLitresUsed,
            laborCost = laborCost,
            supervisorName = supervisorName,
            notes = notes
        )
        dao.insertProductionBatch(batch)

        // 2. Update Finished Product Stock
        dao.updateBlockProduct(
            product.copy(
                currentStock = product.currentStock + netAdded,
                brokenCount = product.brokenCount + defectCount
            )
        )

        // 3. Deduct Raw Materials
        if (cementBagsUsed > 0) {
            dao.getRawMaterialByName("%Cement%")?.let { mat ->
                dao.updateRawMaterial(mat.copy(currentStock = max(0.0, mat.currentStock - cementBagsUsed)))
            }
        }
        if (sandUsedM3 > 0) {
            dao.getRawMaterialByName("%Sand%")?.let { mat ->
                dao.updateRawMaterial(mat.copy(currentStock = max(0.0, mat.currentStock - sandUsedM3)))
            }
        }
        if (aggregateUsedM3 > 0) {
            dao.getRawMaterialByName("%Gravel%")?.let { mat ->
                dao.updateRawMaterial(mat.copy(currentStock = max(0.0, mat.currentStock - aggregateUsedM3)))
            } ?: dao.getRawMaterialByName("%Aggregate%")?.let { mat ->
                dao.updateRawMaterial(mat.copy(currentStock = max(0.0, mat.currentStock - aggregateUsedM3)))
            }
        }
        if (waterLitresUsed > 0) {
            dao.getRawMaterialByName("%Water%")?.let { mat ->
                dao.updateRawMaterial(mat.copy(currentStock = max(0.0, mat.currentStock - waterLitresUsed)))
            }
        }
    }

    suspend fun restockRawMaterial(
        materialId: Long,
        addedQuantity: Double,
        unitCost: Double
    ) {
        val mat = dao.getRawMaterialById(materialId) ?: return
        dao.updateRawMaterial(
            mat.copy(
                currentStock = mat.currentStock + addedQuantity,
                costPerUnit = if (unitCost > 0) unitCost else mat.costPerUnit,
                lastUpdatedDate = System.currentTimeMillis()
            )
        )
    }

    suspend fun createSalesOrder(
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        productId: Long,
        quantity: Int,
        unitPrice: Double,
        deliveryFee: Double,
        initialPaymentAmount: Double,
        paymentMethod: String,
        notes: String
    ) {
        val product = dao.getBlockProductById(productId) ?: return
        val totalAmount = (quantity * unitPrice) + deliveryFee
        val paidAmount = initialPaymentAmount.coerceAtMost(totalAmount)
        val balanceAmount = max(0.0, totalAmount - paidAmount)
        val paymentStatus = when {
            balanceAmount <= 0.0 -> "PAID"
            paidAmount > 0.0 -> "PARTIAL"
            else -> "UNPAID"
        }
        val orderNo = "ORD-${System.currentTimeMillis().toString().takeLast(5)}"

        val order = SalesOrderEntity(
            orderNumber = orderNo,
            customerName = customerName,
            customerPhone = customerPhone,
            deliveryAddress = deliveryAddress,
            orderDateMillis = System.currentTimeMillis(),
            blockProductId = productId,
            blockProductName = product.name,
            quantity = quantity,
            unitPrice = unitPrice,
            deliveryFee = deliveryFee,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            balanceAmount = balanceAmount,
            paymentStatus = paymentStatus,
            deliveryStatus = "PENDING",
            notes = notes
        )
        val orderId = dao.insertSalesOrder(order)

        // Deduct finished block stock
        dao.updateBlockProduct(
            product.copy(currentStock = max(0, product.currentStock - quantity))
        )

        // If down payment made, record transaction
        if (paidAmount > 0.0) {
            val txn = PaymentTransactionEntity(
                orderId = orderId,
                orderNumber = orderNo,
                customerName = customerName,
                dateMillis = System.currentTimeMillis(),
                amount = paidAmount,
                paymentMethod = paymentMethod,
                referenceNo = "TXN-${System.currentTimeMillis().toString().takeLast(6)}",
                notes = "Initial payment upon order creation"
            )
            dao.insertPaymentTransaction(txn)
        }
    }

    suspend fun recordPayment(
        orderId: Long,
        amount: Double,
        paymentMethod: String,
        referenceNo: String,
        notes: String
    ) {
        val order = dao.getSalesOrderById(orderId) ?: return
        val newPaid = (order.paidAmount + amount).coerceAtMost(order.totalAmount)
        val newBalance = max(0.0, order.totalAmount - newPaid)
        val status = if (newBalance <= 0.001) "PAID" else "PARTIAL"

        dao.updateSalesOrder(
            order.copy(
                paidAmount = newPaid,
                balanceAmount = newBalance,
                paymentStatus = status
            )
        )

        val txn = PaymentTransactionEntity(
            orderId = orderId,
            orderNumber = order.orderNumber,
            customerName = order.customerName,
            dateMillis = System.currentTimeMillis(),
            amount = amount,
            paymentMethod = paymentMethod,
            referenceNo = referenceNo.ifEmpty { "TXN-${System.currentTimeMillis().toString().takeLast(6)}" },
            notes = notes
        )
        dao.insertPaymentTransaction(txn)
    }

    suspend fun updateDeliveryStatus(orderId: Long, newStatus: String) {
        val order = dao.getSalesOrderById(orderId) ?: return
        dao.updateSalesOrder(order.copy(deliveryStatus = newStatus))
    }

    suspend fun saveAttendance(
        employeeId: Long,
        status: String,
        overtimeHours: Double,
        notes: String,
        dayStartMillis: Long
    ) {
        val emp = dao.getEmployeeById(employeeId) ?: return
        val wage = when (emp.wageType) {
            "DAILY" -> {
                when (status) {
                    "PRESENT" -> emp.rateAmount + (overtimeHours * (emp.rateAmount / 8.0 * 1.5))
                    "HALF_DAY" -> (emp.rateAmount / 2.0) + (overtimeHours * (emp.rateAmount / 8.0 * 1.5))
                    "OVERTIME" -> emp.rateAmount + (overtimeHours * (emp.rateAmount / 8.0 * 1.5))
                    else -> 0.0
                }
            }
            "MONTHLY" -> {
                // monthly daily equivalent
                val dailyEquiv = emp.rateAmount / 26.0
                if (status == "PRESENT" || status == "HALF_DAY") dailyEquiv else 0.0
            }
            "PIECE_RATE" -> {
                // piece-rate is tied to batch production; base attendance placeholder
                0.0
            }
            else -> 0.0
        }

        val record = AttendanceEntity(
            employeeId = employeeId,
            employeeName = emp.fullName,
            dateMillis = dayStartMillis,
            status = status,
            overtimeHours = overtimeHours,
            wageEarned = wage,
            notes = notes
        )
        dao.insertAttendance(record)
    }

    suspend fun recordWagePayment(
        employeeId: Long,
        amount: Double,
        paymentType: String,
        paymentMethod: String,
        notes: String
    ) {
        val emp = dao.getEmployeeById(employeeId) ?: return
        val wagePayment = WagePaymentEntity(
            employeeId = employeeId,
            employeeName = emp.fullName,
            dateMillis = System.currentTimeMillis(),
            amount = amount,
            paymentType = paymentType,
            paymentMethod = paymentMethod,
            notes = notes
        )
        dao.insertWagePayment(wagePayment)

        // Persist to Expense table
        val expense = com.example.data.model.Expense(
            title = "Wage Payout: ${emp.fullName}",
            category = "WAGE_PAYMENT",
            amount = amount,
            dateMillis = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            recipient = emp.fullName,
            notes = "${paymentType.replace('_', ' ')} - $notes"
        )
        dao.insertExpense(expense)
    }

    suspend fun recordExpense(
        title: String,
        category: String,
        amount: Double,
        paymentMethod: String,
        recipient: String,
        notes: String
    ): Long {
        val expense = com.example.data.model.Expense(
            title = title,
            category = category,
            amount = amount,
            dateMillis = System.currentTimeMillis(),
            paymentMethod = paymentMethod,
            recipient = recipient,
            notes = notes
        )
        return dao.insertExpense(expense)
    }

    suspend fun addEmployee(
        fullName: String,
        phone: String,
        role: String,
        wageType: String,
        rateAmount: Double
    ) {
        val employee = EmployeeEntity(
            fullName = fullName,
            phone = phone,
            role = role,
            wageType = wageType,
            rateAmount = rateAmount,
            status = "ACTIVE",
            dateJoinedMillis = System.currentTimeMillis()
        )
        dao.insertEmployee(employee)
    }

    suspend fun updateProductCostAndPrice(
        productId: Long,
        customCost: Double,
        customPrice: Double
    ) {
        val product = dao.getBlockProductById(productId) ?: return
        dao.updateBlockProduct(
            product.copy(
                unitCost = customCost,
                unitPrice = customPrice
            )
        )
    }

    suspend fun addNewBlockProduct(
        name: String,
        sizeSpec: String,
        initialStock: Int,
        unitCost: Double,
        unitPrice: Double,
        minStockThreshold: Int
    ) {
        val product = BlockProductEntity(
            name = name,
            sizeSpec = sizeSpec,
            currentStock = initialStock,
            unitCost = unitCost,
            unitPrice = unitPrice,
            brokenCount = 0,
            minStockThreshold = minStockThreshold
        )
        dao.insertBlockProduct(product)
    }
}
