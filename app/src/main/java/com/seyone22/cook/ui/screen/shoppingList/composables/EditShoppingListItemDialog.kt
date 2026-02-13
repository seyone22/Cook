import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.seyone22.cook.ui.screen.shoppingList.ShoppingListViewModel

@Composable
fun EditItemDialog(
    item: ShoppingListViewModel.ShoppingItemDisplay,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit // Returns new Qty and Unit Name
) {
    var quantity by remember { mutableStateOf(item.item.quantity.toString()) }
    // In a real app, this would be a Dropdown of units. For now, text is fine.
    var unit by remember { mutableStateOf(item.measureName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${item.ingredientName}") },
        text = {
            Column {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (e.g., kg, pcs)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val qty = quantity.toDoubleOrNull() ?: item.item.quantity
                onConfirm(qty, unit)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        })
}