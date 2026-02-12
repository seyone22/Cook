package com.seyone22.cook.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.seyone22.cook.SharedViewModel
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.screen.cooking.CookingDestination
import com.seyone22.cook.ui.screen.cooking.CookingScreen
import com.seyone22.cook.ui.screen.crud.ingredient.AddIngredientDestination
import com.seyone22.cook.ui.screen.crud.ingredient.AddIngredientScreen
import com.seyone22.cook.ui.screen.crud.ingredient.EditIngredientDestination
import com.seyone22.cook.ui.screen.crud.ingredient.EditIngredientScreen
import com.seyone22.cook.ui.screen.crud.recipe.AddRecipeDestination
import com.seyone22.cook.ui.screen.crud.recipe.EditRecipeDestination
import com.seyone22.cook.ui.screen.crud.recipe.ImportRecipeDestination
import com.seyone22.cook.ui.screen.crud.recipe.RecipeFormMode
import com.seyone22.cook.ui.screen.crud.recipe.RecipeFormScreen
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.home.HomeScreen
import com.seyone22.cook.ui.screen.home.detail.RecipeDetailDestination
import com.seyone22.cook.ui.screen.home.detail.RecipeDetailScreen
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsScreen
import com.seyone22.cook.ui.screen.ingredients.detail.IngredientDetailDestination
import com.seyone22.cook.ui.screen.ingredients.detail.IngredientDetailScreen
import com.seyone22.cook.ui.screen.meals.MealsDestination
import com.seyone22.cook.ui.screen.meals.MealsScreen
import com.seyone22.cook.ui.screen.more.MoreDestination
import com.seyone22.cook.ui.screen.more.MoreScreen
import com.seyone22.cook.ui.screen.more.SettingsDestination
import com.seyone22.cook.ui.screen.more.SettingsDetailScreen
import com.seyone22.cook.ui.screen.more.account.ChangePasswordDestination
import com.seyone22.cook.ui.screen.more.account.ChangePasswordScreen
import com.seyone22.cook.ui.screen.more.account.RegisterDestination
import com.seyone22.cook.ui.screen.more.account.RegisterScreen
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
    sharedViewModel: SharedViewModel = viewModel(
        factory = AppViewModelProvider.Factory
    ),
    snackbarHostState: SnackbarHostState
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
                modifier = Modifier,
                snackbarHostState = snackbarHostState,
                navController = navController,
                navigateToScreen = { screen -> navController.navigate(screen) },
                setOverlayStatus = setOverlayStatus
            )
        }
        composable(route = IngredientsDestination.route) {
            IngredientsScreen(
                modifier = modifier.padding(innerPadding), navController = navController
            )
        }
        composable(route = MoreDestination.route) {
            MoreScreen(
                modifier = modifier.padding(innerPadding), navController = navController
            )
        }

        composable(route = MealsDestination.route) {
            MealsScreen(
                modifier = modifier.padding(innerPadding), navController = navController
            )
        }

        // Routes for CRUD operations
        composable(route = AddIngredientDestination.route) {
            AddIngredientScreen(
                navController = navController
            )
        }

        composable(route = "${AddIngredientDestination.route}/{ingredientName}") { backStackEntry ->
            val ingredientName = backStackEntry.arguments?.getString("ingredientName") ?: ""
            AddIngredientScreen(
                navController = navController, ingredientName = ingredientName
            )
        }

        composable(
            route = EditIngredientDestination.route + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            EditIngredientScreen(
                navController = navController,
                ingredientId = (UUID.fromString(it.arguments?.getString("id")))
            )
        }
        composable(route = AddRecipeDestination.route) {
            RecipeFormScreen(
                navController = navController,
                sharedViewModel = sharedViewModel,
                recipeId = null,
                mode = RecipeFormMode.ADD
            )
        }
        composable(route = ImportRecipeDestination.route) {
            RecipeFormScreen(
                navController = navController,
                sharedViewModel = sharedViewModel,
                recipeId = null,
                mode = RecipeFormMode.IMPORT
            )
        }
        composable(
            route = EditRecipeDestination.route + "/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            RecipeFormScreen(
                navController = navController,
                sharedViewModel = sharedViewModel,
                recipeId = UUID.fromString(it.arguments?.getString("id") ?: "-1"),
                mode = RecipeFormMode.EDIT
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
            route = RecipeDetailDestination.route + "/{recipeId}",
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) {
            RecipeDetailScreen(
                navController = navController,
            )
        }

        // Destination for the Cooking Mode Screen
        composable(
            // 1. Update route to accept an optional 'scale' query parameter
            route = CookingDestination.route + "/{id}?scale={scale}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("scale") {
                    type = NavType.FloatType // Using Float for the multiplier
                    defaultValue = 1.0f      // Default to 1x if not provided
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("id") ?: "-1"
            val scaleFactor = backStackEntry.arguments?.getFloat("scale") ?: 1.0f

            // 2. Pass the scale factor to the screen
            CookingScreen(
                navController = navController,
                backStackEntry = recipeId,
                // Pass the scale to the Composable (see Step 2 below)
                initialScale = scaleFactor
            )
        }

        // Destination for the Shopping List Screen
        composable(
            route = ShoppingListDestination.route,
        ) {
            ShoppingListScreen(
                navController = navController, modifier = modifier.padding(innerPadding)
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
                sharedViewModel = sharedViewModel
            )
        }

        composable(
            route = RegisterDestination.route,
        ) {
            RegisterScreen(onRegisterSuccess = { navController.popBackStack() })
        }

        composable(
            route = ChangePasswordDestination.route,
        ) {
            ChangePasswordScreen(
                navController = navController
            )
        }
    }
}
