package com.seyone22.cook.ui.screen.cooking

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.ui.AppViewModelProvider
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

}