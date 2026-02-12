package com.seyone22.atproto_auth2.data.auth

data class CallbackParams(val iss: String, val state: String, val code: String)