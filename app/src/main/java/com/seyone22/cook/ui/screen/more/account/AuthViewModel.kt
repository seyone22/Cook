package com.seyone22.cook.ui.screen.more.account

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.atproto_auth2.AtProtoAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val userHandle = "akimoriss.bsky.social"
    private val authManager = AtProtoAuthManager(userHandle = userHandle)

    // Use MutableStateFlow for state management
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    private var redirectURI: String? = null

    fun startAuth() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                redirectURI =
                    authManager.fetchRedirectURI()  // Assuming this fetches the correct URI
                _authState.value = AuthState.Success(redirectURI ?: "")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun handleOAuthResult(code: String?, state: String?, iss: String?) {
        if (code != null) {
            Log.d("OAuth", "Auth successful: Code=$code, State=$state, Iss=$iss")
            _authState.value = AuthState.Authenticated(code, state, iss)

            viewModelScope.launch {
                try {
                    authManager.requestTokenDPoP(code)
                } catch (e: Exception) {
                    Log.e("OAuth", "Failed to request token: ${e.message}")
                }
            }

        } else {
            Log.e("OAuth", "OAuth failed")
            _authState.value = AuthState.Error("Failed to authenticate")
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val result: String) : AuthState()
    data class Authenticated(val code: String?, val state: String?, val iss: String?) : AuthState()
    data class Error(val message: String) : AuthState()
}
