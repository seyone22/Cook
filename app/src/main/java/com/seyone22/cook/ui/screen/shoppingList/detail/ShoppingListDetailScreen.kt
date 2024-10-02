package com.seyone22.cook.ui.screen.shoppingList.detail

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.ShoppingList
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.data.model.ShoppingListItemDetails
import com.seyone22.cook.data.model.toShoppingList
import com.seyone22.cook.data.model.toShoppingListItemDetails
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.CookFAB
import com.seyone22.cook.ui.common.CookTopBar
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.ingredients.detail.IngredientDetailDestination
import com.seyone22.cook.ui.screen.shoppingList.ShoppingListViewModel

object ShoppingListDetailDestination : NavigationDestination {
    override val route = "Shopping List Details"
    override val titleRes = R.string.app_name
    override val routeId = 21
}

@Composable
fun ShoppingListDetailScreen(
    viewModel: ShoppingListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    backStackEntry: String,
    navController: NavController,
    context: Context = LocalContext.current
) {
    viewModel.fetchData()

    val data by viewModel.shoppingListViewState.collectAsState()
    val items = data.shoppingListItems.filter { it?.shoppingListId == backStackEntry.toLong() }

    var showNewDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    if (showNewDialog) {
        NewShoppingListItemDialog(onConfirm = {
            viewModel.addToShoppingList(it.copy(shoppingListId = backStackEntry.toLong()))
            viewModel.fetchData()
            showNewDialog = false
        },
            onDismiss = { showNewDialog = false },
            ingredients = data.ingredients,
            measures = data.measures,
            createNewIngredient = { uri -> navController.navigate(uri) })
    }

    if (showRenameDialog) {
        RenameDialog(onConfirm = { renamedShoppingList ->
            viewModel.renameShoppingList(renamedShoppingList)
            viewModel.fetchData()
            showRenameDialog = false
        },
            onDismiss = { showRenameDialog = false },
            shoppingList = data.shoppingLists.find { it -> it?.id == backStackEntry.toLong() }
                ?: ShoppingList())
    }

    if (showDeleteConfirmationDialog) {
        DeleteConfirmationDialog(onConfirm = {
            viewModel.deleteShoppingList(data.shoppingLists.find { it -> it?.id == backStackEntry.toLong() }
                ?: ShoppingList())
            showDeleteConfirmationDialog = false
            navController.popBackStack()
        }, onDismiss = { showDeleteConfirmationDialog = false })
    }

    Scaffold(topBar = {
        CookTopBar(currentActivity = ShoppingListDetailDestination.route,
            navController = navController,
            title = data.shoppingLists.find { it?.id == backStackEntry.toLong() }?.name ?: "",
            activityType = { activity ->
                when (activity) {
                    "delete" -> {
                        showDeleteConfirmationDialog = true
                    }

                    "rename" -> {
                        showRenameDialog = true
                    }

                    "complete" -> {
                        viewModel.completeShoppingList(data.shoppingLists.find { it -> it?.id == backStackEntry.toLong() }
                            ?: ShoppingList())
                        navController.popBackStack()
                    }
                }
            })
    }, floatingActionButton = {
        CookFAB(currentActivity = ShoppingListDetailDestination.route,
            action = { showNewDialog = true })
    }) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items.forEach { item ->
                item {
                    if (item !== null) {
                        ShoppingItemList(
                            item = item,
                            ingredients = data.ingredients,
                            measures = data.measures,
                            navController = navController,
                            viewModel = viewModel,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShoppingItemList(
    item: ShoppingListItem,
    ingredients: List<Ingredient?>,
    measures: List<Measure?>,
    navController: NavController,
    viewModel: ShoppingListViewModel,
) {
    var checked by remember { mutableStateOf(item.checked) }

    var showEditDialog by remember { mutableStateOf(false) }

    if (showEditDialog) {
        EditShoppingListItemDialog(onConfirm = {
            viewModel.addToShoppingList(it.copy(shoppingListId = item.id))
            viewModel.fetchData()
            showEditDialog = false
        },
            onDismiss = { showEditDialog = false },
            ingredients = ingredients,
            measures = measures,
            createNewIngredient = { uri -> navController.navigate(uri) },
            item = item,
            onDelete = {
                viewModel.removeFromShoppingList(it)
                viewModel.fetchData()
                showEditDialog = false
            })
    }

    ListItem(modifier = Modifier
        .padding(0.dp)
        .combinedClickable(enabled = true, onLongClick = {
            showEditDialog = true
        }, onClick = {
            navController.navigate("${IngredientDetailDestination.route}/${item.ingredientId}")
        }), leadingContent = {
        Checkbox(modifier = Modifier.height(32.dp),
            enabled = true,
            checked = checked,
            onCheckedChange = {
                checked = !checked
                viewModel.changePurchaseStatus(item)
            })
    }, headlineContent = {
        Text(
            modifier = Modifier
                .padding(4.dp, 0.dp, 16.dp, 0.dp)
                .width(120.dp),
            text = ingredients.find { i -> i?.id == item.ingredientId }?.nameEn ?: "",
            style = MaterialTheme.typography.bodyLarge.copy(
                textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (checked) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }, trailingContent = {
        Text(
            modifier = Modifier.padding(4.dp, 0.dp, 16.dp, 0.dp),
            text = "${
                String.format("%.2f", item.quantity)
            } ${measures.find { m -> m?.id == item.measureId }?.abbreviation}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewShoppingListItemDialog(
    ingredients: List<Ingredient?>,
    measures: List<Measure?>,
    createNewIngredient: (String) -> Unit,
    onConfirm: (ShoppingListItem) -> Unit,
    onDismiss: () -> Unit
) {
    var ingredientExpanded by remember { mutableStateOf(false) }
    var measuresExpanded by remember { mutableStateOf(false) }

    var shoppingListItemDetails by remember { mutableStateOf(ShoppingListItemDetails()) }

    AlertDialog(onDismissRequest = { onDismiss() }, title = { Text(text = "Add Item") }, text = {
        var ingredientFilter by remember { mutableStateOf("") }
        val filteredIngredients = ingredients.filter {
            (it?.nameEn ?: "").contains(
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
                                    DropdownMenuItem(text = { Text(ingredient.nameEn) }, onClick = {
                                        ingredientFilter = ingredient.nameEn
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
                ingredients.find { i -> i?.id == item.ingredientId }?.nameEn ?: ""
            )
        }
        val filteredIngredients = ingredients.filter {
            (it?.nameEn ?: "").contains(
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
                                    DropdownMenuItem(text = { Text(ingredient.nameEn) }, onClick = {
                                        ingredientFilter = ingredient.nameEn
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