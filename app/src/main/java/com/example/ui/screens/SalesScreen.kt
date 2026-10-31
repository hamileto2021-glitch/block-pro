package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.SalesOrderEntity
import com.example.ui.components.CollectPaymentDialog
import com.example.ui.components.CreateSalesOrderDialog
import com.example.ui.components.DeliveryStatusChip
import com.example.ui.components.PaymentStatusChip
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.viewmodel.BlocketViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesScreen(
    viewModel: BlocketViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.salesOrders.collectAsStateWithLifecycle()
    val products by viewModel.blockProducts.collectAsStateWithLifecycle()

    var statusFilter by remember { mutableStateOf("ALL") }
    val filterTabs = listOf("ALL" to "All Orders", "UNPAID" to "Unpaid Credit", "PARTIAL" to "Partial", "PAID" to "Fully Paid")

    var showCreateOrderDialog by remember { mutableStateOf(false) }
    var orderToCollectPayment by remember { mutableStateOf<SalesOrderEntity?>(null) }

    val filteredOrders = when (statusFilter) {
        "UNPAID" -> orders.filter { it.paymentStatus == "UNPAID" || it.balanceAmount > 0 }
        "PARTIAL" -> orders.filter { it.paymentStatus == "PARTIAL" }
        "PAID" -> orders.filter { it.paymentStatus == "PAID" }
        else -> orders
    }

    val totalOutstanding = orders.sumOf { it.balanceAmount }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("sales_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateOrderDialog = true },
                containerColor = AmberPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_new_sales_order")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Order")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "New Sale", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Outstanding Credit Alert Banner
            if (totalOutstanding > 0) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Outstanding Customer Credit",
                                    fontSize = 13.sp,
                                    color = Color(0xFF7F1D1D),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(Locale.US, "ETB %,.2f Pending", totalOutstanding),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ExpenseRed
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ExpenseRed.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${orders.count { it.balanceAmount > 0 }} Accounts",
                                    fontSize = 12.sp,
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // Filter Tabs Row
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterTabs) { (key, label) ->
                        val isSelected = statusFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) AmberPrimary else Slate200)
                                .clickable { statusFilter = key }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Slate900,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (filteredOrders.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        Text("No orders match the selected filter.", color = Slate400)
                    }
                }
            }

            items(filteredOrders) { order ->
                OrderCard(
                    order = order,
                    onCollectPayment = { orderToCollectPayment = order },
                    onUpdateDelivery = { newStatus -> viewModel.updateDeliveryStatus(order.id, newStatus) }
                )
            }
        }
    }

    if (showCreateOrderDialog) {
        CreateSalesOrderDialog(
            products = products,
            onDismiss = { showCreateOrderDialog = false },
            onConfirm = { name, phone, address, prodId, qty, price, fee, deposit, method, notes ->
                viewModel.createSalesOrder(name, phone, address, prodId, qty, price, fee, deposit, method, notes)
            }
        )
    }

    orderToCollectPayment?.let { order ->
        CollectPaymentDialog(
            order = order,
            onDismiss = { orderToCollectPayment = null },
            onConfirm = { amount, method, refNo, notes ->
                viewModel.recordOrderPayment(order.id, amount, method, refNo, notes)
            }
        )
    }
}

@Composable
private fun OrderCard(
    order: SalesOrderEntity,
    onCollectPayment: () -> Unit,
    onUpdateDelivery: (String) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    var showDeliveryMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("order_card_${order.orderNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order Number & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "#${order.orderNumber}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = AmberPrimary
                    )
                    Text(
                        text = dateFormat.format(Date(order.orderDateMillis)),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    PaymentStatusChip(status = order.paymentStatus)
                    DeliveryStatusChip(status = order.deliveryStatus)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Customer Name & Location
            Text(
                text = order.customerName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (order.customerPhone.isNotEmpty() || order.deliveryAddress.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (order.customerPhone.isNotEmpty()) {
                        Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Slate700, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(order.customerPhone, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    if (order.deliveryAddress.isNotEmpty()) {
                        Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = Slate700, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(order.deliveryAddress, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Order Product Details Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Item & Quantity", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Text(
                        text = "${order.quantity} pcs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = order.blockProductName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Invoiced", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    Text(
                        text = String.format(Locale.US, "ETB %,.2f", order.totalAmount),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (order.deliveryFee > 0) {
                        Text(text = "+ ETB ${order.deliveryFee} delivery", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Financial Balance & Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Paid: " + String.format(Locale.US, "ETB %,.2f", order.paidAmount),
                        fontSize = 13.sp,
                        color = ProfitGreen,
                        fontWeight = FontWeight.Bold
                    )
                    if (order.balanceAmount > 0) {
                        Text(
                            text = "Due: " + String.format(Locale.US, "ETB %,.2f", order.balanceAmount),
                            fontSize = 14.sp,
                            color = ExpenseRed,
                            fontWeight = FontWeight.Black
                        )
                    } else {
                        Text(
                            text = "Fully Cleared",
                            fontSize = 13.sp,
                            color = ProfitGreen,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Update Delivery Dropdown Trigger
                    Box {
                        OutlinedButton(
                            onClick = { showDeliveryMenu = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Status", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        DropdownMenu(
                            expanded = showDeliveryMenu,
                            onDismissRequest = { showDeliveryMenu = false }
                        ) {
                            listOf("PENDING", "DISPATCHED", "DELIVERED").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        onUpdateDelivery(status)
                                        showDeliveryMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Collect Payment Button (if balance > 0)
                    if (order.balanceAmount > 0) {
                        Button(
                            onClick = onCollectPayment,
                            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("collect_payment_btn_${order.orderNumber}")
                        ) {
                            Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(text = "Collect", fontSize = 13.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
