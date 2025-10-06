package com.seyone22.cook.ui.screen.shoppingList.detail

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.model.ShoppingList
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.parser.parseItemString
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.CookFAB
import com.seyone22.cook.ui.common.CookTopBar
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.shoppingList.ShoppingListViewModel
import com.seyone22.cook.ui.screen.shoppingList.composables.DeleteConfirmationDialog
import com.seyone22.cook.ui.screen.shoppingList.composables.RenameDialog
import com.seyone22.cook.ui.screen.shoppingList.composables.ShoppingItemList
import kotlinx.coroutines.launch
import java.util.UUID

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
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // Modal Bottom Sheet State
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()

// Main composable function
    if (showNewDialog) {
        var textFieldValue by remember { mutableStateOf("") }

        val filteredIngredients = data.ingredients.filter {
            it?.nameEn?.contains(textFieldValue, ignoreCase = true) == true
        }

        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch { bottomSheetState.hide() }
                showNewDialog = false
            },
            sheetState = bottomSheetState,
            modifier = Modifier.windowInsetsPadding(WindowInsets.ime)
        ) {
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current

            // Request focus and show keyboard only after the dialog is visible
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }

            Column {
                // Bottom sheet content
                LazyRow {
                    filteredIngredients.forEach { ingredient ->
                        item {
                            InputChip(onClick = {
                                // Add the selected ingredient to the shopping list
                                viewModel.addToShoppingList(
                                    ShoppingListItem(
                                        shoppingListId = backStackEntry.toLong(),
                                        ingredientId = ingredient?.id ?: UUID.randomUUID(),
                                        quantity = 1.0,
                                        measureId = 1
                                    )
                                )
                                textFieldValue = "" // Clear the text field
                                viewModel.fetchData() // Fetch updated data
                            },
                                label = { Text(ingredient?.nameEn ?: "") },
                                selected = false,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextField(value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        placeholder = { Text("Item Name") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,  // Set the background to transparent
                            focusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.clickable {
                                    try {
                                        val triple = parseItemString(textFieldValue)
                                        textFieldValue = "${triple.first} + ${triple.second} + ${triple.third}"
                                    } catch (e: Exception) {
                                        textFieldValue = e.message ?: ""
                                    }
                                })
                        },
                        keyboardActions = KeyboardActions(
                            onDone = {
                                // Add the selected ingredient to the shopping list
                                viewModel.addToShoppingList(
                                    ShoppingListItem(
                                        shoppingListId = backStackEntry.toLong(),
                                        ingredientId = UUID.randomUUID(),
                                        quantity = 1.0,
                                        measureId = 1
                                    )
                                )
                                textFieldValue = "" // Clear the text field
                                viewModel.fetchData() // Fetch updated data
                            }
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }
        }
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
            )
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