package com.ashok.holidayplanner.data.remote.dto

import com.google.gson.annotations.SerializedName

data class HolidayDto(
    @SerializedName("date") val date: String?,
    @SerializedName("localName") val localName: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("countryCode") val countryCode: String?,
    @SerializedName("fixed") val fixed: Boolean? = false,
    @SerializedName("global") val global: Boolean? = false,
    @SerializedName("counties") val counties: List<String>? = null,
    @SerializedName("launchYear") val launchYear: Int? = null,
    @SerializedName("types") val types: List<String>? = null
)
