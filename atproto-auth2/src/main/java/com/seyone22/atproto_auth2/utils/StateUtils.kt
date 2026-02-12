package com.seyone22.atproto_auth2.utils

fun verifyState(expectedState: String, actualState: String): Boolean {
    return expectedState == actualState
}