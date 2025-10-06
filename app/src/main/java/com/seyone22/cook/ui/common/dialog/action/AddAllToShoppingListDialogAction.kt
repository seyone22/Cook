package com.seyone22.cook.ui.common.dialog.action

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seyone22.cook.data.DialogAction
import com.seyone22.cook.data.DialogStyle
import com.seyone22.cook.data.model.ShoppingList

class AddAllToShoppingListDialogAction(
    override val style: DialogStyle = DialogStyle.BASIC,
    override val modifier: Modifier = Modifier,
    private val itemName: String,
    private val onAdd: (Int) -> Unit,

    shoppingLists: List<ShoppingList?>
) : DialogAction {
    override val title: String = "Add to a Shopping List"
    override val message: String = ""

    private var selectedShoppingListIndex by mutableStateOf(0)

    @OptIn(ExperimentalMaterial3Api::class)
    override val content: @Composable () -> Unit = {
        var shoppingListExpanded by remember { mutableStateOf(false) }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                ExposedDropdownMenuBox(expanded = shoppingListExpanded, onExpandedChange = {
                    shoppingListExpanded = !shoppingListExpanded
                }) {
                    OutlinedTextField(
                        modifier = Modifier
                            .padding(
                                0.dp, 0.dp, 8.dp, 0.dp
                            )
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                            .clickable(enabled = true) {
                                shoppingListExpanded = true
                            },
                        value = if (shoppingLists.isNotEmpty()) {
                            shoppingLists[selectedShoppingListIndex]?.name ?: ""
                        } else {
                            ""
                        },
                        readOnly = true,
                        onValueChange = { },
                        label = { Text("") },
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = shoppingListExpanded)
                        })

                    ExposedDropdownMenu(
                        expanded = shoppingListExpanded,
                        onDismissRequest = { shoppingListExpanded = false }) {
                        shoppingLists.forEachIndexed { index, shoppingList ->
                            shoppingList?.let {
                                DropdownMenuItem(text = { Text(shoppingList.name) }, onClick = {
                                    selectedShoppingListIndex = index
                                    shoppingListExpanded = false
                                })
                            }
                        }
                    }
                }
                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                    IconButton(
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp),
                        onClick = { /*TODO*/ },
                        content = {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                            )
                        })
                }
            }
        }
    }

    override fun onConfirm() {
        //onAdd(            shoppingLists[selectedShoppingListIndex]?.id ?: -1,)
    }

    override fun onCancel() {
        TODO("Not yet implemented")
    }
}