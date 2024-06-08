package com.seyone22.cook.ui.screen.shoppingList.detail

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.data.model.ShoppingListItemDetails
import com.seyone22.cook.data.model.toShoppingList
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

@OptIn(ExperimentalMaterial3Api::class)
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
    if (showNewDialog) {
        NewShoppingListItemDialog(
            onConfirm = {
                viewModel.addToShoppingList(it.copy(shoppingListId = backStackEntry.toLong()))
                viewModel.fetchData()
                showNewDialog = false
            },
            onDismiss = { showNewDialog = false },
            ingredients = data.ingredients,
            measures = data.measures
        )
    }

    Scaffold(topBar = {
        CookTopBar(
            currentActivity = ShoppingListDetailDestination.route,
            navController = navController,
            title = data.shoppingLists.find { it?.id == backStackEntry.toLong() }?.name ?: ""
        )
    }, floatingActionButton = {
        CookFAB(currentActivity = ShoppingListDetailDestination.route,
            action = { showNewDialog = true })
    }) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items.forEach { item ->
                item {
                    ShoppingItemList(
                        item = item,
                        ingredients = data.ingredients,
                        measures = data.measures,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun ShoppingItemList(
    item: ShoppingListItem?,
    ingredients: List<Ingredient?>,
    measures: List<Measure?>,
    navController: NavController
) {
    Row(
        modifier = Modifier.padding(0.dp),
    ) {
        val checked = remember {
            mutableStateOf(
                ingredients.find { i -> i?.id == item?.ingredientId }?.stocked ?: false
            )
        }

        Checkbox(modifier = Modifier.height(32.dp),
            enabled = !(ingredients.find { i -> i?.id == item?.ingredientId }?.stocked ?: false),
            checked = checked.value,
            onCheckedChange = { checked.value = !checked.value })
        Text(
            modifier = Modifier
                .padding(4.dp, 0.dp, 16.dp, 0.dp)
                .align(Alignment.CenterVertically)
                .width(120.dp)
                .clickable {
                    navController.navigate("${IngredientDetailDestination.route}/${item?.ingredientId}")
                },
            text = ingredients.find { i -> i?.id == item?.ingredientId }?.nameEn ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            modifier = Modifier
                .padding(4.dp, 0.dp, 16.dp, 0.dp)
                .align(Alignment.CenterVertically),
            text = "${
                String.format("%.2f", item?.quantity)
            } ${measures.find { m -> m?.id == item?.measureId }?.abbreviation}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewShoppingListItemDialog(
    ingredients: List<Ingredient?>,
    measures: List<Measure?>,
    onConfirm: (ShoppingListItem) -> Unit,
    onDismiss: () -> Unit
) {
    var ingredientExpanded by remember { mutableStateOf(false) }
    var measuresExpanded by remember { mutableStateOf(false) }

    var shoppingListItemDetails by remember { mutableStateOf(ShoppingListItemDetails()) }

    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text(text = "Add Ingredient") },
        text = {
            Column {
                Row {
                    ExposedDropdownMenuBox(expanded = ingredientExpanded, onExpandedChange = {
                        ingredientExpanded = !ingredientExpanded
                    }) {
                        OutlinedTextField(modifier = Modifier
                            .padding(0.dp, 0.dp, 8.dp, 0.dp)
                            .width(156.dp)
                            .menuAnchor()
                            .clickable(enabled = true) {
                                ingredientExpanded = true
                            },
                            value = ingredients.find { m -> m?.id?.toInt() == shoppingListItemDetails.ingredientId.toInt() }?.nameEn
                                ?: "",
                            readOnly = true,
                            onValueChange = { },
                            label = { Text("") },
                            singleLine = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = ingredientExpanded)
                            })

                        ExposedDropdownMenu(expanded = ingredientExpanded,
                            onDismissRequest = { ingredientExpanded = false }) {
                            ingredients.forEach { ingredient ->
                                ingredient?.let {
                                    DropdownMenuItem(text = { Text(ingredient.nameEn) }, onClick = {
                                        shoppingListItemDetails =
                                            shoppingListItemDetails.copy(ingredientId = ingredient.id)
                                        ingredientExpanded = false
                                    })
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        modifier = Modifier
                            .width(64.dp)
                            .padding(0.dp, 0.dp, 8.dp, 0.dp),
                        value = shoppingListItemDetails.quantity,
                        singleLine = true,
                        onValueChange = { newQty ->
                            shoppingListItemDetails =
                                shoppingListItemDetails.copy(quantity = newQty)
                        },
                        label = { Text("No") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                        )
                    )
                    ExposedDropdownMenuBox(expanded = measuresExpanded,
                        onExpandedChange = { measuresExpanded = !measuresExpanded }) {
                        OutlinedTextField(modifier = Modifier
                            .padding(0.dp, 0.dp, 8.dp, 0.dp)
                            .menuAnchor()
                            .width(80.dp)
                            .clickable(enabled = true) {
                                measuresExpanded = true
                            },
                            value = measures.find { m -> m?.id?.toInt() == shoppingListItemDetails.measureId.toInt() }?.abbreviation
                                ?: "",
                            readOnly = true,
                            onValueChange = { },
                            label = { Text("") },
                            singleLine = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = measuresExpanded)
                            })

                        ExposedDropdownMenu(expanded = measuresExpanded,
                            onDismissRequest = { measuresExpanded = false }) {
                            measures.forEach { measure ->
                                measure?.let {
                                    DropdownMenuItem(text = { Text(measure.abbreviation) },
                                        onClick = {
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
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(shoppingListItemDetails.toShoppingList()) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        })
}