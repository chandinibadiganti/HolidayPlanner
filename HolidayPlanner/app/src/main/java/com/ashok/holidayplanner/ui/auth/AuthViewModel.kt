package com.ashok.holidayplanner.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ashok.holidayplanner.data.repository.AuthRepository
import com.ashok.holidayplanner.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<Unit>>(UiState.Empty)
    val authState: StateFlow<UiState<Unit>> = _authState.asStateFlow()

    fun isUserSignedIn(): Boolean {
        return authRepository.isSignedIn()
    }

    fun getUserDisplayName(): String {
        return authRepository.getUserDisplayName()
    }

    fun getUserEmail(): String? {
        return authRepository.getUserEmail()
    }

    fun getUserPhotoUrl(): String? {
        return authRepository.getUserPhotoUrl()
    }

    fun isGuest(): Boolean {
        return authRepository.isGuest()
    }

    fun handleGoogleIdToken(idToken: String) {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                authRepository.signInWithGoogleIdToken(idToken)
                _authState.value = UiState.Success(Unit)
            } catch (e: Exception) {
                _authState.value = UiState.Error(
                    message = e.localizedMessage ?: "Failed to authenticate with Firebase using Google ID token."
                )
            }
        }
    }

    fun continueAsGuest(name: String = "Guest User") {
        authRepository.continueAsGuest(name)
        _authState.value = UiState.Success(Unit)
    }

    fun signOut() {
        authRepository.signOut()
        _authState.value = UiState.Empty
    }

    fun resetState() {
        _authState.value = UiState.Empty
    }
}
