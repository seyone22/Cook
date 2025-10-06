package com.seyone22.cook.ui.common.dialog.action

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.seyone22.cook.data.DialogAction
import com.seyone22.cook.data.DialogStyle

class DeleteDialogAction(
    override val style: DialogStyle = DialogStyle.BASIC,
    override val modifier: Modifier = Modifier,
    private val itemName: String,
    private val onDelete: () -> Unit,
) : DialogAction {
    override val title: String = "Delete $itemName?"
    override val message: String =
        "Are you sure you want to delete $itemName? This action cannot be undone."

    override val content: @Composable () -> Unit = {}

    override fun onConfirm() {
        onDelete()
    }

    override fun onCancel() {}
}