package com.seyone22.cook.ui.screen.ingredients

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.ui.navigation.NavigationDestination

object IngredientsDestination : NavigationDestination {
    override val route = "Ingredients"
    override val titleRes = R.string.app_name
    override val routeId = 1
}

@Composable
fun IngredientsScreen(
    viewModel: IngredientsViewModel = viewModel()
) {
    // Call the ViewModel function to fetch ingredients when the screen is first displayed
    viewModel.getIngredients()

    // Observe the ingredientList StateFlow to display ingredients
    val ingredients = viewModel.ingredientList.collectAsState(initial = emptyList()).value

    // Implement the UI for the Ingredients screen using Jetpack Compose
    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(ingredients) { ingredient ->
            IngredientItem(ingredient = ingredient)
        }
    }
}

@Composable
fun IngredientItem(ingredient: Ingredient) {
    // Implement the UI for displaying an individual ingredient
    Text(text = ingredient.nameEn)
    // You can display other details of the ingredient here
}