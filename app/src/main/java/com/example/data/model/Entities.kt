package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 1. Product Entity - Concrete Hollow Blocks, Pavers, Bricks, and precast masonry
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sizeSpec: String,
    val currentStock: Int,
    val unitCost: Double,
    val unitPrice: Double,
    val brokenCount: Int = 0,
    val minStockThreshold: Int = 500
)

/**
 * 2. Employee Entity - Plant operators, mixers, yard crew, drivers, and supervisors
 */
@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phone: String,
    val role: String,
    val wageType: String, // DAILY, MONTHLY, PIECE_RATE
    val rateAmount: Double,
    val status: String = "ACTIVE", // ACTIVE, ON_LEAVE, INACTIVE
    val dateJoinedMillis: Long
)

/**
 * 3. Sale Entity - Customer orders, delivered quantities, billing & balances
 */
@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val orderDateMillis: Long,
    val blockProductId: Long,
    val blockProductName: String,
    val quantity: Int,
    val unitPrice: Double,
    val deliveryFee: Double,
    val totalAmount: Double,
    val paidAmount: Double,
    val balanceAmount: Double,
    val paymentStatus: String, // PAID, PARTIAL, UNPAID
    val deliveryStatus: String, // PENDING, DISPATCHED, DELIVERED
    val notes: String = ""
) {
    val productId: Long get() = blockProductId
    val productName: String get() = blockProductName
}

/**
 * 4. Expense Entity - Raw material purchases, employee wages, batch labor, fuel & plant costs
 */
@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // WAGE_PAYMENT, RAW_MATERIAL, BATCH_LABOR, MAINTENANCE, UTILITY, OTHER
    val amount: Double,
    val dateMillis: Long,
    val paymentMethod: String = "CASH", // CASH, BANK_TRANSFER, MOBILE_MONEY
    val recipient: String = "",
    val notes: String = ""
)

// Backward compatibility typealiases
typealias BlockProductEntity = Product
typealias EmployeeEntity = Employee
typealias SalesOrderEntity = Sale
typealias ExpenseEntity = Expense

// Supporting auxiliary operational records for the factory
@Entity(tableName = "raw_materials")
data class RawMaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: String,
    val currentStock: Double,
    val minAlertThreshold: Double,
    val costPerUnit: Double,
    val lastUpdatedDate: Long
)

@Entity(tableName = "production_batches")
data class ProductionBatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchCode: String,
    val dateMillis: Long,
    val blockProductId: Long,
    val blockProductName: String,
    val quantityMolded: Int,
    val defectCount: Int,
    val cementBagsUsed: Double,
    val sandUsedM3: Double,
    val aggregateUsedM3: Double,
    val waterLitresUsed: Double = 0.0,
    val laborCost: Double,
    val supervisorName: String,
    val notes: String = ""
)

@Entity(tableName = "attendance_records")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val dateMillis: Long,
    val status: String, // PRESENT, HALF_DAY, ABSENT, OVERTIME
    val overtimeHours: Double = 0.0,
    val wageEarned: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "wage_payments")
data class WagePaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val employeeName: String,
    val dateMillis: Long,
    val amount: Double,
    val paymentType: String,
    val paymentMethod: String,
    val notes: String = ""
)

@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val orderNumber: String,
    val customerName: String,
    val dateMillis: Long,
    val amount: Double,
    val paymentMethod: String,
    val referenceNo: String = "",
    val notes: String = ""
)
