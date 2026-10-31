package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.BlockProductEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.RawMaterialEntity
import com.example.data.model.SalesOrderEntity
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import java.util.Locale

// --- 0. Edit Product Unit Cost & Price Dialog (Custom / Manual Entry) ---
@Composable
fun EditProductCostPriceDialog(
    product: BlockProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (customCost: Double, customPrice: Double) -> Unit
) {
    var unitCostText by remember { mutableStateOf(product.unitCost.toString()) }
    var unitPriceText by remember { mutableStateOf(product.unitPrice.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Custom Cost & Price",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${product.name} (${product.sizeSpec})",
                            fontSize = 12.sp,
                            color = Slate500
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Enter custom unit production cost and selling price in Ethiopian Birr (ETB).",
                    fontSize = 13.sp,
                    color = Slate700
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = unitCostText,
                    onValueChange = { unitCostText = it },
                    label = { Text("Unit Production Cost (ETB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("edit_unit_cost_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = unitPriceText,
                    onValueChange = { unitPriceText = it },
                    label = { Text("Unit Selling Price (ETB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("edit_unit_price_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Profit margin preview
                val costVal = unitCostText.toDoubleOrNull() ?: 0.0
                val priceVal = unitPriceText.toDoubleOrNull() ?: 0.0
                val unitMargin = priceVal - costVal
                val marginPct = if (priceVal > 0) (unitMargin / priceVal) * 100 else 0.0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate200.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Unit Margin:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = String.format(Locale.US, "ETB %,.2f (%.1f%%)", unitMargin, marginPct),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (unitMargin >= 0) AmberPrimary else Slate700
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val cost = unitCostText.toDoubleOrNull() ?: product.unitCost
                        val price = unitPriceText.toDoubleOrNull() ?: product.unitPrice
                        onConfirm(cost, price)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_cost_price_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Save Rates", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 1. Log Production Batch Dialog ---
@Composable
fun LogProductionBatchDialog(
    products: List<BlockProductEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        batchCode: String,
        productId: Long,
        quantity: Int,
        defects: Int,
        cement: Double,
        sand: Double,
        gravel: Double,
        water: Double,
        laborCost: Double,
        supervisor: String,
        notes: String
    ) -> Unit
) {
    if (products.isEmpty()) return

    var selectedProductIndex by remember { mutableIntStateOf(0) }
    var quantityText by remember { mutableStateOf("1000") }
    var defectText by remember { mutableStateOf("12") }
    var cementText by remember { mutableStateOf("20") }
    var sandText by remember { mutableStateOf("4.5") }
    var gravelText by remember { mutableStateOf("5.5") }
    var waterText by remember { mutableStateOf("400") }
    var laborCostText by remember { mutableStateOf("1200") }
    var supervisorText by remember { mutableStateOf("Getachew Tadesse") }
    var notesText by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log Production Batch",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Target Block Product", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    OutlinedButton(
                        onClick = { expandedDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("select_product_button")
                    ) {
                        Text(
                            text = products.getOrNull(selectedProductIndex)?.name ?: "Select Block Type",
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        products.forEachIndexed { idx, p ->
                            DropdownMenuItem(
                                text = { Text("${p.name} (${p.sizeSpec})") },
                                onClick = {
                                    selectedProductIndex = idx
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Molded (pcs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("molded_quantity_input")
                    )
                    OutlinedTextField(
                        value = defectText,
                        onValueChange = { defectText = it },
                        label = { Text("Defects (pcs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("defects_input")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(text = "Raw Materials Consumed", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cementText,
                        onValueChange = { cementText = it },
                        label = { Text("Cement (Bags)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sandText,
                        onValueChange = { sandText = it },
                        label = { Text("Sand (m³)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = gravelText,
                        onValueChange = { gravelText = it },
                        label = { Text("Gravel (m³)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = waterText,
                        onValueChange = { waterText = it },
                        label = { Text("Water (L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = laborCostText,
                    onValueChange = { laborCostText = it },
                    label = { Text("Direct Batch Labor Cost (ETB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = supervisorText,
                    onValueChange = { supervisorText = it },
                    label = { Text("Shift Lead / Operator") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Batch Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val product = products.getOrNull(selectedProductIndex) ?: return@Button
                        val qty = quantityText.toIntOrNull() ?: 0
                        val def = defectText.toIntOrNull() ?: 0
                        val cmt = cementText.toDoubleOrNull() ?: 0.0
                        val snd = sandText.toDoubleOrNull() ?: 0.0
                        val grv = gravelText.toDoubleOrNull() ?: 0.0
                        val wtr = waterText.toDoubleOrNull() ?: 0.0
                        val lbr = laborCostText.toDoubleOrNull() ?: 0.0

                        onConfirm(
                            "PRD-B26-${System.currentTimeMillis().toString().takeLast(3)}",
                            product.id,
                            qty,
                            def,
                            cmt,
                            snd,
                            grv,
                            wtr,
                            lbr,
                            supervisorText,
                            notesText
                        )
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("confirm_production_batch_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Confirm & Produce Blocks", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 2. Create Sales Order Dialog ---
@Composable
fun CreateSalesOrderDialog(
    products: List<BlockProductEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
        customerName: String,
        phone: String,
        address: String,
        productId: Long,
        quantity: Int,
        unitPrice: Double,
        deliveryFee: Double,
        initialDeposit: Double,
        paymentMethod: String,
        notes: String
    ) -> Unit
) {
    if (products.isEmpty()) return

    var selectedProductIndex by remember { mutableIntStateOf(0) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("500") }
    var unitPriceText by remember {
        mutableStateOf(products.firstOrNull()?.unitPrice?.toString() ?: "54.0")
    }
    var deliveryFeeText by remember { mutableStateOf("1500.0") }
    var depositText by remember { mutableStateOf("0.0") }
    var paymentMethod by remember { mutableStateOf("TELEBIRR") }
    var notesText by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }

    val qty = quantityText.toIntOrNull() ?: 0
    val price = unitPriceText.toDoubleOrNull() ?: 0.0
    val fee = deliveryFeeText.toDoubleOrNull() ?: 0.0
    val subtotal = qty * price
    val total = subtotal + fee
    val deposit = depositText.toDoubleOrNull() ?: 0.0
    val remainingBalance = (total - deposit).coerceAtLeast(0.0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Sales Order",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer / Contractor Name") },
                    placeholder = { Text("e.g. Afro-Tsion / Sunshine / Ayat") },
                    modifier = Modifier.fillMaxWidth().testTag("order_customer_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Phone (+251)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Site Location") },
                        placeholder = { Text("e.g. Bole Bulbula") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Select Block Type", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Box {
                    val currentProd = products.getOrNull(selectedProductIndex)
                    OutlinedButton(
                        onClick = { expandedDropdown = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${currentProd?.name ?: "Select Block"} (Stock: ${currentProd?.currentStock ?: 0})",
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        products.forEachIndexed { idx, p ->
                            DropdownMenuItem(
                                text = { Text("${p.name} - ETB ${p.unitPrice} (Stock: ${p.currentStock})") },
                                onClick = {
                                    selectedProductIndex = idx
                                    unitPriceText = p.unitPrice.toString()
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity (pcs)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("order_quantity_input")
                    )
                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = { unitPriceText = it },
                        label = { Text("Unit Price (ETB)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("order_unit_price_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = deliveryFeeText,
                        onValueChange = { deliveryFeeText = it },
                        label = { Text("Delivery (ETB)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = depositText,
                        onValueChange = { depositText = it },
                        label = { Text("Deposit (ETB)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("order_deposit_input")
                    )
                }

                if (deposit > 0.0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Payment Method", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        listOf(
                            "TELEBIRR" to "Telebirr (ቴሌብር)",
                            "CBE_BIRR" to "CBE Birr (ንግድ ባንክ)",
                            "BANK_TRANSFER" to "Bank Transfer",
                            "CASH" to "Cash (ጥሬ ገንዘብ)",
                            "CHECK" to "Check"
                        ).chunked(2).forEach { rowMethods ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                rowMethods.forEach { (method, label) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RadioButton(
                                            selected = paymentMethod == method,
                                            onClick = { paymentMethod = method }
                                        )
                                        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Slate200.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Invoice Amount:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(String.format(Locale.US, "ETB %,.2f", total), fontSize = 15.sp, fontWeight = FontWeight.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Balance Due (Credit):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate700)
                            Text(String.format(Locale.US, "ETB %,.2f", remainingBalance), fontSize = 14.sp, fontWeight = FontWeight.Black, color = if (remainingBalance > 0) AmberPrimary else Slate900)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val product = products.getOrNull(selectedProductIndex) ?: return@Button
                        if (customerName.isBlank()) return@Button
                        onConfirm(
                            customerName,
                            customerPhone,
                            deliveryAddress,
                            product.id,
                            qty,
                            price,
                            fee,
                            deposit,
                            paymentMethod,
                            notesText
                        )
                        onDismiss()
                    },
                    enabled = customerName.isNotBlank() && qty > 0,
                    modifier = Modifier.fillMaxWidth().testTag("confirm_create_order_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Confirm & Create Order", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 3. Collect Order Payment Dialog ---
@Composable
fun CollectPaymentDialog(
    order: SalesOrderEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, paymentMethod: String, referenceNo: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf(order.balanceAmount.toString()) }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var refNoText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Collect Payment",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Order #${order.orderNumber} • ${order.customerName}",
                    fontSize = 13.sp,
                    color = Slate500
                )
                Text(
                    text = "Current Balance: " + String.format(Locale.US, "ETB %,.2f", order.balanceAmount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (ETB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Payment Method", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    listOf(
                        "TELEBIRR" to "Telebirr (ቴሌብር)",
                        "CBE_BIRR" to "CBE Birr (ንግድ ባንክ)",
                        "BANK_TRANSFER" to "Bank Wire",
                        "CASH" to "Cash (ጥሬ ገንዘብ)",
                        "CHECK" to "Check"
                    ).chunked(2).forEach { rowMethods ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowMethods.forEach { (method, label) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = paymentMethod == method,
                                        onClick = { paymentMethod = method }
                                    )
                                    Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900, maxLines = 1)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = refNoText,
                    onValueChange = { refNoText = it },
                    label = { Text("Receipt / Ref Number (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Payment Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onConfirm(amount, paymentMethod, refNoText, notesText)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("confirm_collect_payment_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Confirm Payment Received", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 4. Restock Raw Material Dialog ---
@Composable
fun RestockMaterialDialog(
    material: RawMaterialEntity,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, unitCost: Double) -> Unit
) {
    var quantityText by remember { mutableStateOf("10.0") }
    var unitCostText by remember { mutableStateOf(material.costPerUnit.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Restock Raw Material",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "${material.name} (Current: ${material.currentStock} ${material.unit})",
                    fontSize = 13.sp,
                    color = Slate500
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Added Quantity (${material.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("restock_quantity_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = unitCostText,
                    onValueChange = { unitCostText = it },
                    label = { Text("Cost Per ${material.unit} (ETB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val qty = quantityText.toDoubleOrNull() ?: 0.0
                        val cost = unitCostText.toDoubleOrNull() ?: material.costPerUnit
                        if (qty > 0) {
                            onConfirm(qty, cost)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("confirm_restock_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Add to Raw Material Stock", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 5. Add Employee Dialog ---
@Composable
fun AddEmployeeDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, phone: String, role: String, wageType: String, rate: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Block Machine Operator") }
    var wageType by remember { mutableStateOf("DAILY") }
    var rateText by remember { mutableStateOf("600.0") }

    val roles = listOf(
        "Block Machine Operator",
        "Batching Mixer Operator",
        "Yard & Curing Supervisor",
        "Block Stacker & Palletizer",
        "Forklift & Loading Driver",
        "Maintenance Technician"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Worker / Staff",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. Alazar Mengistu") },
                    modifier = Modifier.fillMaxWidth().testTag("employee_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (+251)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Job Role", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                roles.forEach { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { role = r }
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = role == r, onClick = { role = r })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = r, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Wage Structure", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("DAILY" to "Daily", "MONTHLY" to "Monthly", "PIECE_RATE" to "Piece").forEach { (type, lbl) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = wageType == type, onClick = { wageType = type })
                            Text(text = lbl, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = {
                        Text(
                            when (wageType) {
                                "DAILY" -> "Daily Rate (ETB)"
                                "MONTHLY" -> "Monthly Salary (ETB)"
                                else -> "Rate per 100 blocks (ETB)"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val rate = rateText.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank()) {
                            onConfirm(name, phone, role, wageType, rate)
                            onDismiss()
                        }
                    },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().testTag("confirm_add_employee_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Save Employee", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 6. Record Wage Payout Dialog ---
@Composable
fun RecordWagePayoutDialog(
    employees: List<EmployeeEntity>,
    onDismiss: () -> Unit,
    onConfirm: (empId: Long, amount: Double, paymentType: String, method: String, notes: String) -> Unit
) {
    if (employees.isEmpty()) return

    var selectedIndex by remember { mutableIntStateOf(0) }
    var amountText by remember { mutableStateOf(employees.firstOrNull()?.rateAmount?.toString() ?: "600.0") }
    var paymentType by remember { mutableStateOf("DAILY_WAGE") }
    var method by remember { mutableStateOf("TELEBIRR") }
    var notesText by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Disburse Wage / Advance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Select Worker", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.SemiBold)
                Box {
                    val emp = employees.getOrNull(selectedIndex)
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${emp?.fullName} (${emp?.role})",
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                        employees.forEachIndexed { idx, e ->
                            DropdownMenuItem(
                                text = { Text("${e.fullName} - ${e.role}") },
                                onClick = {
                                    selectedIndex = idx
                                    amountText = e.rateAmount.toString()
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payout Amount (ETB)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("wage_amount_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Payout Category", fontSize = 13.sp, color = Slate900, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("DAILY_WAGE" to "Day Wage", "PIECE_PAYMENT" to "Piece Pay", "SALARY" to "Salary", "ADVANCE" to "Advance").forEach { (cat, lbl) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = paymentType == cat, onClick = { paymentType = cat })
                            Text(text = lbl, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Disbursement Channel", fontSize = 13.sp, color = Slate900, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf(
                        "TELEBIRR" to "Telebirr",
                        "CBE_BIRR" to "CBE Birr",
                        "CASH" to "Cash",
                        "BANK_TRANSFER" to "Wire"
                    ).forEach { (m, lbl) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = method == m, onClick = { method = m })
                            Text(text = lbl, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Reference") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val emp = employees.getOrNull(selectedIndex) ?: return@Button
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onConfirm(emp.id, amt, paymentType, method, notesText)
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("confirm_wage_payout_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                ) {
                    Text("Disburse Payment", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
