package com.ashok.holidayplanner.data.repository

import com.ashok.holidayplanner.data.prefs.SettingsStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val settingsStore: SettingsStore
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun isSignedIn(): Boolean {
        return auth.currentUser != null || settingsStore.isGuestMode()
    }

    fun isGuest(): Boolean {
        return settingsStore.isGuestMode()
    }

    fun getUserId(): String {
        return auth.currentUser?.uid ?: if (settingsStore.isGuestMode()) "guest_user_id" else "anonymous"
    }

    fun getUserDisplayName(): String {
        return auth.currentUser?.displayName
            ?: if (settingsStore.isGuestMode()) settingsStore.getGuestName() else "Explorer"
    }

    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun getUserPhotoUrl(): String? {
        return auth.currentUser?.photoUrl?.toString()
    }

    suspend fun signInWithGoogleIdToken(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        settingsStore.setGuestMode(false)
        return authResult.user
    }

    fun continueAsGuest(name: String = "Guest User") {
        settingsStore.setGuestMode(true, name)
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (ignored: Exception) {
        }
        settingsStore.setGuestMode(false)
    }
}
