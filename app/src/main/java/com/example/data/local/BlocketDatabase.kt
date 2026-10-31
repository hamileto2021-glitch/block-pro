package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AttendanceEntity
import com.example.data.model.BrickBatchTrackingEntity
import com.example.data.model.BrickInventoryEntity
import com.example.data.model.Employee
import com.example.data.model.Expense
import com.example.data.model.PaymentTransactionEntity
import com.example.data.model.Product
import com.example.data.model.ProductionBatchEntity
import com.example.data.model.RawMaterialEntity
import com.example.data.model.Sale
import com.example.data.model.WagePaymentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Local Room database structure for Blocket Production Management.
 * Entities: Product, Employee, Sale, Expense, BrickInventoryEntity, BrickBatchTrackingEntity, etc.
 */
@Database(
    entities = [
        Product::class,
        Employee::class,
        Sale::class,
        Expense::class,
        RawMaterialEntity::class,
        ProductionBatchEntity::class,
        AttendanceEntity::class,
        WagePaymentEntity::class,
        PaymentTransactionEntity::class,
        BrickInventoryEntity::class,
        BrickBatchTrackingEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BlocketDatabase : RoomDatabase() {

    // Dedicated entity DAOs as requested
    abstract fun productDao(): ProductDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun saleDao(): SaleDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun brickInventoryDao(): BrickInventoryDao

    // Unified operational DAO
    abstract fun blocketDao(): BlocketDao

    companion object {
        @Volatile
        private var INSTANCE: BlocketDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BlocketDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BlocketDatabase::class.java,
                    "blocket_production_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(BlocketDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class BlocketDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    DatabaseInitializer.seedInitialData(
                        database.blocketDao(),
                        database.brickInventoryDao()
                    )
                }
            }
        }
    }
}
