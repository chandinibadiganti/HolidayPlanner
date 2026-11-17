package com.ashok.holidayplanner.domain.model

import java.time.LocalDate

data class Holiday(
    val date: LocalDate,
    val localName: String,
    val name: String,
    val countryCode: String,
    val fixed: Boolean = false,
    val global: Boolean = false,
    val counties: List<String>? = null,
    val launchYear: Int? = null,
    val types: List<String>? = null,
    val isLongWeekend: Boolean = false,
    val longWeekendSpan: String? = null,
    val isOverlappingLeave: Boolean = false,
    val overlappingLeaveTitles: List<String> = emptyList(),
    val bridgeSuggestion: Pair<LocalDate, LocalDate>? = null
)
