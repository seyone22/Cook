package com.seyone22.cook.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.seyone22.cook.ui.screen.crud.ingredient.AddIngredientDestination
import com.seyone22.cook.ui.screen.crud.recipe.AddRecipeDestination
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination
import com.seyone22.cook.ui.screen.shoppingList.detail.ShoppingListDetailDestination

@Composable
fun CookFAB(
    currentActivity: String?, navigateToScreen: (String) -> Unit = {}, action: () -> Unit = {}
) {
    if ((currentActivity == HomeDestination.route) or (currentActivity == IngredientsDestination.route)) {
        ExtendedFloatingActionButton(text = { Text(text = "New $currentActivity") }, icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
            )
        }, onClick = {
            when (currentActivity) {
                IngredientsDestination.route -> {
                    navigateToScreen(AddIngredientDestination.route)
                }

                HomeDestination.route -> {
                    navigateToScreen(AddRecipeDestination.route)
                }
            }
        })
    }

    if (currentActivity == ShoppingListDetailDestination.route) {
        ExtendedFloatingActionButton(text = { Text(text = "Add Item") }, icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
            )
        }, onClick = action)
    }
}