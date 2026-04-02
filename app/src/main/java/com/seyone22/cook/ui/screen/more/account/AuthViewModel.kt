package com.seyone22.cook.ui.screen.more.account

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.update

// Create a simple sealed class for UI feedback
sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object Success : AuthUiState()
}

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // Expose UI state so the Settings page can show a Snackbar or Spinner
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val WEB_CLIENT_ID = "662029970133-7vhl0j7i70hnrie104r157ig44g6i217.apps.googleusercontent.com"

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
            if (firebaseAuth.currentUser == null) {
                auth.signInAnonymously()
            }
        }
    }

    fun launchCredentialManagerAuth(context: Context) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(context, request)
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                handleFirebaseSignIn(googleIdTokenCredential.idToken)
            } catch (e: GetCredentialException) {
                val errorMessage = when (e) {
                    is NoCredentialException -> "No Google accounts found. Add one in device settings."
                    else -> "Sign-in cancelled or failed."
                }
                _uiState.value = AuthUiState.Error(errorMessage)
            }
        }
    }

    private suspend fun handleFirebaseSignIn(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val user = auth.currentUser

        try {
            if (user != null && user.isAnonymous) {
                // Try to upgrade the ghost account
                user.linkWithCredential(credential).await()
                _uiState.value = AuthUiState.Success
            } else {
                auth.signInWithCredential(credential).await()
                _uiState.value = AuthUiState.Success
            }
        } catch (e: Exception) {
            if (e is FirebaseAuthUserCollisionException) {
                // COLLISION FIX: This Google account is already used elsewhere.
                // Instead of failing, just sign in to that existing account.
                Log.d("Auth", "Collision detected, switching to existing account.")
                auth.signInWithCredential(credential).await()
                _uiState.value = AuthUiState.Success
            } else {
                _uiState.value = AuthUiState.Error("Authentication failed: ${e.localizedMessage}")
            }
        }
    }

    fun resetUiState() { _uiState.value = AuthUiState.Idle }

    fun signOut() { auth.signOut() }
}