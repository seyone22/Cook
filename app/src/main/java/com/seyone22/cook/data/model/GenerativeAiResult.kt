package com.seyone22.cook.data.model

sealed class GenerativeAiResult<out T> {
    data class Success<T>(val data: T) : GenerativeAiResult<T>()
    data class Error(val message: String) : GenerativeAiResult<Nothing>()
}