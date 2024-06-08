package com.seyone22.cook.ui.screen.cooking

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

object CookingDestination : NavigationDestination {
    override val route = "Cooking"
    override val titleRes = R.string.app_name
    override val routeId = 12
}

@Composable
fun CookingScreen(
    viewModel: CookingViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController,
    backStackEntry: String
) {
    // Call the ViewModel function to fetch ingredients when the screen is first displayed
    viewModel.fetchData()
    // Observe the ingredientList StateFlow to display ingredients
    val cookingViewState by viewModel.cookingViewState.collectAsState()

    Scaffold(
        topBar = {
            CookTopBar(
                currentActivity = CookingDestination.route,
                navController = navController
            )
        }
    ) {
        it
    }
}