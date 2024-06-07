package com.seyone22.cook.ui.screen.shoppingList

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.CookTopBar
import com.seyone22.cook.ui.navigation.NavigationDestination

object ShoppingListDestination : NavigationDestination {
    override val route = "Shopping List"
    override val titleRes = R.string.app_name
    override val routeId = 7
}

@Composable
fun ShoppingListScreen(
    modifier: androidx.compose.ui.Modifier,
    viewModel: ShoppingListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController
) {
    // Call the ViewModel function to fetch ingredients when the screen is first displayed
    viewModel.fetchData()
    // Observe the ingredientList StateFlow to display ingredients
    val shoppingListViewState by viewModel.shoppingListViewState.collectAsState()

    Scaffold(
        topBar = {
            CookTopBar(
                currentActivity = ShoppingListDestination.route,
                navController = navController
            )
        }
    ) {
        it
    }
}