package com.seyone22.cook.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.ui.navigation.NavigationDestination

object HomeDestination : NavigationDestination {
    override val route = "Home"
    override val titleRes = R.string.app_name
    override val routeId = 0
}

@Composable
fun HomeScreen(
    navController: NavController
) {
}