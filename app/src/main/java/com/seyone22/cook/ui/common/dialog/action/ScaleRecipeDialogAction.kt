package com.seyone22.cook.ui.common.dialog.action

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.setValue
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
    override val title: String = "Scale Recipe by:"
    override val message: String = ""

    private var scaleFactor by mutableDoubleStateOf(
        initialEntry ?: 1.0
    )

    override val content: @Composable () -> Unit = {
        TextField(
            value = scaleFactor.toString(),
            onValueChange = { sF -> scaleFactor = sF.toDoubleOrNull() ?: 1.0 },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            label = { Text("People count") })
    }

    override fun onConfirm() {
        onAdd(scaleFactor)
    }

    override fun onCancel() {

    }
}