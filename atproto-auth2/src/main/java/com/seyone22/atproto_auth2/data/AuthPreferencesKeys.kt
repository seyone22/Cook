package com.seyone22.atproto_auth2.data

import androidx.datastore.preferences.core.stringPreferencesKey

// Keys for storing tokens
object AuthPreferencesKeys {
    val ACCESS_TOKEN = stringPreferencesKey("access_token")
    val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    val ID_TOKEN = stringPreferencesKey("id_token")
    val TOKEN_TYPE = stringPreferencesKey("token_type")
}