package com.example

import com.example.data.model.BlockProductEntity
import com.example.data.model.ProductionBatchEntity
import com.example.data.model.SalesOrderEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BlocketSystemUnitTest {

    @Test
    fun testBlocketTypesAndCustomPricing() {
        val blocket10 = BlockProductEntity(
            id = 1,
            name = "Blocket 10",
            sizeSpec = "40 x 20 x 10 cm",
            currentStock = 3800,
            unitCost = 26.0,
            unitPrice = 38.0
        )
        val blocket15 = BlockProductEntity(
            id = 2,
            name = "Blocket 15",
            sizeSpec = "40 x 20 x 15 cm",
            currentStock = 5600,
            unitCost = 38.0,
            unitPrice = 54.0
        )
        val blocket20 = BlockProductEntity(
            id = 3,
            name = "Blocket 20",
            sizeSpec = "40 x 20 x 20 cm",
            currentStock = 3200,
            unitCost = 52.0,
            unitPrice = 74.0
        )

        // Verify Ethiopian blocket specifications
        assertEquals("Blocket 10", blocket10.name)
        assertEquals("Blocket 15", blocket15.name)
        assertEquals("Blocket 20", blocket20.name)

        // Custom rate margin calculation in ETB
        val margin10 = blocket10.unitPrice - blocket10.unitCost
        assertEquals(12.0, margin10, 0.001)

        val margin15 = blocket15.unitPrice - blocket15.unitCost
        assertEquals(16.0, margin15, 0.001)

        val margin20 = blocket20.unitPrice - blocket20.unitCost
        assertEquals(22.0, margin20, 0.001)

        // Updating custom price and cost
        val customizedBlocket15 = blocket15.copy(unitCost = 40.0, unitPrice = 58.0)
        assertEquals(40.0, customizedBlocket15.unitCost, 0.001)
        assertEquals(58.0, customizedBlocket15.unitPrice, 0.001)
        assertEquals(18.0, customizedBlocket15.unitPrice - customizedBlocket15.unitCost, 0.001)
    }

    @Test
    fun testSalesOrderBalanceAndFinancials() {
        val order = SalesOrderEntity(
            id = 1,
            orderNumber = "ORD-2026-001",
            customerName = "Afro-Tsion Construction",
            customerPhone = "+251 91 123 4567",
            deliveryAddress = "Bole Bulbula Site",
            orderDateMillis = System.currentTimeMillis(),
            blockProductId = 2,
            blockProductName = "Blocket 15",
            quantity = 1500,
            unitPrice = 54.0, // ETB
            deliveryFee = 1500.0, // ETB
            totalAmount = (1500 * 54.0) + 1500.0, // 82500 ETB
            paidAmount = 50000.0, // Paid via Telebirr
            balanceAmount = 32500.0, // Remaining credit
            paymentStatus = "PARTIAL",
            deliveryStatus = "DISPATCHED"
        )

        assertEquals(82500.0, order.totalAmount, 0.001)
        assertEquals(32500.0, order.balanceAmount, 0.001)
        assertTrue(order.balanceAmount > 0)
    }

    @Test
    fun testBatchProductionDefectRate() {
        val batch = ProductionBatchEntity(
            id = 1,
            batchCode = "PRD-B26-001",
            dateMillis = System.currentTimeMillis(),
            blockProductId = 2,
            blockProductName = "Blocket 15",
            quantityMolded = 1200,
            defectCount = 18,
            cementBagsUsed = 24.0,
            sandUsedM3 = 5.0,
            aggregateUsedM3 = 6.0,
            waterLitresUsed = 500.0,
            laborCost = 1200.0, // ETB
            supervisorName = "Getachew Tadesse"
        )

        val netUsableBlocks = batch.quantityMolded - batch.defectCount
        assertEquals(1182, netUsableBlocks)
        val defectPct = (batch.defectCount.toDouble() / batch.quantityMolded) * 100
        assertEquals(1.5, defectPct, 0.01)
    }

    @Test
    fun testBrickProductionInventoryAndStockLevels() {
        val inventory = com.example.data.model.BrickInventoryEntity(
            id = 2,
            brickType = "Blocket 15",
            dimensions = "40 x 20 x 15 cm",
            currentStockLevel = 5600,
            reservedStockLevel = 1200,
            damagedStockLevel = 35,
            minimumStockThreshold = 1200,
            unitProductionCost = 38.0,
            unitSellingPrice = 54.0,
            storageYardLocation = "Yard Sector 2B"
        )

        assertEquals("Blocket 15", inventory.brickType)
        assertEquals(5600, inventory.currentStockLevel)
        assertEquals(1200, inventory.reservedStockLevel)
        assertEquals(35, inventory.damagedStockLevel)
        assertEquals(6835, inventory.totalPhysicalStock)
        assertEquals(false, inventory.isLowStock)

        // Low stock trigger check
        val lowStockInventory = inventory.copy(currentStockLevel = 900)
        assertTrue(lowStockInventory.isLowStock)
    }

    @Test
    fun testBrickBatchTrackingAndCuringProcess() {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24L * 60 * 60 * 1000

        val batch = com.example.data.model.BrickBatchTrackingEntity(
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
            qualityGrade = "GRADE_A"
        )

        assertEquals("BATCH-B15-081", batch.batchNumber)
        assertEquals(1200, batch.quantityProduced)
        assertEquals(1175, batch.quantityAccepted)
        assertEquals(25, batch.defectCount)
        assertEquals("READY", batch.curingStatus)
        assertEquals("GRADE_A", batch.qualityGrade)
        assertEquals(45600.0, batch.totalBatchCostEtb, 0.01)
    }

    @Test
    fun testFilterBatchesByBrickType10_15_20() {
        val now = System.currentTimeMillis()
        val batches = listOf(
            ProductionBatchEntity(
                id = 1,
                batchCode = "PRD-B10-001",
                dateMillis = now,
                blockProductId = 1,
                blockProductName = "Blocket 10",
                quantityMolded = 1000,
                defectCount = 10,
                cementBagsUsed = 15.0,
                sandUsedM3 = 3.0,
                aggregateUsedM3 = 4.0,
                laborCost = 1500.0,
                supervisorName = "Selamawit Tadesse"
            ),
            ProductionBatchEntity(
                id = 2,
                batchCode = "PRD-B15-001",
                dateMillis = now - 3600_000,
                blockProductId = 2,
                blockProductName = "Blocket 15",
                quantityMolded = 1500,
                defectCount = 15,
                cementBagsUsed = 22.0,
                sandUsedM3 = 4.5,
                aggregateUsedM3 = 6.0,
                laborCost = 2200.0,
                supervisorName = "Getachew Bekele"
            ),
            ProductionBatchEntity(
                id = 3,
                batchCode = "PRD-B20-001",
                dateMillis = now - 7200_000,
                blockProductId = 3,
                blockProductName = "Blocket 20",
                quantityMolded = 800,
                defectCount = 8,
                cementBagsUsed = 18.0,
                sandUsedM3 = 3.5,
                aggregateUsedM3 = 5.0,
                laborCost = 1800.0,
                supervisorName = "Selamawit Tadesse"
            )
        )

        // 1. Filter Blocket 10
        val results10 = com.example.ui.components.filterBatches(
            batches = batches,
            query = "",
            typeFilter = com.example.ui.components.BatchTypeFilter.BLOCKET_10,
            dateFilter = com.example.ui.components.BatchDateFilter.ALL,
            nowMillis = now
        )
        assertEquals(1, results10.size)
        assertEquals("Blocket 10", results10[0].blockProductName)

        // 2. Filter Blocket 15
        val results15 = com.example.ui.components.filterBatches(
            batches = batches,
            query = "",
            typeFilter = com.example.ui.components.BatchTypeFilter.BLOCKET_15,
            dateFilter = com.example.ui.components.BatchDateFilter.ALL,
            nowMillis = now
        )
        assertEquals(1, results15.size)
        assertEquals("Blocket 15", results15[0].blockProductName)

        // 3. Filter Blocket 20
        val results20 = com.example.ui.components.filterBatches(
            batches = batches,
            query = "",
            typeFilter = com.example.ui.components.BatchTypeFilter.BLOCKET_20,
            dateFilter = com.example.ui.components.BatchDateFilter.ALL,
            nowMillis = now
        )
        assertEquals(1, results20.size)
        assertEquals("Blocket 20", results20[0].blockProductName)

        // 4. All Types
        val allResults = com.example.ui.components.filterBatches(
            batches = batches,
            query = "",
            typeFilter = com.example.ui.components.BatchTypeFilter.ALL,
            dateFilter = com.example.ui.components.BatchDateFilter.ALL,
            nowMillis = now
        )
        assertEquals(3, allResults.size)
    }

    @Test
    fun testFilterBatchesByDateAddedAndSorting() {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24L * 60 * 60 * 1000

        val batchToday = ProductionBatchEntity(
            id = 1,
            batchCode = "PRD-TODAY",
            dateMillis = now - 3600_000, // 1 hour ago (Today)
            blockProductId = 2,
            blockProductName = "Blocket 15",
            quantityMolded = 1000,
            defectCount = 10,
            cementBagsUsed = 15.0,
            sandUsedM3 = 3.0,
            aggregateUsedM3 = 4.0,
            laborCost = 1500.0,
            supervisorName = "Selamawit Tadesse"
        )
        val batch3DaysAgo = ProductionBatchEntity(
            id = 2,
            batchCode = "PRD-3DAYS",
            dateMillis = now - (3 * oneDayMillis),
            blockProductId = 1,
            blockProductName = "Blocket 10",
            quantityMolded = 1200,
            defectCount = 12,
            cementBagsUsed = 18.0,
            sandUsedM3 = 3.5,
            aggregateUsedM3 = 4.5,
            laborCost = 1800.0,
            supervisorName = "Getachew Bekele"
        )
        val batch14DaysAgo = ProductionBatchEntity(
            id = 3,
            batchCode = "PRD-14DAYS",
            dateMillis = now - (14 * oneDayMillis),
            blockProductId = 3,
            blockProductName = "Blocket 20",
            quantityMolded = 800,
            defectCount = 8,
            cementBagsUsed = 16.0,
            sandUsedM3 = 3.0,
            aggregateUsedM3 = 4.0,
            laborCost = 1400.0,
            supervisorName = "Abebe Kebede"
        )

        val batches = listOf(batchToday, batch3DaysAgo, batch14DaysAgo)

        // Today filter
        val todayResults = com.example.ui.components.filterBatches(
            batches = batches,
            query = "",
            typeFilter = com.example.ui.components.BatchTypeFilter.ALL,
            dateFilter = com.example.ui.components.BatchDateFilter.TODAY,
            nowMillis = now
        )
        assertEquals(1, todayResults.size)
        assertEquals("PRD-TODAY", todayResults[0].batchCode)

        // Last 7 days filter
        val last7DaysResults = com.example.ui.components.filterBatches(
            batches = batches,
            query = "",
            typeFilter = com.example.ui.components.BatchTypeFilter.ALL,
            dateFilter = com.example.ui.components.BatchDateFilter.LAST_7_DAYS,
            nowMillis = now
        )
        assertEquals(2, last7DaysResults.size)
        assertTrue(last7DaysResults.any { it.batchCode == "PRD-TODAY" })
        assertTrue(last7DaysResults.any { it.batchCode == "PRD-3DAYS" })

        // Sorting: Newest First vs Oldest First
        val newestFirst = com.example.ui.components.filterBatches(
            batches = batches,
            query = "",
            typeFilter = com.example.ui.components.BatchTypeFilter.ALL,
            dateFilter = com.example.ui.components.BatchDateFilter.ALL,
            dateSort = com.example.ui.components.BatchDateSort.NEWEST_FIRST,
            nowMillis = now
        )
        assertEquals("PRD-TODAY", newestFirst.first().batchCode)
        assertEquals("PRD-14DAYS", newestFirst.last().batchCode)

        val oldestFirst = com.example.ui.components.filterBatches(
            batches = batches,
            query = "",
            typeFilter = com.example.ui.components.BatchTypeFilter.ALL,
            dateFilter = com.example.ui.components.BatchDateFilter.ALL,
            dateSort = com.example.ui.components.BatchDateSort.OLDEST_FIRST,
            nowMillis = now
        )
        assertEquals("PRD-14DAYS", oldestFirst.first().batchCode)
        assertEquals("PRD-TODAY", oldestFirst.last().batchCode)
    }

    @Test
    fun testFilterBatchesBySearchQuery() {
        val now = System.currentTimeMillis()
        val batches = listOf(
            ProductionBatchEntity(
                id = 1,
                batchCode = "PRD-ETH-101",
                dateMillis = now,
                blockProductId = 2,
                blockProductName = "Blocket 15",
                quantityMolded = 1800,
                defectCount = 15,
                cementBagsUsed = 28.0,
                sandUsedM3 = 6.0,
                aggregateUsedM3 = 7.5,
                laborCost = 3600.0,
                supervisorName = "Selamawit Tadesse",
                notes = "Morning run of 15 cm standard blockets."
            ),
            ProductionBatchEntity(
                id = 2,
                batchCode = "PRD-ETH-100",
                dateMillis = now,
                blockProductId = 1,
                blockProductName = "Blocket 10",
                quantityMolded = 2400,
                defectCount = 18,
                cementBagsUsed = 26.0,
                sandUsedM3 = 5.4,
                aggregateUsedM3 = 6.8,
                laborCost = 4800.0,
                supervisorName = "Getachew Bekele",
                notes = "10 cm partition blockets run."
            )
        )

        // Search by batch code
        val codeSearch = com.example.ui.components.filterBatches(
            batches = batches,
            query = "101",
            typeFilter = com.example.ui.components.BatchTypeFilter.ALL,
            dateFilter = com.example.ui.components.BatchDateFilter.ALL,
            nowMillis = now
        )
        assertEquals(1, codeSearch.size)
        assertEquals("PRD-ETH-101", codeSearch[0].batchCode)

        // Search by supervisor name
        val supervisorSearch = com.example.ui.components.filterBatches(
            batches = batches,
            query = "Selamawit",
            typeFilter = com.example.ui.components.BatchTypeFilter.ALL,
            dateFilter = com.example.ui.components.BatchDateFilter.ALL,
            nowMillis = now
        )
        assertEquals(1, supervisorSearch.size)
        assertEquals("Selamawit Tadesse", supervisorSearch[0].supervisorName)

        // Search by notes keyword
        val notesSearch = com.example.ui.components.filterBatches(
            batches = batches,
            query = "partition",
            typeFilter = com.example.ui.components.BatchTypeFilter.ALL,
            dateFilter = com.example.ui.components.BatchDateFilter.ALL,
            nowMillis = now
        )
        assertEquals(1, notesSearch.size)
        assertEquals("PRD-ETH-100", notesSearch[0].batchCode)
    }

    @Test
    fun testDailySalesReportRevenueAndOutputCalculations() {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24L * 60 * 60 * 1000

        // Sales Orders: 2 orders today, 1 order yesterday
        val orders = listOf(
            SalesOrderEntity(
                id = 1,
                orderNumber = "ORD-TODAY-01",
                customerName = "Sunshine Construction PLC",
                customerPhone = "+251 91 144 5566",
                deliveryAddress = "Bole Bulbula",
                orderDateMillis = now - 3600_000,
                blockProductId = 2,
                blockProductName = "Blocket 15",
                quantity = 1500,
                unitPrice = 54.0,
                deliveryFee = 2500.0,
                totalAmount = 83500.0,
                paidAmount = 50000.0,
                balanceAmount = 33500.0,
                paymentStatus = "PARTIAL",
                deliveryStatus = "DISPATCHED"
            ),
            SalesOrderEntity(
                id = 2,
                orderNumber = "ORD-TODAY-02",
                customerName = "Flintstone Engineering",
                customerPhone = "+251 92 233 4455",
                deliveryAddress = "CMC Site",
                orderDateMillis = now - 7200_000,
                blockProductId = 1,
                blockProductName = "Blocket 10",
                quantity = 2500,
                unitPrice = 38.0,
                deliveryFee = 3000.0,
                totalAmount = 98000.0,
                paidAmount = 98000.0,
                balanceAmount = 0.0,
                paymentStatus = "PAID",
                deliveryStatus = "DELIVERED"
            ),
            SalesOrderEntity(
                id = 3,
                orderNumber = "ORD-YESTERDAY",
                customerName = "Afro-Tsion",
                customerPhone = "+251 93 322 1100",
                deliveryAddress = "Ayat Site",
                orderDateMillis = now - (2 * oneDayMillis),
                blockProductId = 3,
                blockProductName = "Blocket 20",
                quantity = 1000,
                unitPrice = 74.0,
                deliveryFee = 2000.0,
                totalAmount = 76000.0,
                paidAmount = 76000.0,
                balanceAmount = 0.0,
                paymentStatus = "PAID",
                deliveryStatus = "DELIVERED"
            )
        )

        // Payment Transactions for today
        val txns = listOf(
            com.example.data.model.PaymentTransactionEntity(
                id = 1,
                orderId = 1,
                orderNumber = "ORD-TODAY-01",
                customerName = "Sunshine Construction PLC",
                dateMillis = now - 3600_000,
                amount = 50000.0,
                paymentMethod = "CBE_BIRR",
                referenceNo = "CBE-001"
            ),
            com.example.data.model.PaymentTransactionEntity(
                id = 2,
                orderId = 2,
                orderNumber = "ORD-TODAY-02",
                customerName = "Flintstone Engineering",
                dateMillis = now - 7200_000,
                amount = 98000.0,
                paymentMethod = "BANK_TRANSFER",
                referenceNo = "WIRE-002"
            )
        )

        // Production Batches for today (Blocket 15 and Blocket 10)
        val batches = listOf(
            ProductionBatchEntity(
                id = 1,
                batchCode = "PRD-TODAY-15",
                dateMillis = now - 3600_000,
                blockProductId = 2,
                blockProductName = "Blocket 15",
                quantityMolded = 1800,
                defectCount = 15,
                cementBagsUsed = 28.0,
                sandUsedM3 = 6.0,
                aggregateUsedM3 = 7.5,
                laborCost = 3600.0,
                supervisorName = "Selamawit Tadesse"
            ),
            ProductionBatchEntity(
                id = 2,
                batchCode = "PRD-TODAY-10",
                dateMillis = now - 7200_000,
                blockProductId = 1,
                blockProductName = "Blocket 10",
                quantityMolded = 2400,
                defectCount = 18,
                cementBagsUsed = 26.0,
                sandUsedM3 = 5.4,
                aggregateUsedM3 = 6.8,
                laborCost = 4800.0,
                supervisorName = "Selamawit Tadesse"
            ),
            ProductionBatchEntity(
                id = 3,
                batchCode = "PRD-YESTERDAY",
                dateMillis = now - (2 * oneDayMillis),
                blockProductId = 3,
                blockProductName = "Blocket 20",
                quantityMolded = 1200,
                defectCount = 12,
                cementBagsUsed = 25.0,
                sandUsedM3 = 5.5,
                aggregateUsedM3 = 7.0,
                laborCost = 2400.0,
                supervisorName = "Selamawit Tadesse"
            )
        )

        val materials = listOf(
            com.example.data.model.RawMaterialEntity(
                name = "Cement",
                unit = "Bags",
                currentStock = 200.0,
                minAlertThreshold = 50.0,
                costPerUnit = 1650.0,
                lastUpdatedDate = now
            )
        )
        val products = listOf(
            BlockProductEntity(
                id = 1,
                name = "Blocket 10",
                sizeSpec = "40x20x10",
                currentStock = 3000,
                unitCost = 26.0,
                unitPrice = 38.0
            )
        )

        val report = com.example.ui.viewmodel.calculateDailyReport(
            orders = orders,
            batches = batches,
            txns = txns,
            materials = materials,
            products = products,
            nowMillis = now
        )

        // 1. Verify Today's Total Revenue
        assertEquals(148000.0, report.todayRevenue, 0.001)
        assertEquals(181500.0, report.todayInvoicedSales, 0.001)
        assertEquals(33500.0, report.todayPendingCredit, 0.001)
        assertEquals(2, report.todaySalesCount)
        assertEquals(4000, report.todayBlocksSold) // 1500 + 2500

        // 2. Verify Today's Production Output
        assertEquals(4200, report.todayProductionOutput) // 1800 + 2400
        assertEquals(33, report.todayDefectCount) // 15 + 18
        assertEquals(4167, report.todayNetUsableBlocks)
        assertEquals((33.0 / 4200.0) * 100.0, report.todayDefectRatePct, 0.01)
        assertEquals(2, report.todayBatchesCount)

        // 3. Verify Block Type Breakdown
        assertEquals(2400, report.outputByBlockType["Blocket 10"])
        assertEquals(1800, report.outputByBlockType["Blocket 15"])
        assertEquals(0, report.outputByBlockType["Blocket 20"])

        // 4. Verify Payment Channels
        assertEquals(50000.0, report.paymentMethodsBreakdown["CBE Birr"] ?: 0.0, 0.001)
        assertEquals(98000.0, report.paymentMethodsBreakdown["Bank Transfer"] ?: 0.0, 0.001)

        // 5. Verify Labor Cost
        assertEquals(8400.0, report.todayLaborCost, 0.001)
    }

    @Test
    fun testDailySalesReportEmptyDay() {
        val now = System.currentTimeMillis()
        val emptyReport = com.example.ui.viewmodel.calculateDailyReport(
            orders = emptyList(),
            batches = emptyList(),
            txns = emptyList(),
            materials = emptyList(),
            products = emptyList(),
            nowMillis = now
        )

        assertEquals(0.0, emptyReport.todayRevenue, 0.001)
        assertEquals(0, emptyReport.todayProductionOutput)
        assertEquals(0, emptyReport.todayDefectCount)
        assertEquals(0, emptyReport.todayNetUsableBlocks)
        assertEquals(0.0, emptyReport.todayDefectRatePct, 0.001)
        assertEquals(0, emptyReport.todaySalesCount)
        assertEquals("None", emptyReport.topSellingBlockName)
        assertEquals(0, emptyReport.outputByBlockType["Blocket 10"])
        assertEquals(0, emptyReport.outputByBlockType["Blocket 15"])
        assertEquals(0, emptyReport.outputByBlockType["Blocket 20"])
    }

    @Test
    fun testEthiopianDateFormatting() {
        // Oct 31, 2026: Month = Calendar.OCTOBER (9), Day = 31, Year = 2026
        val cal = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.OCTOBER, 31, 12, 0, 0)
        }
        val formatted = com.example.util.EthiopianDateHelper.formatEthiopianDate(cal.timeInMillis)
        assertTrue(formatted.contains("ጥቅምት"))
        assertTrue(formatted.contains("21"))
        assertTrue(formatted.contains("2019"))
        assertTrue(formatted.contains("Tikimt"))
    }
}
