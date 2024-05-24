package com.seyone22.cook.ui.screen.crud

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.ui.navigation.NavigationDestination

object AddRecipeDestination : NavigationDestination {
    override val route = "Add Recipe"
    override val titleRes = R.string.app_name
    override val routeId = 10
}
@Composable
fun AddRecipeScreen(
    navController: NavController
) {

}