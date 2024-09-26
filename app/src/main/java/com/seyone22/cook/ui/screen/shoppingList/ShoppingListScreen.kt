package com.seyone22.cook.ui.screen.shoppingList

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.model.ShoppingList
import com.seyone22.cook.helper.DateTimeHelper.toIsoString
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.CookFAB
import com.seyone22.cook.ui.common.CookTopBar
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.shoppingList.detail.ShoppingListDetailDestination
import java.time.LocalDateTime

object ShoppingListDestination : NavigationDestination {
    override val route = "Shopping List"
    override val titleRes = R.string.app_name
    override val routeId = 7
}

@Composable
fun ShoppingListScreen(
    modifier: Modifier,
    viewModel: ShoppingListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController
) {
    // Call the ViewModel function to fetch ingredients when the screen is first displayed
    viewModel.fetchData()
    // Observe the ingredientList StateFlow to display ingredients
    val shoppingListViewState by viewModel.shoppingListViewState.collectAsState()

    var filterCompleted by remember { mutableStateOf(true) }

    var shoppingData =
        shoppingListViewState.shoppingLists.filter { i -> i?.completed == !filterCompleted }


    var showNewDialog by remember { mutableStateOf(false) }
    if (showNewDialog) {
        NewShoppingListDialog(onConfirm = {
            viewModel.addShoppingList(it)
            viewModel.fetchData()
            showNewDialog = false
        }, onDismiss = { showNewDialog = false })
    }

    Scaffold(topBar = {
        CookTopBar(
            currentActivity = ShoppingListDestination.route, navController = navController
        )
    }, floatingActionButton = { CookFAB(currentActivity = "newlist", action = {
        showNewDialog = true
        Log.d("TAG", "ShoppingListScreen: $showNewDialog")
    }) }) {
        LazyColumn(modifier = Modifier.padding(it)) {
            item {
                Row(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    FilterChip(selected = !filterCompleted, leadingIcon = {
                        if (!filterCompleted) Icon(
                            imageVector = Icons.Default.Check, contentDescription = null
                        )
                    }, onClick = {
                        filterCompleted = !filterCompleted
                        shoppingData = if (filterCompleted) {
                            shoppingListViewState.shoppingLists.filter { i -> i?.completed == true }
                        } else {
                            shoppingListViewState.shoppingLists
                        }
                    }, label = { Text("Completed") })
                }
            }
            if (shoppingData.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp, 100.dp, 16.dp, 0.dp)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            text = "༼;´༎ຶ \u06DD ༎ຶ༽",
                            fontSize = 32.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Unfortunately for you, however, you are Shopping List-less. Without a plan or list, you are fated, it seems, to shop around aimlessly. To see all shopping lists, toggle the chip above.",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                shoppingListViewState.shoppingLists.forEach { item ->
                    item {
                        if (item != null) {
                            ShoppingListCard(item) { id -> navController.navigate("${ShoppingListDetailDestination.route}/$id") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListCard(
    item: ShoppingList, onClick: (Long) -> Unit
) {
    ListItem(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp, 4.dp)
        .clickable { onClick(item.id) },
        headlineContent = { Text(text = item.name) },
        leadingContent = {
            if (item.completed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            } else {
            }
        })
}

@Composable
fun NewShoppingListDialog(
    onConfirm: (ShoppingList) -> Unit, onDismiss: () -> Unit
) {
    var shoppingList by remember {
        mutableStateOf(
            ShoppingList(
                name = "",
                dateCreated = LocalDateTime.now().toIsoString(),
                dateModified = LocalDateTime.now().toIsoString()
            )
        )
    }
    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text(text = "Create a Shopping List") },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.width(310.dp),
                    value = shoppingList.name,
                    onValueChange = { newName ->
                        shoppingList = shoppingList.copy(name = newName)
                    },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onConfirm(shoppingList) })
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(shoppingList) }) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        })
}