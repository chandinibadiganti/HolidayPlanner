package com.ashok.holidayplanner.domain.model

data class Country(
    val code: String,
    val name: String
) {
    override fun toString(): String = name
}
