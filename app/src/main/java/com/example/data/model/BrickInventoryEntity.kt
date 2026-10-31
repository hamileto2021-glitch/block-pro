package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database entity for managing brick production inventory.
 * Tracks current stock levels, reserved stock, damaged bricks, and pricing in Ethiopian Birr (ETB)
 * for different brick types (Blocket 10, Blocket 15, Blocket 20, Solid Brick, etc.).
 */
@Entity(
    tableName = "brick_inventory",
    indices = [Index(value = ["brickType"], unique = true)]
)
data class BrickInventoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val brickType: String,             // e.g. "Blocket 10", "Blocket 15", "Blocket 20", "Solid Concrete Brick"
    val dimensions: String,            // e.g. "40 x 20 x 10 cm", "40 x 20 x 15 cm", "40 x 20 x 20 cm"
    val currentStockLevel: Int,        // Available ready-to-sell inventory
    val reservedStockLevel: Int = 0,   // Reserved for pending customer orders
    val damagedStockLevel: Int = 0,    // Broken or rejected blocks
    val minimumStockThreshold: Int = 500, // Reorder / low-stock warning threshold
    val unitProductionCost: Double,    // Custom cost per unit in ETB
    val unitSellingPrice: Double,      // Custom selling price per unit in ETB
    val storageYardLocation: String = "Main Yard", // Plant yard sector
    val lastBatchDateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val totalPhysicalStock: Int get() = currentStockLevel + reservedStockLevel + damagedStockLevel
    val isLowStock: Boolean get() = currentStockLevel <= minimumStockThreshold
}

/**
 * Room database entity for batch tracking across different types of bricks.
 * Records batch codes, molded quantity, defects, raw material consumption,
 * curing status, and shift supervisor.
 */
@Entity(
    tableName = "brick_batch_tracking",
    indices = [
        Index(value = ["batchNumber"], unique = true),
        Index(value = ["brickTypeId"]),
        Index(value = ["productionDateMillis"])
    ]
)
data class BrickBatchTrackingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val batchNumber: String,           // e.g. "BATCH-BLK15-2026-001"
    val brickTypeId: Long,             // Foreign reference to BrickInventoryEntity.id
    val brickTypeName: String,         // e.g. "Blocket 15"
    val productionDateMillis: Long,    // Date & timestamp of batch production
    val quantityProduced: Int,         // Quantity molded from concrete mix
    val quantityAccepted: Int,         // Good blocks accepted after curing
    val defectCount: Int = 0,          // Cracks, voids, or broken blocks
    val cementBagsUsed: Double = 0.0,  // Portland Cement bags (e.g. Dangote/Derba 42.5R)
    val sandUsedM3: Double = 0.0,      // River sand volume in m³
    val aggregateUsedM3: Double = 0.0, // Crushed basalt aggregate (01/02) in m³
    val waterLitersUsed: Double = 0.0, // Water volume in liters
    val totalBatchCostEtb: Double = 0.0, // Combined material & labor cost in ETB
    val curingStatus: String = "CURING", // "CURING", "READY", "DISPATCHED"
    val curingDaysRequired: Int = 7,   // Standard concrete curing duration in days
    val curingCompletionDateMillis: Long = 0L,
    val supervisorOrShift: String = "", // e.g. "Abebe Kebede (Morning Shift)"
    val machineOrLineNumber: String = "Vibro-Press Line 1",
    val qualityGrade: String = "GRADE_A", // "GRADE_A", "STANDARD", "REJECTED"
    val notes: String = ""
)
