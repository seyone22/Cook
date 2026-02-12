package com.seyone22.cook.ui.screen.shoppingList.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
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
import com.seyone22.cook.helper.DateTimeUtil.toIsoString
import java.time.LocalDateTime

@Composable
fun NewShoppingListDialog(
    onConfirm: (ShoppingList) -> Unit, onDismiss: () -> Unit
) {
    var shoppingList by remember {
        mutableStateOf(
            ShoppingList(
                name = "",
                dateCreated = LocalDateTime.now().toIsoString(),
                dateModified = LocalDateTime.now().toIsoString()
            )
        )
    }
    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text(text = "Create a Shopping List") },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.width(310.dp),
                    value = shoppingList.name,
                    onValueChange = { newName ->
                        shoppingList = shoppingList.copy(name = newName)
                    },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onConfirm(shoppingList) })
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(shoppingList) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        })
}