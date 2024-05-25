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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.seyone22.cook.ui.screen.crud.AddIngredientDestination
import com.seyone22.cook.ui.screen.crud.AddIngredientScreen
import com.seyone22.cook.ui.screen.crud.AddRecipeDestination
import com.seyone22.cook.ui.screen.crud.AddRecipeScreen
import com.seyone22.cook.ui.screen.crud.EditIngredientDestination
import com.seyone22.cook.ui.screen.crud.EditIngredientScreen
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.home.HomeScreen
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsScreen
import com.seyone22.cook.ui.screen.home.detail.RecipeDetailDestination
import com.seyone22.cook.ui.screen.home.detail.RecipeDetailScreen
import com.seyone22.cook.ui.screen.ingredients.detail.IngredientDetailDestination
import com.seyone22.cook.ui.screen.ingredients.detail.IngredientDetailScreen

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CookNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    setTopBarAction: (Int) -> Unit,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                modifier = modifier.padding(innerPadding),
                navController = navController,
            )
        }
        composable(route = IngredientsDestination.route) {
            IngredientsScreen(
                modifier = modifier.padding(innerPadding),
                navController = navController
            )
        }

        // Routes for CRUD operations
        composable(route = AddIngredientDestination.route) {
            AddIngredientScreen(
                navController = navController
            )
        }
        // Routes for CRUD operations
        composable(
            route = EditIngredientDestination.route + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            EditIngredientScreen(
                navController = navController,
                ingredientId = (it.arguments?.getString("id") ?: "-1").toLong()
            )
        }
        composable(route = AddRecipeDestination.route) {
            AddRecipeScreen(
                navController = navController
            )
        }
        composable(
            route = IngredientDetailDestination.route + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            IngredientDetailScreen(
                navController = navController,
                backStackEntry = it.arguments?.getString("id") ?: "-1"
            )
        }
        composable(
            route = RecipeDetailDestination.route + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            RecipeDetailScreen(
                navController = navController,
                backStackEntry = it.arguments?.getString("id") ?: "-1"
            )
        }
    }
}
