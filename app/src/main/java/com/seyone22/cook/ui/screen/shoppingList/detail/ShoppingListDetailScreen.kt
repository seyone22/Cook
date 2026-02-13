package com.seyone22.cook.ui.screen.shoppingList.detail

import EditItemDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.CookTopBar
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.shoppingList.ShoppingListViewModel
import com.seyone22.cook.ui.screen.shoppingList.composables.DeleteConfirmationDialog
import com.seyone22.cook.ui.screen.shoppingList.composables.RenameDialog

object ShoppingListDetailDestination : NavigationDestination {
    override val route = "Shopping List Details"
    override val titleRes = R.string.app_name
    override val routeId = 8
}


@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun ShoppingListDetailScreen(
    viewModel: ShoppingListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    backStackEntry: String, // This is the listId
    navController: NavController
) {
    val listId = backStackEntry.toLongOrNull() ?: return

    // 1. Trigger the smart loader
    LaunchedEffect(listId) { viewModel.loadShoppingListDetails(listId) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }

    // --- DIALOG STATES ---
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // --- ITEM ACTION STATES ---
    var itemToEdit by remember { mutableStateOf<ShoppingListViewModel.ShoppingItemDisplay?>(null) }
    var itemToDelete by remember { mutableStateOf<ShoppingListViewModel.ShoppingItemDisplay?>(null) }

    // --- ITEM DIALOGS ---
    if (itemToEdit != null) {
        EditItemDialog(
            item = itemToEdit!!,
            onDismiss = { itemToEdit = null },
            onConfirm = { newQty, newUnit ->
                // 1. Update the item
                // Note: We are just updating quantity here.
                // To update Unit properly, you'd need to find the Measure ID from the Repo.
                // For V1, let's just update Quantity.
                viewModel.updateShoppingListItem(
                    itemToEdit!!.item.copy(quantity = newQty)
                )
                itemToEdit = null
            }
        )
    }

    if (itemToDelete != null) {
        DeleteConfirmationDialog(
            onDismiss = { itemToDelete = null },
            onConfirm = {
                viewModel.removeFromShoppingList(itemToDelete!!.item)
                itemToDelete = null
            }
        )
    }

    // --- DIALOGS ---
    if (showRenameDialog && uiState.shoppingList != null) {
        RenameDialog(
            shoppingList = uiState.shoppingList!!,
            onDismiss = { showRenameDialog = false },
            onConfirm = {
                viewModel.renameShoppingList(it)
                showRenameDialog = false
            })
    }

    if (showDeleteDialog && uiState.shoppingList != null) {
        DeleteConfirmationDialog(onDismiss = { showDeleteDialog = false }, onConfirm = {
            viewModel.deleteShoppingList(uiState.shoppingList!!)
            showDeleteDialog = false
            navController.popBackStack()
        })
    }

    Scaffold(topBar = {
        CookTopBar(
            title = if (uiState.shoppingList?.completed == true) {
                "${uiState.listName} (Completed)"
            } else {
                uiState.listName
            },
            navController = navController,
            currentActivity = ShoppingListDetailDestination.route,
            activityType = { action ->
                when (action) {
                    "rename" -> showRenameDialog = true
                    "delete" -> showDeleteDialog = true
                    "complete" -> {
                        uiState.shoppingList?.let {
                            viewModel.completeShoppingList(it)
                            navController.popBackStack()
                        }
                    }
                }
            })
    }, floatingActionButtonPosition = FabPosition.Center, floatingActionButton = {
        // --- SMART INPUT BAR ---
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.imePadding()) {
            // Looks like a messaging app input
            HorizontalFloatingToolbar(
                expanded = true,
                modifier = Modifier.padding(0.dp),
            ) {
                OutlinedTextField(
                    modifier = Modifier.padding(0.dp),
                    enabled = uiState.shoppingList?.completed == false,
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("e.g. 2 kg Chicken") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (inputText.isNotBlank()) {
                            viewModel.addSmartItem(listId, inputText)
                            inputText = ""
                        }
                    })
                )
            }
            Spacer(Modifier.width(8.dp))
            FloatingActionButton(
                onClick = {
                    if (inputText.isNotBlank() && uiState.shoppingList?.completed == false) {
                        viewModel.addSmartItem(listId, inputText)
                        inputText = ""
                    }
                }) {
                Icon(Icons.AutoMirrored.Filled.Send, "Add Item")
            }
        }
    }) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Space so last item isn't hidden by input bar
            ) {
                // Render groups from the Map
                uiState.categories.forEach { (category, items) ->
                    // --- STICKY HEADER ---
                    stickyHeader {
                        Surface(
                            color = MaterialTheme.colorScheme.surface, // Background to cover scrolling items
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (category == "Completed") MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .background(
                                        if (category == "Completed") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer.copy(
                                            alpha = 0.3f
                                        ), RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // --- LIST ITEMS ---
                    items(items) { displayItem ->
                        ShoppingListItemRow(
                            item = displayItem,
                            onToggle = { viewModel.toggleItemCheck(displayItem.item) },
                            onEdit = { itemToEdit = displayItem },
                            onDelete = { itemToDelete = displayItem }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListItemRow(
    item: ShoppingListViewModel.ShoppingItemDisplay,
    onToggle: () -> Unit,
    onEdit: () -> Unit,   // NEW
    onDelete: () -> Unit  // NEW
) {
    val isChecked = item.item.checked
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        modifier = Modifier.clickable { onToggle() },
        headlineContent = {
            Text(
                text = item.ingredientName,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                color = if (isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = "${if (item.item.quantity % 1.0 == 0.0) item.item.quantity.toInt() else item.item.quantity} ${item.measureName}",
                color = MaterialTheme.colorScheme.secondary
            )
        },
        leadingContent = {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
        },
        // NEW: Trailing Icon with Menu
        trailingContent = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    )
    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}