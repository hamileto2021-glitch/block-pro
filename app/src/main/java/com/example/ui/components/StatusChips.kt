package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.InfoBlueContainer
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenContainer
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberContainer

@Composable
fun PaymentStatusChip(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "PAID" -> Triple(ProfitGreenContainer, ProfitGreen, "PAID")
        "PARTIAL" -> Triple(WarningAmberContainer, WarningAmber, "PARTIAL")
        else -> Triple(ExpenseRedContainer, ExpenseRed, "UNPAID")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun DeliveryStatusChip(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "DELIVERED" -> Triple(ProfitGreenContainer, ProfitGreen, "DELIVERED")
        "DISPATCHED" -> Triple(InfoBlueContainer, InfoBlue, "DISPATCHED")
        else -> Triple(Slate200, Slate700, "PENDING")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun WageTypeChip(wageType: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (wageType.uppercase()) {
        "DAILY" -> Triple(Color(0xFFE0E7FF), Color(0xFF3730A3), "Daily Wage")
        "MONTHLY" -> Triple(Color(0xFFF3E8FF), Color(0xFF6B21A8), "Monthly Salary")
        "PIECE_RATE" -> Triple(Color(0xFFFFEDD5), Color(0xFF9A3412), "Piece Rate")
        else -> Triple(Slate200, Slate700, wageType)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun AttendanceBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "PRESENT" -> Triple(ProfitGreenContainer, ProfitGreen, "Present")
        "HALF_DAY" -> Triple(WarningAmberContainer, WarningAmber, "Half Day")
        "OVERTIME" -> Triple(Color(0xFFEDE9FE), Color(0xFF5B21B6), "Overtime")
        "ABSENT" -> Triple(ExpenseRedContainer, ExpenseRed, "Absent")
        else -> Triple(Slate200, Slate700, "Not Marked")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
