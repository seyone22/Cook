package com.seyone22.cook.ui.common.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.seyone22.cook.data.DialogAction
import com.seyone22.cook.data.DialogStyle

@Composable
fun GenericDialog(
    dialogAction: DialogAction, onDismiss: () -> Unit
) {
    if (dialogAction.style == DialogStyle.FULLSCREEN) {
        val showPopup = remember { mutableStateOf(true) }

        if (showPopup.value) {
            Popup(
                properties = PopupProperties(focusable = true, dismissOnBackPress = true),
                onDismissRequest = {
                    showPopup.value = false
                    onDismiss()
                }) {
                dialogAction.content()
            }
        }

    } else {
        AlertDialog(modifier = dialogAction.modifier, onDismissRequest = onDismiss, title = {
            if (dialogAction.title != null) {
                Text(text = dialogAction.title!!)
            }
        }, text = {
            Column {
                // Only render the message if it's not null or empty
                dialogAction.message?.let { message ->
                    Text(text = message)
                }
                dialogAction.content() // Render the dynamic content here
            }
        }, confirmButton = {
            Button(
                onClick = {
                    dialogAction.onConfirm()
                    onDismiss()
                }) {
                Text("Confirm")
            }
        }, dismissButton = {
            Button(
                onClick = {
                    dialogAction.onCancel()
                    onDismiss()
                }) {
                Text("Cancel")
            }
        })
    }
}
