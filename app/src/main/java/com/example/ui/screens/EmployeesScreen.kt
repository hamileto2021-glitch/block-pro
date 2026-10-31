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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.AttendanceEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.WagePaymentEntity
import com.example.ui.components.AddEmployeeDialog
import com.example.ui.components.AttendanceBadge
import com.example.ui.components.RecordWagePayoutDialog
import com.example.ui.components.WageTypeChip
import com.example.ui.theme.AmberPrimary
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.BlocketViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmployeesScreen(
    viewModel: BlocketViewModel,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val attendanceRecords by viewModel.attendanceRecords.collectAsStateWithLifecycle()
    val wagePayments by viewModel.wagePayments.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Staff Directory", "Daily Attendance", "Wage Payouts")

    var showAddEmployeeDialog by remember { mutableStateOf(false) }
    var showWagePayoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("employees_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTabIndex == 2) showWagePayoutDialog = true
                    else showAddEmployeeDialog = true
                },
                containerColor = AmberPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_employee_action")
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedTabIndex == 2) "Pay Wage" else "Add Worker",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = AmberPrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Black else FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> StaffDirectoryTab(
                    employees = employees,
                    onPayWage = { showWagePayoutDialog = true }
                )
                1 -> AttendanceTab(
                    employees = employees,
                    attendanceRecords = attendanceRecords,
                    onMarkAttendance = { empId, status, ot, notes ->
                        viewModel.markAttendance(empId, status, ot, notes)
                    }
                )
                2 -> WagePayoutsTab(wagePayments = wagePayments)
            }
        }
    }

    if (showAddEmployeeDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddEmployeeDialog = false },
            onConfirm = { name, phone, role, wageType, rate ->
                viewModel.addEmployee(name, phone, role, wageType, rate)
            }
        )
    }

    if (showWagePayoutDialog) {
        RecordWagePayoutDialog(
            employees = employees,
            onDismiss = { showWagePayoutDialog = false },
            onConfirm = { empId, amount, type, method, notes ->
                viewModel.recordWagePayout(empId, amount, type, method, notes)
            }
        )
    }
}

@Composable
private fun StaffDirectoryTab(
    employees: List<EmployeeEntity>,
    onPayWage: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Blocket Plant Crew",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${employees.count { it.status == "ACTIVE" }} Active Operators, Mixers & Yard Labor",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    }

                    OutlinedButton(onClick = onPayWage) {
                        Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Disburse Pay", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(employees) { employee ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("employee_card_${employee.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AmberPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Engineering,
                                    contentDescription = null,
                                    tint = AmberPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = employee.fullName,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = employee.role,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate700
                                )
                            }
                        }

                        WageTypeChip(wageType = employee.wageType)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Slate700, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = employee.phone, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate700)
                        }

                        Text(
                            text = when (employee.wageType) {
                                "DAILY" -> String.format(Locale.US, "ETB %,.2f / day", employee.rateAmount)
                                "MONTHLY" -> String.format(Locale.US, "ETB %,.2f / mo", employee.rateAmount)
                                else -> String.format(Locale.US, "ETB %,.2f / 100 blocks", employee.rateAmount)
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = AmberPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceTab(
    employees: List<EmployeeEntity>,
    attendanceRecords: List<AttendanceEntity>,
    onMarkAttendance: (empId: Long, status: String, otHours: Double, notes: String) -> Unit
) {
    val todayFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    val todayDateStr = todayFormat.format(Date())

    val todayRecords = attendanceRecords.associateBy { it.employeeId }
    val presentCount = todayRecords.values.count { it.status == "PRESENT" || it.status == "OVERTIME" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = todayDateStr,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$presentCount / ${employees.size} Plant Workers Checked In Today",
                        fontSize = 13.sp,
                        color = ProfitGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(employees) { employee ->
            val record = todayRecords[employee.id]
            val currentStatus = record?.status ?: "NONE"

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = employee.fullName,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = employee.role,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700
                            )
                        }

                        AttendanceBadge(status = currentStatus)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Fast attendance status toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AttendanceToggleButton(
                            label = "Present",
                            isSelected = currentStatus == "PRESENT",
                            color = ProfitGreen,
                            onClick = { onMarkAttendance(employee.id, "PRESENT", 0.0, "Regular shift") },
                            modifier = Modifier.weight(1f)
                        )
                        AttendanceToggleButton(
                            label = "Half Day",
                            isSelected = currentStatus == "HALF_DAY",
                            color = WarningAmber,
                            onClick = { onMarkAttendance(employee.id, "HALF_DAY", 0.0, "Half shift") },
                            modifier = Modifier.weight(1f)
                        )
                        AttendanceToggleButton(
                            label = "+OT (2h)",
                            isSelected = currentStatus == "OVERTIME",
                            color = Color(0xFF6D28D9),
                            onClick = { onMarkAttendance(employee.id, "OVERTIME", 2.0, "Overtime evening batch") },
                            modifier = Modifier.weight(1f)
                        )
                        AttendanceToggleButton(
                            label = "Absent",
                            isSelected = currentStatus == "ABSENT",
                            color = ExpenseRed,
                            onClick = { onMarkAttendance(employee.id, "ABSENT", 0.0, "Absent") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceToggleButton(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) color else Slate200)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            color = if (isSelected) Color.White else Slate900
        )
    }
}

@Composable
private fun WagePayoutsTab(wagePayments: List<WagePaymentEntity>) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (wagePayments.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text("No wage disbursements recorded yet.", color = Slate500, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        items(wagePayments) { payment ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = payment.employeeName,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${payment.paymentType.replace('_', ' ')} • via ${payment.paymentMethod.replace('_', ' ')}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        Text(
                            text = dateFormat.format(Date(payment.dateMillis)),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.US, "ETB %,.2f", payment.amount),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = ExpenseRed
                        )
                        if (payment.notes.isNotEmpty()) {
                            Text(
                                text = payment.notes,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
