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
import com.seyone22.cook.ui.screen.cooking.CookingDestination
import com.seyone22.cook.ui.screen.cooking.CookingScreen
import com.seyone22.cook.ui.screen.crud.ingredient.AddIngredientDestination
import com.seyone22.cook.ui.screen.crud.ingredient.AddIngredientScreen
import com.seyone22.cook.ui.screen.crud.ingredient.EditIngredientDestination
import com.seyone22.cook.ui.screen.crud.ingredient.EditIngredientScreen
import com.seyone22.cook.ui.screen.crud.recipe.AddRecipeDestination
import com.seyone22.cook.ui.screen.crud.recipe.AddRecipeScreen
import com.seyone22.cook.ui.screen.crud.recipe.EditRecipeDestination
import com.seyone22.cook.ui.screen.crud.recipe.EditRecipeScreen
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.home.HomeScreen
import com.seyone22.cook.ui.screen.home.detail.RecipeDetailDestination
import com.seyone22.cook.ui.screen.home.detail.RecipeDetailScreen
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsScreen
import com.seyone22.cook.ui.screen.ingredients.detail.IngredientDetailDestination
import com.seyone22.cook.ui.screen.ingredients.detail.IngredientDetailScreen
import com.seyone22.cook.ui.screen.more.MoreDestination
import com.seyone22.cook.ui.screen.more.MoreScreen
import com.seyone22.cook.ui.screen.more.SettingsDestination
import com.seyone22.cook.ui.screen.more.SettingsDetailScreen
import com.seyone22.cook.ui.screen.shoppingList.ShoppingListDestination
import com.seyone22.cook.ui.screen.shoppingList.ShoppingListScreen
import com.seyone22.cook.ui.screen.shoppingList.detail.ShoppingListDetailDestination
import com.seyone22.cook.ui.screen.shoppingList.detail.ShoppingListDetailScreen
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CookNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    setOverlayStatus: (Boolean) -> Unit = {},
) {
    NavHost(
        navController = navController,
        startDestination = HomeDestination.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        // Main Navigation Destinations
        composable(route = HomeDestination.route) {
            HomeScreen(
                modifier = modifier.padding(innerPadding),
                navController = navController,
                setOverlayStatus = setOverlayStatus
            )
        }
        composable(route = IngredientsDestination.route) {
            IngredientsScreen(
                modifier = modifier.padding(innerPadding),
                navController = navController
            )
        }
        composable(route = MoreDestination.route) {
            MoreScreen(
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
            route = EditRecipeDestination.route + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            EditRecipeScreen(
                navController = navController,
                recipeId = (UUID.fromString(it.arguments?.getString("id") ?: "-1"))
            )
        }

        // Detail Screen Destinations
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

        // Destination for the Cooking Mode Screen
        composable(
            route = CookingDestination.route + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            CookingScreen(
                navController = navController,
                backStackEntry = it.arguments?.getString("id") ?: "-1"
            )
        }
        // Destination for the Shopping List Screen
        composable(
            route = ShoppingListDestination.route,
        ) {
            ShoppingListScreen(
                navController = navController,
                modifier = modifier.padding(innerPadding)
            )
        }
        composable(
            route = ShoppingListDetailDestination.route + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            ShoppingListDetailScreen(
                navController = navController,
                backStackEntry = it.arguments?.getString("id") ?: "-1"
            )
        }
        // Settings Screens
        composable(
            route = SettingsDestination.route + "/{setting}",
            arguments = listOf(navArgument("setting") { type = NavType.StringType })
        ) {
            SettingsDetailScreen(
                navigateToScreen = { screen -> navController.navigate(screen) },
                navigateBack = { navController.popBackStack() },
                backStackEntry = it.arguments?.getString("setting") ?: "-1",
            )
        }
    }
}
