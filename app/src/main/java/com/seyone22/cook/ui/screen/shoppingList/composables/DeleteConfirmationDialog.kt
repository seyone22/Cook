package com.seyone22.cook.ui.screen.shoppingList.composables

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text(text = "Are you sure you want to delete?") },
        text = {},
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        })
}