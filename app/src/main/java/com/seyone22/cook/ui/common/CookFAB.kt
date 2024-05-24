package com.seyone22.cook.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.seyone22.cook.ui.screen.crud.AddIngredientDestination
import com.seyone22.cook.ui.screen.crud.AddRecipeDestination
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination

@Composable
fun CookFAB(
    currentActivity: String?,
    navigateToScreen: (String) -> Unit,
) {
    if ((currentActivity == HomeDestination.route) or (currentActivity == IngredientsDestination.route)) {
        ExtendedFloatingActionButton(text = { Text(text = "Add $currentActivity") }, icon = {
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
}