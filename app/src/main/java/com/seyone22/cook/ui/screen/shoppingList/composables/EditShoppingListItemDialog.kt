package com.seyone22.cook.ui.screen.shoppingList.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
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
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.data.model.toShoppingList
import com.seyone22.cook.data.model.toShoppingListItemDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditShoppingListItemDialog(
    ingredients: List<Ingredient?>,
    measures: List<Measure?>,
    createNewIngredient: (String) -> Unit,
    onConfirm: (ShoppingListItem) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (ShoppingListItem) -> Unit,
    item: ShoppingListItem
) {
    var ingredientExpanded by remember { mutableStateOf(false) }
    var measuresExpanded by remember { mutableStateOf(false) }

    var shoppingListItemDetails by remember { mutableStateOf(item.toShoppingListItemDetails()) }

    AlertDialog(onDismissRequest = { onDismiss() }, title = { Text(text = "Edit Item") }, text = {
        var ingredientFilter by remember {
            mutableStateOf(
                ingredients.find { i -> i?.id == item.ingredientId }?.name ?: ""
            )
        }
        val filteredIngredients = ingredients.filter {
            (it?.name ?: "").contains(
                ingredientFilter, true
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                ExposedDropdownMenuBox(expanded = ingredientExpanded, onExpandedChange = {
                    ingredientExpanded = !ingredientExpanded
                }) {
                    OutlinedTextField(modifier = Modifier
                        .padding(
                            0.dp, 0.dp, 8.dp, 0.dp
                        )
                        .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                        .clickable(enabled = true) {
                            ingredientExpanded = true
                        },
                        value = ingredientFilter,
                        onValueChange = { v -> ingredientFilter = v },
                        label = { Text("") },
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = ingredientExpanded)
                        })

                    ExposedDropdownMenu(expanded = ingredientExpanded, onDismissRequest = { }) {
                        if (filteredIngredients.isNotEmpty()) {
                            filteredIngredients.forEach { ingredient ->
                                ingredient?.let {
                                    DropdownMenuItem(text = { Text(ingredient.name) }, onClick = {
                                        ingredientFilter = ingredient.name
                                        shoppingListItemDetails =
                                            shoppingListItemDetails.copy(ingredientId = ingredient.id)
                                        ingredientExpanded = false
                                    })
                                }
                            }
                        } else {
                            DropdownMenuItem(text = { Text("Add $ingredientFilter to database") },
                                onClick = {
                                    createNewIngredient("Add Ingredient/$ingredientFilter")
                                })
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .width(120.dp)
                        .padding(0.dp, 0.dp, 8.dp, 0.dp),
                    value = shoppingListItemDetails.quantity,
                    singleLine = true,
                    onValueChange = { newQty ->
                        shoppingListItemDetails =
                            shoppingListItemDetails.copy(quantity = newQty.filter { i -> i.isDigit() })
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                    )
                )
                ExposedDropdownMenuBox(expanded = measuresExpanded, onExpandedChange = {
                    measuresExpanded = !measuresExpanded
                }) {
                    OutlinedTextField(modifier = Modifier
                        .padding(
                            0.dp, 0.dp, 8.dp, 0.dp
                        )
                        .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                        .clickable(enabled = true) {
                            measuresExpanded = true
                        },
                        value = measures.find { m -> m?.id?.toInt() == shoppingListItemDetails.measureId.toInt() }?.abbreviation
                            ?: "",
                        readOnly = true,
                        onValueChange = { },
                        label = { Text("Unit") },
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = measuresExpanded)
                        })

                    ExposedDropdownMenu(expanded = measuresExpanded,
                        onDismissRequest = { measuresExpanded = false }) {
                        measures.forEach { measure ->
                            measure?.let {
                                DropdownMenuItem(text = { Text(measure.abbreviation) }, onClick = {
                                    shoppingListItemDetails =
                                        shoppingListItemDetails.copy(measureId = measure.id)
                                    measuresExpanded = false
                                })
                            }
                        }
                    }
                }
            }
            Row {
                TextButton(onClick = { onDelete(shoppingListItemDetails.toShoppingList()) }) {
                    Text("Delete")
                }
            }
        }
    }, confirmButton = {
        TextButton(onClick = { onConfirm(shoppingListItemDetails.toShoppingList()) }) {
            Text("Add")
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    })
}