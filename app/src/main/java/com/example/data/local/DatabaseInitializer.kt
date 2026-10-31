package com.example.data.local

import com.example.data.model.AttendanceEntity
import com.example.data.model.BlockProductEntity
import com.example.data.model.BrickBatchTrackingEntity
import com.example.data.model.BrickInventoryEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.Expense
import com.example.data.model.PaymentTransactionEntity
import com.example.data.model.ProductionBatchEntity
import com.example.data.model.RawMaterialEntity
import com.example.data.model.SalesOrderEntity
import com.example.data.model.WagePaymentEntity
import java.util.Calendar

object DatabaseInitializer {

    suspend fun seedInitialData(dao: BlocketDao, brickDao: BrickInventoryDao? = null) {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24L * 60 * 60 * 1000

        // 1. Raw Materials in Ethiopian Market Units & ETB
        val materials = listOf(
            RawMaterialEntity(
                name = "Dangote / Derba Cement 42.5R (ሲሚንቶ)",
                unit = "Bags",
                currentStock = 280.0,
                minAlertThreshold = 60.0,
                costPerUnit = 1650.0, // ETB per bag
                lastUpdatedDate = now
            ),
            RawMaterialEntity(
                name = "Awash River Sand (የወንዝ አሸዋ)",
                unit = "m³",
                currentStock = 65.0,
                minAlertThreshold = 20.0,
                costPerUnit = 1100.0, // ETB per m³
                lastUpdatedDate = now
            ),
            RawMaterialEntity(
                name = "Basalt Crushed Aggregate 01/02 (ጠጠር)",
                unit = "m³",
                currentStock = 75.0,
                minAlertThreshold = 25.0,
                costPerUnit = 1250.0, // ETB per m³
                lastUpdatedDate = now
            ),
            RawMaterialEntity(
                name = "Water Tanker Delivery (የቦቴ ውሃ)",
                unit = "Liters",
                currentStock = 8000.0,
                minAlertThreshold = 2000.0,
                costPerUnit = 0.35, // ETB per liter
                lastUpdatedDate = now
            ),
            RawMaterialEntity(
                name = "Concrete Accelerator / Hardener (ኬሚካል)",
                unit = "Liters",
                currentStock = 120.0,
                minAlertThreshold = 30.0,
                costPerUnit = 280.0, // ETB per liter
                lastUpdatedDate = now
            ),
            RawMaterialEntity(
                name = "Heavy Duty Pallets (የብሎኬት ፓሌቶች)",
                unit = "Units",
                currentStock = 450.0,
                minAlertThreshold = 120.0,
                costPerUnit = 350.0, // ETB per pallet
                lastUpdatedDate = now
            )
        )
        dao.insertRawMaterials(materials)

        // 2. Finished Block Products: Blocket 10, 15, and 20 (Prices in ETB)
        val products = listOf(
            BlockProductEntity(
                name = "Blocket 10",
                sizeSpec = "40 x 20 x 10 cm (10ኛ ክፍልፋይ)",
                currentStock = 3800,
                unitCost = 26.0,  // ETB custom cost
                unitPrice = 38.0, // ETB custom price
                brokenCount = 24,
                minStockThreshold = 800
            ),
            BlockProductEntity(
                name = "Blocket 15",
                sizeSpec = "40 x 20 x 15 cm (15ኛ መደበኛ)",
                currentStock = 5600,
                unitCost = 38.0,  // ETB custom cost
                unitPrice = 54.0, // ETB custom price
                brokenCount = 35,
                minStockThreshold = 1200
            ),
            BlockProductEntity(
                name = "Blocket 20",
                sizeSpec = "40 x 20 x 20 cm (20ኛ መሰረት/ዋና)",
                currentStock = 3200,
                unitCost = 52.0,  // ETB custom cost
                unitPrice = 74.0, // ETB custom price
                brokenCount = 28,
                minStockThreshold = 900
            )
        )
        dao.insertBlockProducts(products)

        // 3. Ethiopian Plant Employees & Wage Rates (in ETB)
        val employees = listOf(
            EmployeeEntity(
                fullName = "Dawit Kebede",
                phone = "+251 91 123 4567",
                role = "የማሽን ኦፕሬተር (Machine Lead)",
                wageType = "DAILY",
                rateAmount = 450.0, // ETB per day
                status = "ACTIVE",
                dateJoinedMillis = now - 180 * oneDayMillis
            ),
            EmployeeEntity(
                fullName = "Alula Haile",
                phone = "+251 92 345 6789",
                role = "የማቀላቀያ ሰራተኛ (Batch Mixer)",
                wageType = "DAILY",
                rateAmount = 400.0, // ETB per day
                status = "ACTIVE",
                dateJoinedMillis = now - 120 * oneDayMillis
            ),
            EmployeeEntity(
                fullName = "Selamawit Tadesse",
                phone = "+251 93 456 7890",
                role = "የያርድ ተቆጣጣሪ (Yard Supervisor)",
                wageType = "MONTHLY",
                rateAmount = 16500.0, // ETB per month
                status = "ACTIVE",
                dateJoinedMillis = now - 240 * oneDayMillis
            ),
            EmployeeEntity(
                fullName = "Getachew Bekele",
                phone = "+251 94 567 8901",
                role = "ብሎኬት ደራቢ (Stacker & Palletizer)",
                wageType = "PIECE_RATE",
                rateAmount = 200.0, // ETB per 100 blocks
                status = "ACTIVE",
                dateJoinedMillis = now - 90 * oneDayMillis
            ),
            EmployeeEntity(
                fullName = "Biruk Yohannes",
                phone = "+251 95 678 9012",
                role = "ብሎኬት ደራቢ (Stacker & Palletizer)",
                wageType = "PIECE_RATE",
                rateAmount = 200.0, // ETB per 100 blocks
                status = "ACTIVE",
                dateJoinedMillis = now - 60 * oneDayMillis
            ),
            EmployeeEntity(
                fullName = "Ermias Tefera",
                phone = "+251 96 789 0123",
                role = "የሲኖትራክ / ፎርክሊፍት ሹፌር (Driver)",
                wageType = "DAILY",
                rateAmount = 450.0, // ETB per day
                status = "ACTIVE",
                dateJoinedMillis = now - 150 * oneDayMillis
            )
        )
        dao.insertEmployees(employees)

        // 4. Production Batches (Blocket 10, 15, and 20)
        val batches = listOf(
            ProductionBatchEntity(
                batchCode = "PRD-ETH-101",
                dateMillis = now - (0 * oneDayMillis),
                blockProductId = 2,
                blockProductName = "Blocket 15",
                quantityMolded = 1800,
                defectCount = 15,
                cementBagsUsed = 28.0,
                sandUsedM3 = 6.0,
                aggregateUsedM3 = 7.5,
                waterLitresUsed = 550.0,
                laborCost = 3600.0, // ETB direct batch labor
                supervisorName = "Selamawit Tadesse",
                notes = "Morning run of 15 cm standard blockets."
            ),
            ProductionBatchEntity(
                batchCode = "PRD-ETH-100",
                dateMillis = now - (0 * oneDayMillis) - 3 * 3600 * 1000,
                blockProductId = 1,
                blockProductName = "Blocket 10",
                quantityMolded = 2400,
                defectCount = 18,
                cementBagsUsed = 26.0,
                sandUsedM3 = 5.4,
                aggregateUsedM3 = 6.8,
                waterLitresUsed = 490.0,
                laborCost = 4800.0, // ETB
                supervisorName = "Selamawit Tadesse",
                notes = "10 cm partition blockets run."
            ),
            ProductionBatchEntity(
                batchCode = "PRD-ETH-099",
                dateMillis = now - (1 * oneDayMillis),
                blockProductId = 3,
                blockProductName = "Blocket 20",
                quantityMolded = 1200,
                defectCount = 12,
                cementBagsUsed = 25.0,
                sandUsedM3 = 5.5,
                aggregateUsedM3 = 7.0,
                waterLitresUsed = 520.0,
                laborCost = 2400.0, // ETB
                supervisorName = "Selamawit Tadesse",
                notes = "20 cm load-bearing foundation blockets."
            ),
            ProductionBatchEntity(
                batchCode = "PRD-ETH-098",
                dateMillis = now - (2 * oneDayMillis),
                blockProductId = 2,
                blockProductName = "Blocket 15",
                quantityMolded = 1600,
                defectCount = 14,
                cementBagsUsed = 27.0,
                sandUsedM3 = 5.8,
                aggregateUsedM3 = 7.2,
                waterLitresUsed = 510.0,
                laborCost = 3200.0, // ETB
                supervisorName = "Selamawit Tadesse",
                notes = "Continuous mix 15 cm blocks run."
            )
        )
        dao.insertProductionBatches(batches)

        // 5. Today's Attendance
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = cal.timeInMillis

        val attendances = listOf(
            AttendanceEntity(
                employeeId = 1,
                employeeName = "Dawit Kebede",
                dateMillis = todayStart,
                status = "PRESENT",
                overtimeHours = 1.0,
                wageEarned = 534.0,
                notes = "ማሽን ኦፕሬተር በመደበኛ ፈረቃ"
            ),
            AttendanceEntity(
                employeeId = 2,
                employeeName = "Alula Haile",
                dateMillis = todayStart,
                status = "PRESENT",
                overtimeHours = 0.0,
                wageEarned = 400.0,
                notes = "የማቀላቀያ ሰራተኛ"
            ),
            AttendanceEntity(
                employeeId = 3,
                employeeName = "Selamawit Tadesse",
                dateMillis = todayStart,
                status = "PRESENT",
                overtimeHours = 0.0,
                wageEarned = 635.0,
                notes = "የያርድና ጥራት ቁጥጥር"
            ),
            AttendanceEntity(
                employeeId = 4,
                employeeName = "Getachew Bekele",
                dateMillis = todayStart,
                status = "PRESENT",
                overtimeHours = 0.0,
                wageEarned = 840.0, // ETB for stacking
                notes = "4200 ብሎኬቶች ደርቧል"
            ),
            AttendanceEntity(
                employeeId = 5,
                employeeName = "Biruk Yohannes",
                dateMillis = todayStart,
                status = "PRESENT",
                overtimeHours = 0.0,
                wageEarned = 840.0,
                notes = "4200 ብሎኬቶች ደርቧል"
            ),
            AttendanceEntity(
                employeeId = 6,
                employeeName = "Ermias Tefera",
                dateMillis = todayStart,
                status = "PRESENT",
                overtimeHours = 2.0,
                wageEarned = 618.0,
                notes = "ወደ ቦሌ ቡልቡላና ሲኤምሲ አድርሷል"
            )
        )
        dao.insertAttendances(attendances)

        // 6. Recent Wage Payments (in ETB)
        val wagePayments = listOf(
            WagePaymentEntity(
                employeeId = 4,
                employeeName = "Getachew Bekele",
                dateMillis = now - (0 * oneDayMillis) - 2 * 3600 * 1000,
                amount = 840.0,
                paymentType = "PIECE_PAYMENT",
                paymentMethod = "TELEBIRR",
                notes = "የብሎኬት ድርደራ ክፍያ በቴሌብር"
            ),
            WagePaymentEntity(
                employeeId = 5,
                employeeName = "Biruk Yohannes",
                dateMillis = now - (0 * oneDayMillis) - 2 * 3600 * 1000,
                amount = 840.0,
                paymentType = "PIECE_PAYMENT",
                paymentMethod = "TELEBIRR",
                notes = "የብሎኬት ድርደራ ክፍያ በቴሌብር"
            ),
            WagePaymentEntity(
                employeeId = 1,
                employeeName = "Dawit Kebede",
                dateMillis = now - (1 * oneDayMillis),
                amount = 450.0,
                paymentType = "DAILY_WAGE",
                paymentMethod = "CBE_BIRR",
                notes = "የቀን ማሽን ኦፕሬተር ክፍያ"
            ),
            WagePaymentEntity(
                employeeId = 6,
                employeeName = "Ermias Tefera",
                dateMillis = now - (1 * oneDayMillis),
                amount = 450.0,
                paymentType = "DAILY_WAGE",
                paymentMethod = "CASH",
                notes = "የሹፌር የቀን አበል"
            )
        )
        dao.insertWagePayments(wagePayments)

        // 7. Ethiopian Sales Orders & Custom Unit Prices (ETB)
        val orders = listOf(
            SalesOrderEntity(
                orderNumber = "ORD-AA-401",
                customerName = "Sunshine Construction PLC",
                customerPhone = "+251 91 144 5566",
                deliveryAddress = "Bole Bulbula Site, Addis Ababa",
                orderDateMillis = now - 1 * 3600 * 1000,
                blockProductId = 2,
                blockProductName = "Blocket 15",
                quantity = 1500,
                unitPrice = 54.0, // custom price ETB
                deliveryFee = 2500.0, // ETB transport
                totalAmount = 83500.0,
                paidAmount = 50000.0,
                balanceAmount = 33500.0,
                paymentStatus = "PARTIAL",
                deliveryStatus = "DISPATCHED",
                notes = "15 cm standard blockets dispatched by Isuzu"
            ),
            SalesOrderEntity(
                orderNumber = "ORD-AA-400",
                customerName = "Flintstone Engineering",
                customerPhone = "+251 92 233 4455",
                deliveryAddress = "CMC Goha Real Estate Phase 3",
                orderDateMillis = now - (0 * oneDayMillis) - 4 * 3600 * 1000,
                blockProductId = 1,
                blockProductName = "Blocket 10",
                quantity = 2500,
                unitPrice = 38.0, // custom price ETB
                deliveryFee = 3000.0,
                totalAmount = 98000.0,
                paidAmount = 98000.0,
                balanceAmount = 0.0,
                paymentStatus = "PAID",
                deliveryStatus = "DELIVERED",
                notes = "Full settlement received via CBE Bank Transfer"
            ),
            SalesOrderEntity(
                orderNumber = "ORD-AA-399",
                customerName = "Afro-Tsion Construction",
                customerPhone = "+251 93 322 1100",
                deliveryAddress = "Ayat Zone 4 Commercial Center",
                orderDateMillis = now - (1 * oneDayMillis),
                blockProductId = 3,
                blockProductName = "Blocket 20",
                quantity = 1200,
                unitPrice = 74.0, // custom price ETB
                deliveryFee = 2800.0,
                totalAmount = 91600.0,
                paidAmount = 91600.0,
                balanceAmount = 0.0,
                paymentStatus = "PAID",
                deliveryStatus = "DELIVERED",
                notes = "Telebirr merchant clearance confirmed"
            ),
            SalesOrderEntity(
                orderNumber = "ORD-AA-398",
                customerName = "Ovid Construction",
                customerPhone = "+251 94 455 6677",
                deliveryAddress = "Akaki Kality Logistics Center",
                orderDateMillis = now - (2 * oneDayMillis),
                blockProductId = 2,
                blockProductName = "Blocket 15",
                quantity = 3000,
                unitPrice = 52.0, // negotiated volume price ETB
                deliveryFee = 4500.0,
                totalAmount = 160500.0,
                paidAmount = 100000.0,
                balanceAmount = 60500.0,
                paymentStatus = "PARTIAL",
                deliveryStatus = "DELIVERED",
                notes = "Advance paid via Awash Bank, remaining net 10 days"
            )
        )
        dao.insertSalesOrders(orders)

        // 8. Payment Transactions in ETB
        val transactions = listOf(
            PaymentTransactionEntity(
                orderId = 1,
                orderNumber = "ORD-AA-401",
                customerName = "Sunshine Construction PLC",
                dateMillis = now - 1 * 3600 * 1000,
                amount = 50000.0,
                paymentMethod = "CBE_BIRR",
                referenceNo = "CBE-TX-998412",
                notes = "Down payment via CBE Mobile Banking"
            ),
            PaymentTransactionEntity(
                orderId = 2,
                orderNumber = "ORD-AA-400",
                customerName = "Flintstone Engineering",
                dateMillis = now - (0 * oneDayMillis) - 4 * 3600 * 1000,
                amount = 98000.0,
                paymentMethod = "BANK_TRANSFER",
                referenceNo = "AWASH-WIRE-4412",
                notes = "Full wire clearance via Awash Bank"
            ),
            PaymentTransactionEntity(
                orderId = 3,
                orderNumber = "ORD-AA-399",
                customerName = "Afro-Tsion Construction",
                dateMillis = now - (1 * oneDayMillis),
                amount = 91600.0,
                paymentMethod = "TELEBIRR",
                referenceNo = "TB-88492019",
                notes = "Telebirr superapp merchant payment"
            ),
            PaymentTransactionEntity(
                orderId = 4,
                orderNumber = "ORD-AA-398",
                customerName = "Ovid Construction",
                dateMillis = now - (2 * oneDayMillis),
                amount = 100000.0,
                paymentMethod = "BANK_TRANSFER",
                referenceNo = "ABY-FT-77291",
                notes = "Deposit via Bank of Abyssinia"
            )
        )
        dao.insertPaymentTransactions(transactions)

        // 9. Initial Operating & Production Expenses (ETB)
        val expenses = listOf(
            Expense(
                title = "Dangote Cement Restock (60 Bags)",
                category = "RAW_MATERIAL",
                amount = 99000.0, // 60 * 1650 ETB
                dateMillis = now - (1 * oneDayMillis),
                paymentMethod = "CBE_BIRR",
                recipient = "National Cement Distributor Addis",
                notes = "Dangote 42.5R delivered to site"
            ),
            Expense(
                title = "Awash River Sand Delivery (Sino-truck 16 m³)",
                category = "RAW_MATERIAL",
                amount = 17600.0, // 16 * 1100 ETB
                dateMillis = now - (2 * oneDayMillis),
                paymentMethod = "TELEBIRR",
                recipient = "Meki Sand Transport",
                notes = "Clean coarse river sand"
            ),
            Expense(
                title = "Stacking & Molding Crew Daily Wage",
                category = "WAGE_PAYMENT",
                amount = 1680.0,
                dateMillis = now - 2 * 3600 * 1000,
                paymentMethod = "TELEBIRR",
                recipient = "Getachew Bekele & Biruk Yohannes",
                notes = "Piece-rate stacking payout"
            ),
            Expense(
                title = "Block Machine Hydraulic Oil & Greasing",
                category = "MAINTENANCE",
                amount = 4500.0,
                dateMillis = now - (2 * oneDayMillis),
                paymentMethod = "CASH",
                recipient = "Kality Machinery Spare Parts",
                notes = "Hydraulic pressure pump maintenance"
            )
        )
        dao.insertExpenses(expenses)

        // 10. Dedicated Brick Production Inventory & Batch Tracking
        brickDao?.let { bDao ->
            val brickInventories = listOf(
                BrickInventoryEntity(
                    id = 1,
                    brickType = "Blocket 10",
                    dimensions = "40 x 20 x 10 cm (10ኛ ክፍልፋይ)",
                    currentStockLevel = 3800,
                    reservedStockLevel = 400,
                    damagedStockLevel = 24,
                    minimumStockThreshold = 800,
                    unitProductionCost = 26.0,
                    unitSellingPrice = 38.0,
                    storageYardLocation = "Yard Sector 1A",
                    lastBatchDateMillis = now - (1 * oneDayMillis),
                    notes = "Used for non-load bearing interior partition walls"
                ),
                BrickInventoryEntity(
                    id = 2,
                    brickType = "Blocket 15",
                    dimensions = "40 x 20 x 15 cm (15ኛ መደበኛ)",
                    currentStockLevel = 5600,
                    reservedStockLevel = 1200,
                    damagedStockLevel = 35,
                    minimumStockThreshold = 1200,
                    unitProductionCost = 38.0,
                    unitSellingPrice = 54.0,
                    storageYardLocation = "Yard Sector 2B",
                    lastBatchDateMillis = now,
                    notes = "Standard commercial & residential building exterior walls"
                ),
                BrickInventoryEntity(
                    id = 3,
                    brickType = "Blocket 20",
                    dimensions = "40 x 20 x 20 cm (20ኛ መሰረት/ዋና)",
                    currentStockLevel = 3200,
                    reservedStockLevel = 600,
                    damagedStockLevel = 28,
                    minimumStockThreshold = 900,
                    unitProductionCost = 52.0,
                    unitSellingPrice = 74.0,
                    storageYardLocation = "Yard Sector 3C",
                    lastBatchDateMillis = now - (2 * oneDayMillis),
                    notes = "Heavy duty load-bearing foundation and boundary perimeter walls"
                ),
                BrickInventoryEntity(
                    id = 4,
                    brickType = "Solid Concrete Brick",
                    dimensions = "25 x 12 x 6 cm (ሙሉ ድፍን ጡብ)",
                    currentStockLevel = 2100,
                    reservedStockLevel = 250,
                    damagedStockLevel = 18,
                    minimumStockThreshold = 500,
                    unitProductionCost = 16.0,
                    unitSellingPrice = 25.0,
                    storageYardLocation = "Yard Sector 4D",
                    lastBatchDateMillis = now - (3 * oneDayMillis),
                    notes = "High-strength solid bricks for columns, lintels, and paved walkways"
                )
            )
            bDao.insertBrickInventories(brickInventories)

            val batches = listOf(
                BrickBatchTrackingEntity(
                    id = 1,
                    batchNumber = "BATCH-B15-081",
                    brickTypeId = 2,
                    brickTypeName = "Blocket 15",
                    productionDateMillis = now - (7 * oneDayMillis),
                    quantityProduced = 1200,
                    quantityAccepted = 1175,
                    defectCount = 25,
                    cementBagsUsed = 12.0,
                    sandUsedM3 = 3.2,
                    aggregateUsedM3 = 4.0,
                    waterLitersUsed = 600.0,
                    totalBatchCostEtb = 45600.0,
                    curingStatus = "READY",
                    curingDaysRequired = 7,
                    curingCompletionDateMillis = now,
                    supervisorOrShift = "Abebe Kebede (Day Shift)",
                    machineOrLineNumber = "Vibro-Press Line 1",
                    qualityGrade = "GRADE_A",
                    notes = "High-density batch for Ayat Real Estate project"
                ),
                BrickBatchTrackingEntity(
                    id = 2,
                    batchNumber = "BATCH-B10-044",
                    brickTypeId = 1,
                    brickTypeName = "Blocket 10",
                    productionDateMillis = now - (3 * oneDayMillis),
                    quantityProduced = 1000,
                    quantityAccepted = 980,
                    defectCount = 20,
                    cementBagsUsed = 8.5,
                    sandUsedM3 = 2.4,
                    aggregateUsedM3 = 3.0,
                    waterLitersUsed = 420.0,
                    totalBatchCostEtb = 26000.0,
                    curingStatus = "CURING",
                    curingDaysRequired = 7,
                    curingCompletionDateMillis = now + (4 * oneDayMillis),
                    supervisorOrShift = "Dawit Tadesse (Afternoon Shift)",
                    machineOrLineNumber = "Vibro-Press Line 2",
                    qualityGrade = "GRADE_A",
                    notes = "Under active water-spray curing process"
                ),
                BrickBatchTrackingEntity(
                    id = 3,
                    batchNumber = "BATCH-B20-032",
                    brickTypeId = 3,
                    brickTypeName = "Blocket 20",
                    productionDateMillis = now - (1 * oneDayMillis),
                    quantityProduced = 750,
                    quantityAccepted = 732,
                    defectCount = 18,
                    cementBagsUsed = 11.0,
                    sandUsedM3 = 2.8,
                    aggregateUsedM3 = 3.5,
                    waterLitersUsed = 520.0,
                    totalBatchCostEtb = 39000.0,
                    curingStatus = "CURING",
                    curingDaysRequired = 7,
                    curingCompletionDateMillis = now + (6 * oneDayMillis),
                    supervisorOrShift = "Abebe Kebede (Day Shift)",
                    machineOrLineNumber = "Vibro-Press Line 1",
                    qualityGrade = "GRADE_A",
                    notes = "Heavy foundation blocks, burlap covered"
                )
            )
            bDao.insertBatches(batches)
        }
    }
}
