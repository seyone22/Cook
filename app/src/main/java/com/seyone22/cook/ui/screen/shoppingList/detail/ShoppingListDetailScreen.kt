package com.seyone22.cook.ui.screen.shoppingList.detail

import android.content.Context
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination
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

    LazyColumn() {
        items.forEach { item ->
            item {
                Text(text = item?.ingredientId.toString())
            }
        }
    }
}