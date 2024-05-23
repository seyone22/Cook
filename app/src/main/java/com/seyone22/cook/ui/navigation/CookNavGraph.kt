package com.seyone22.cook.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.home.HomeScreen
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsScreen

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CookNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    setTopBarAction : (Int) -> Unit,
    innerPadding : PaddingValues
) {
    NavHost(
        modifier = modifier.padding(innerPadding),
        navController = navController,
        startDestination = HomeDestination.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navController = navController
            )
        }
        composable(route = IngredientsDestination.route) {
            IngredientsScreen(
                navController = navController
            )
        }
    }
}
