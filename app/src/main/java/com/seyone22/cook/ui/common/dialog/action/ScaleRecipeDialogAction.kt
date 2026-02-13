package com.seyone22.cook.ui.common.dialog.action

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.seyone22.cook.data.DialogAction
import com.seyone22.cook.data.DialogStyle

class ScaleRecipeDialogAction(
    override val style: DialogStyle = DialogStyle.BASIC,
    override val modifier: Modifier = Modifier,
    private val itemName: String,
    private val onAdd: (Double) -> Unit,
    initialEntry: Double? = 1.0
) : DialogAction {
    override val title: String = "Scale Recipe"
    override val message: String = "Adjust the serving size for $itemName"

    // 1. Store state as a String to allow for empty inputs and better typing
    // 2. Format the initial Double to remove ".0" if it's a whole number
    private var textFieldValue by mutableStateOf(
        initialEntry?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: ""
    )

    override val content: @Composable () -> Unit = {
        TextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                // Only allow numbers and a single decimal point
                if (newValue.isEmpty() || newValue.toDoubleOrNull() != null || newValue == ".") {
                    textFieldValue = newValue
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("Number of Servings") },
            singleLine = true
        )
    }

    override fun onConfirm() {
        // Fallback to 1.0 only at the moment of confirmation
        val finalScale = textFieldValue.toDoubleOrNull() ?: 1.0
        onAdd(finalScale)
    }

    override fun onCancel() {}
}