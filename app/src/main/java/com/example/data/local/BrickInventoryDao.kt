package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.BrickBatchTrackingEntity
import com.example.data.model.BrickInventoryEntity
import kotlinx.coroutines.flow.Flow
import kotlin.math.max

/**
 * Data Access Object for managing brick production inventory and batch tracking.
 * Supports real-time stock levels, low-stock alerts, batch lifecycle tracking,
 * and atomic stock adjustment transactions.
 */
@Dao
interface BrickInventoryDao {

    // ==========================================
    // 1. Brick Inventory Operations (Stock Levels)
    // ==========================================

    @Query("SELECT * FROM brick_inventory ORDER BY brickType ASC")
    fun getAllBrickInventoryFlow(): Flow<List<BrickInventoryEntity>>

    @Query("SELECT * FROM brick_inventory WHERE id = :id LIMIT 1")
    suspend fun getBrickInventoryById(id: Long): BrickInventoryEntity?

    @Query("SELECT * FROM brick_inventory WHERE brickType = :brickType LIMIT 1")
    suspend fun getBrickInventoryByType(brickType: String): BrickInventoryEntity?

    @Query("SELECT * FROM brick_inventory WHERE brickType = :brickType LIMIT 1")
    fun getBrickInventoryByTypeFlow(brickType: String): Flow<BrickInventoryEntity?>

    @Query("SELECT * FROM brick_inventory WHERE currentStockLevel <= minimumStockThreshold ORDER BY currentStockLevel ASC")
    fun getLowStockBricksFlow(): Flow<List<BrickInventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrickInventory(inventory: BrickInventoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrickInventories(inventories: List<BrickInventoryEntity>)

    @Update
    suspend fun updateBrickInventory(inventory: BrickInventoryEntity)

    @Query("UPDATE brick_inventory SET currentStockLevel = MAX(0, currentStockLevel + :deltaQuantity), lastBatchDateMillis = :timestamp WHERE id = :brickId")
    suspend fun updateStockLevel(brickId: Long, deltaQuantity: Int, timestamp: Long)

    @Query("UPDATE brick_inventory SET unitProductionCost = :newCost, unitSellingPrice = :newPrice WHERE id = :brickId")
    suspend fun updateCostAndPrice(brickId: Long, newCost: Double, newPrice: Double)

    @Query("UPDATE brick_inventory SET damagedStockLevel = damagedStockLevel + :damagedDelta WHERE id = :brickId")
    suspend fun recordDamagedStock(brickId: Long, damagedDelta: Int)

    @Delete
    suspend fun deleteBrickInventory(inventory: BrickInventoryEntity)

    @Query("SELECT SUM(currentStockLevel) FROM brick_inventory")
    fun getTotalReadyStockFlow(): Flow<Int?>

    // ==========================================
    // 2. Batch Tracking Operations
    // ==========================================

    @Query("SELECT * FROM brick_batch_tracking ORDER BY productionDateMillis DESC, id DESC")
    fun getAllBatchesFlow(): Flow<List<BrickBatchTrackingEntity>>

    @Query("SELECT * FROM brick_batch_tracking WHERE brickTypeId = :brickTypeId ORDER BY productionDateMillis DESC")
    fun getBatchesForBrickTypeFlow(brickTypeId: Long): Flow<List<BrickBatchTrackingEntity>>

    @Query("SELECT * FROM brick_batch_tracking WHERE curingStatus = :curingStatus ORDER BY productionDateMillis DESC")
    fun getBatchesByCuringStatusFlow(curingStatus: String): Flow<List<BrickBatchTrackingEntity>>

    @Query("SELECT * FROM brick_batch_tracking WHERE id = :id LIMIT 1")
    suspend fun getBatchById(id: Long): BrickBatchTrackingEntity?

    @Query("SELECT * FROM brick_batch_tracking WHERE batchNumber = :batchNumber LIMIT 1")
    suspend fun getBatchByNumber(batchNumber: String): BrickBatchTrackingEntity?

    @Query("SELECT * FROM brick_batch_tracking WHERE productionDateMillis >= :startMillis AND productionDateMillis <= :endMillis ORDER BY productionDateMillis DESC")
    fun getBatchesBetweenDatesFlow(startMillis: Long, endMillis: Long): Flow<List<BrickBatchTrackingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BrickBatchTrackingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<BrickBatchTrackingEntity>)

    @Update
    suspend fun updateBatch(batch: BrickBatchTrackingEntity)

    @Query("UPDATE brick_batch_tracking SET curingStatus = :newStatus WHERE id = :batchId")
    suspend fun updateBatchCuringStatus(batchId: Long, newStatus: String)

    @Delete
    suspend fun deleteBatch(batch: BrickBatchTrackingEntity)

    @Query("SELECT SUM(quantityProduced) FROM brick_batch_tracking WHERE brickTypeId = :brickTypeId")
    fun getTotalProducedForBrickTypeFlow(brickTypeId: Long): Flow<Int?>

    @Query("SELECT SUM(defectCount) FROM brick_batch_tracking WHERE brickTypeId = :brickTypeId")
    fun getTotalDefectsForBrickTypeFlow(brickTypeId: Long): Flow<Int?>

    // ==========================================
    // 3. Atomic Composite Transactions
    // ==========================================

    /**
     * Atomically records a production batch and updates the brick inventory stock level
     * by the accepted quantity (produced minus defects). Also records damaged count.
     */
    @Transaction
    suspend fun recordBatchAndIncrementStock(batch: BrickBatchTrackingEntity): Long {
        val batchId = insertBatch(batch)
        val netAccepted = max(0, batch.quantityProduced - batch.defectCount)

        val inventory = getBrickInventoryById(batch.brickTypeId)
        if (inventory != null) {
            val updated = inventory.copy(
                currentStockLevel = inventory.currentStockLevel + netAccepted,
                damagedStockLevel = inventory.damagedStockLevel + batch.defectCount,
                lastBatchDateMillis = batch.productionDateMillis
            )
            updateBrickInventory(updated)
        }
        return batchId
    }

    /**
     * Atomically dispatches stock when sales or deliveries occur.
     */
    @Transaction
    suspend fun dispatchStock(brickId: Long, quantity: Int): Boolean {
        val inventory = getBrickInventoryById(brickId) ?: return false
        if (inventory.currentStockLevel < quantity) return false
        val updated = inventory.copy(
            currentStockLevel = inventory.currentStockLevel - quantity
        )
        updateBrickInventory(updated)
        return true
    }
}
