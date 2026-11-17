package com.ashok.holidayplanner.domain.model

import com.ashok.holidayplanner.data.local.entities.LeavePlan
import com.ashok.holidayplanner.util.DateUtils

data class LeaveWithHolidays(
    val leavePlan: LeavePlan,
    val includedHolidays: List<Holiday> = emptyList()
) {
    val durationDays: Long
        get() = DateUtils.calculateInclusiveDays(leavePlan.startDate, leavePlan.endDate)

    val holidaysSummaryText: String
        get() = when (includedHolidays.size) {
            0 -> "No public holidays included"
            1 -> {
                val h = includedHolidays.first()
                "Includes 1 public holiday: ${h.localName.ifBlank { h.name }}"
            }
            else -> {
                val names = includedHolidays.joinToString(", ") { it.localName.ifBlank { it.name } }
                "Includes ${includedHolidays.size} public holidays: $names"
            }
        }
}
