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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.PaymentTransactionEntity
import com.example.data.model.SalesOrderEntity
import com.example.ui.components.CollectPaymentDialog
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate900
import com.example.ui.viewmodel.BlocketViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentsScreen(
    viewModel: BlocketViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.paymentTransactions.collectAsStateWithLifecycle()
    val orders by viewModel.salesOrders.collectAsStateWithLifecycle()

    var selectedMethodFilter by remember { mutableStateOf("ALL") }
    val methodFilters = listOf(
        "ALL" to "All Methods",
        "TELEBIRR" to "Telebirr (ቴሌብር)",
        "CBE_BIRR" to "CBE Birr",
        "BANK_TRANSFER" to "Bank Wire",
        "CASH" to "Cash",
        "CHECK" to "Check"
    )

    var orderToCollect by remember { mutableStateOf<SalesOrderEntity?>(null) }

    val totalCollected = transactions.sumOf { it.amount }
    val pendingCreditOrders = orders.filter { it.balanceAmount > 0 }
    val totalPendingCredit = pendingCreditOrders.sumOf { it.balanceAmount }

    val filteredTxns = when (selectedMethodFilter) {
        "ALL" -> transactions
        else -> transactions.filter { it.paymentMethod.equals(selectedMethodFilter, ignoreCase = true) }
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("payments_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. High-Level Financial Ledger Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Cash Flow & Collections",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "ETB %,.2f", totalCollected),
                        color = Color(0xFF4ADE80),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Pending Receivables", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                String.format(Locale.US, "ETB %,.2f", totalPendingCredit),
                                color = Color(0xFFFCA5A5),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Recorded", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${transactions.size} Payments",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // 2. Pending Credit Collection Row
        if (pendingCreditOrders.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Pending Customer Credit Invoices",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(pendingCreditOrders) { order ->
                            Card(
                                modifier = Modifier.width(260.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                         Text(text = "#${order.orderNumber}", fontSize = 13.sp, color = AmberPrimary, fontWeight = FontWeight.Black)
                                        Text(
                                            text = "Due: ETB ${order.balanceAmount.toInt()}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ExpenseRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = order.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { orderToCollect = order },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                                        modifier = Modifier.fillMaxWidth().height(38.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Collect Payment", fontSize = 13.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Payment Method Filter Row
        item {
            Column {
                Text(
                    text = "Payment Transactions Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(methodFilters) { (key, label) ->
                        val isSelected = selectedMethodFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) AmberPrimary else Slate200)
                                .clickable { selectedMethodFilter = key }
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
        }

        if (filteredTxns.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 30.dp), contentAlignment = Alignment.Center) {
                    Text("No transactions found for this method.", color = Slate400)
                }
            }
        }

        items(filteredTxns) { txn ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("payment_txn_${txn.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when (txn.paymentMethod) {
                                        "TELEBIRR" -> Color(0xFFCCFBF1)
                                        "CBE_BIRR" -> Color(0xFFF3E8FF)
                                        "CASH" -> Color(0xFFDCFCE7)
                                        "BANK_TRANSFER" -> Color(0xFFE0F2FE)
                                        "MOBILE_MONEY" -> Color(0xFFFFEDD5)
                                        else -> Slate200
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (txn.paymentMethod) {
                                    "TELEBIRR" -> Icons.Default.PhoneAndroid
                                    "CBE_BIRR" -> Icons.Default.AccountBalance
                                    "CASH" -> Icons.Default.AttachMoney
                                    "BANK_TRANSFER" -> Icons.Default.AccountBalance
                                    "MOBILE_MONEY" -> Icons.Default.PhoneAndroid
                                    else -> Icons.Default.ReceiptLong
                                },
                                contentDescription = null,
                                tint = when (txn.paymentMethod) {
                                    "TELEBIRR" -> Color(0xFF0F766E)
                                    "CBE_BIRR" -> Color(0xFF7E22CE)
                                    "CASH" -> ProfitGreen
                                    "BANK_TRANSFER" -> InfoBlue
                                    "MOBILE_MONEY" -> AmberPrimary
                                    else -> Slate700
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = txn.customerName,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Order #${txn.orderNumber} • ${txn.paymentMethod.replace('_', ' ')}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                            Text(
                                text = dateFormat.format(Date(txn.dateMillis)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.US, "+ETB %,.2f", txn.amount),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = ProfitGreen
                        )
                        if (txn.referenceNo.isNotEmpty()) {
                            Text(
                                text = txn.referenceNo,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                        }
                    }
                }
            }
        }
    }

    orderToCollect?.let { order ->
        CollectPaymentDialog(
            order = order,
            onDismiss = { orderToCollect = null },
            onConfirm = { amount, method, refNo, notes ->
                viewModel.recordOrderPayment(order.id, amount, method, refNo, notes)
            }
        )
    }
}
