package com.seyone22.atproto_auth2.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.seyone22.atproto_auth2.data.AuthPreferencesKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


// Define DataStore Name
private val Context.dataStore by preferencesDataStore(name = "auth_tokens")

// DataStore Helper Class
class AuthTokenManager(private val context: Context) {

    // Save tokens securely
    suspend fun saveTokens(accessToken: String, refreshToken: String, tokenType: String?) {
        context.dataStore.edit { prefs ->
            prefs[AuthPreferencesKeys.ACCESS_TOKEN] = accessToken
            prefs[AuthPreferencesKeys.REFRESH_TOKEN] = refreshToken
            if (tokenType != null) prefs[AuthPreferencesKeys.TOKEN_TYPE] = tokenType
        }
    }

    // Retrieve Access Token
    suspend fun getAccessToken(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[AuthPreferencesKeys.ACCESS_TOKEN]
        }.first()
    }

    // Retrieve Refresh Token
    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[AuthPreferencesKeys.REFRESH_TOKEN]
        }.first()
    }

    // Retrieve Token Type
    suspend fun getTokenType(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[AuthPreferencesKeys.TOKEN_TYPE]
        }.first()
    }

    // Clear All Tokens
    suspend fun clearTokens() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
