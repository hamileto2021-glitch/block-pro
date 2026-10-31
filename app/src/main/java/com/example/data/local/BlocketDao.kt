package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceEntity
import com.example.data.model.Employee
import com.example.data.model.Expense
import com.example.data.model.PaymentTransactionEntity
import com.example.data.model.Product
import com.example.data.model.ProductionBatchEntity
import com.example.data.model.RawMaterialEntity
import com.example.data.model.Sale
import com.example.data.model.WagePaymentEntity
import kotlinx.coroutines.flow.Flow

// 1. Dedicated Product DAO
@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)
}

// 2. Dedicated Employee DAO
@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY fullName ASC")
    fun getAllEmployeesFlow(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE status = 'ACTIVE' ORDER BY fullName ASC")
    fun getActiveEmployeesFlow(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: Long): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployees(employees: List<Employee>)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee: Employee)
}

// 3. Dedicated Sale DAO
@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY orderDateMillis DESC, id DESC")
    fun getAllSalesFlow(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE balanceAmount > 0 ORDER BY orderDateMillis DESC")
    fun getUnpaidSalesFlow(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSaleById(id: Long): Sale?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<Sale>)

    @Update
    suspend fun updateSale(sale: Sale)

    @Delete
    suspend fun deleteSale(sale: Sale)
}

// 4. Dedicated Expense DAO
@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC, id DESC")
    fun getAllExpensesFlow(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getExpensesBetweenDatesFlow(startMillis: Long, endMillis: Long): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}

// Unified DAO interface for overall Blocket Plant Management
@Dao
interface BlocketDao : ProductDao, EmployeeDao, SaleDao, ExpenseDao {

    // Aliases to support existing repository queries seamlessly
    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllBlockProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getBlockProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockProducts(products: List<Product>)

    @Update
    suspend fun updateBlockProduct(product: Product)

    @Query("SELECT * FROM sales ORDER BY orderDateMillis DESC, id DESC")
    fun getAllSalesOrdersFlow(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE balanceAmount > 0 ORDER BY orderDateMillis DESC")
    fun getPendingCreditOrdersFlow(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id LIMIT 1")
    suspend fun getSalesOrderById(id: Long): Sale?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesOrder(order: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalesOrders(orders: List<Sale>)

    @Update
    suspend fun updateSalesOrder(order: Sale)

    // --- RAW MATERIALS ---
    @Query("SELECT * FROM raw_materials ORDER BY name ASC")
    fun getAllRawMaterialsFlow(): Flow<List<RawMaterialEntity>>

    @Query("SELECT * FROM raw_materials WHERE id = :id LIMIT 1")
    suspend fun getRawMaterialById(id: Long): RawMaterialEntity?

    @Query("SELECT * FROM raw_materials WHERE name LIKE :namePrefix LIMIT 1")
    suspend fun getRawMaterialByName(namePrefix: String): RawMaterialEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRawMaterial(material: RawMaterialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRawMaterials(materials: List<RawMaterialEntity>)

    @Update
    suspend fun updateRawMaterial(material: RawMaterialEntity)

    // --- PRODUCTION BATCHES ---
    @Query("SELECT * FROM production_batches ORDER BY dateMillis DESC, id DESC")
    fun getAllProductionBatchesFlow(): Flow<List<ProductionBatchEntity>>

    @Query("SELECT * FROM production_batches WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getBatchesBetweenDatesFlow(startMillis: Long, endMillis: Long): Flow<List<ProductionBatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductionBatch(batch: ProductionBatchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductionBatches(batches: List<ProductionBatchEntity>)

    // --- ATTENDANCE ---
    @Query("SELECT * FROM attendance_records WHERE dateMillis = :dayStartMillis")
    fun getAttendanceForDayFlow(dayStartMillis: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records ORDER BY dateMillis DESC")
    fun getAllAttendanceFlow(): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<AttendanceEntity>)

    // --- WAGE PAYMENTS ---
    @Query("SELECT * FROM wage_payments ORDER BY dateMillis DESC, id DESC")
    fun getAllWagePaymentsFlow(): Flow<List<WagePaymentEntity>>

    @Query("SELECT * FROM wage_payments WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getWagePaymentsBetweenDatesFlow(startMillis: Long, endMillis: Long): Flow<List<WagePaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWagePayment(wagePayment: WagePaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWagePayments(wagePayments: List<WagePaymentEntity>)

    // --- PAYMENT TRANSACTIONS ---
    @Query("SELECT * FROM payment_transactions ORDER BY dateMillis DESC, id DESC")
    fun getAllPaymentTransactionsFlow(): Flow<List<PaymentTransactionEntity>>

    @Query("SELECT * FROM payment_transactions WHERE orderId = :orderId ORDER BY dateMillis DESC")
    fun getTransactionsForOrderFlow(orderId: Long): Flow<List<PaymentTransactionEntity>>

    @Query("SELECT * FROM payment_transactions WHERE dateMillis >= :startMillis AND dateMillis <= :endMillis ORDER BY dateMillis DESC")
    fun getTransactionsBetweenDatesFlow(startMillis: Long, endMillis: Long): Flow<List<PaymentTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentTransaction(transaction: PaymentTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentTransactions(transactions: List<PaymentTransactionEntity>)
}
