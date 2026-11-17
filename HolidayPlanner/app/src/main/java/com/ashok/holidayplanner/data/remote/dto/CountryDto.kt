package com.ashok.holidayplanner.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CountryDto(
    @SerializedName("countryCode") val countryCode: String?,
    @SerializedName("name") val name: String?
)
