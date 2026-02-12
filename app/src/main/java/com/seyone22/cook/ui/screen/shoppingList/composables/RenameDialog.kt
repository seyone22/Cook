package com.seyone22.cook.ui.screen.shoppingList.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.seyone22.cook.data.model.ShoppingList

@Composable
fun RenameDialog(
    shoppingList: ShoppingList, onConfirm: (ShoppingList) -> Unit, onDismiss: () -> Unit
) {
    var newName by remember { mutableStateOf(shoppingList.name) }

    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text(text = "Rename Shopping List") },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 16.dp),
                    value = newName,
                    singleLine = true,
                    onValueChange = { n -> newName = n },
                    label = { Text("Name") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next, keyboardType = KeyboardType.Text
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(shoppingList.copy(name = newName)) }) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        })
}