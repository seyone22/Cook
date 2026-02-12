package com.seyone22.cook.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface DialogAction {
    val modifier: Modifier // Modifier for the dialog
    val style: DialogStyle // Style for the dialog
    val title: String?
    val message: String?
    val content: @Composable () -> Unit // Content for the body of the dialog
    fun onConfirm() // Action when user confirms
    fun onCancel() // Action when user cancels
}

enum class DialogStyle {
    BASIC, FULLSCREEN
}