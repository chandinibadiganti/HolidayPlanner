package com.ashok.holidayplanner.ui.common

sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val canRetry: Boolean = true) : UiState<Nothing>
    object Empty : UiState<Nothing>
}
