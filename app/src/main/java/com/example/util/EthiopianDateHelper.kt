package com.example.util

import java.util.Calendar

object EthiopianDateHelper {

    private val amharicMonths = listOf(
        "መስከረም", "ጥቅምት", "ኅዳር", "ታኅሣሥ", "ጥር", "የካቲት",
        "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰኔ", "ሐምሌ", "ነሐሴ", "ጳጉሜ"
    )

    private val englishMonths = listOf(
        "Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yakatit",
        "Magabit", "Miyazya", "Ginbot", "Sene", "Hamle", "Nehase", "Pagume"
    )

    /**
     * Formats Gregorian timestamp into formatted Ethiopian Calendar date string.
     * E.g. "ጥቅምት 21, 2019 ዓ.ም (Tikimt 21, 2019 E.C.)"
     */
    fun formatEthiopianDate(millis: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) // 0-based: Jan=0, Oct=9
        val day = cal.get(Calendar.DAY_OF_MONTH)

        val ethYear = if (month > Calendar.SEPTEMBER || (month == Calendar.SEPTEMBER && day >= 11)) {
            year - 7
        } else {
            year - 8
        }

        val (ethMonthIdx, ethDay) = when (month) {
            Calendar.JANUARY -> if (day <= 8) Pair(3, day + 22) else Pair(4, day - 8)
            Calendar.FEBRUARY -> if (day <= 7) Pair(4, day + 23) else Pair(5, day - 7)
            Calendar.MARCH -> if (day <= 9) Pair(5, day + 21) else Pair(6, day - 9)
            Calendar.APRIL -> if (day <= 8) Pair(6, day + 22) else Pair(7, day - 8)
            Calendar.MAY -> if (day <= 8) Pair(7, day + 22) else Pair(8, day - 8)
            Calendar.JUNE -> if (day <= 7) Pair(8, day + 23) else Pair(9, day - 7)
            Calendar.JULY -> if (day <= 7) Pair(9, day + 23) else Pair(10, day - 7)
            Calendar.AUGUST -> if (day <= 6) Pair(10, day + 24) else Pair(11, day - 6)
            Calendar.SEPTEMBER -> when {
                day <= 5 -> Pair(11, day + 25)
                day <= 10 -> Pair(12, day - 5) // Pagume
                else -> Pair(0, day - 10) // Meskerem
            }
            Calendar.OCTOBER -> if (day <= 10) Pair(0, day + 20) else Pair(1, day - 10) // Tikimt
            Calendar.NOVEMBER -> if (day <= 9) Pair(1, day + 21) else Pair(2, day - 9) // Hidar
            Calendar.DECEMBER -> if (day <= 9) Pair(2, day + 21) else Pair(3, day - 9) // Tahsas
            else -> Pair(1, day)
        }

        val safeMonthIdx = ethMonthIdx.coerceIn(0, 12)
        val amharicMonth = amharicMonths[safeMonthIdx]
        val engMonth = englishMonths[safeMonthIdx]
        return "$amharicMonth $ethDay, $ethYear ዓ.ም ($engMonth $ethDay, $ethYear E.C.)"
    }
}
