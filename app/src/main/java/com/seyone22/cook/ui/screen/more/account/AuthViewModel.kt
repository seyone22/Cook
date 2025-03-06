package com.seyone22.cook.ui.screen.more.account

import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.atproto_auth2.AtProtoAuthManager
import com.seyone22.atproto_auth2.client
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val authManager = AtProtoAuthManager(client, "https://madrilenyer.neocities.org/bsky/oauth/client-metadata.json")

    // Use MutableStateFlow for state management
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    fun startAuth() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val (requestUri, nonce) = authManager.startAuthFlow()
                // Handle requestUri and nonce, then navigate to the WebView
                // to initiate the authorization flow
                _authState.value = AuthState.Success(requestUri)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun handleAuthCallback(code: String, state: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                val accessToken = authManager.handleAuthorizationCallback(code, state)
                // Store accessToken and update UI
                _authState.value = AuthState.Success(accessToken)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val result: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
