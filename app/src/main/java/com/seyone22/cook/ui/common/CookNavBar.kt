package com.seyone22.cook.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.seyone22.cook.ui.navigation.BottomNavigationDestination
import com.seyone22.cook.ui.screen.crud.AddIngredientDestination
import com.seyone22.cook.ui.screen.crud.AddRecipeDestination
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination

val destinations = listOf(
    BottomNavigationDestination(HomeDestination, Icons.Default.Fastfood, Icons.Filled.Fastfood),
    BottomNavigationDestination(IngredientsDestination, Icons.Default.ShoppingBasket, Icons.Filled.ShoppingBasket)
)

@Composable
fun CookNavBar(
    currentActivity: String?,
    navigateToScreen: (screen: String) -> Unit,
) {
    if ((currentActivity == HomeDestination.route) or (currentActivity == IngredientsDestination.route)) {
        NavigationBar {
            destinations.forEachIndexed { _, pair ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (currentActivity == pair.destination.route) {
                                pair.iconSelected
                            } else {
                                pair.icon
                            },
                            contentDescription = pair.destination.route,
                        )
                    },
                    label = { Text(pair.destination.route) },
                    selected = currentActivity == pair.destination.route,
                    onClick = {
                        navigateToScreen(pair.destination.route)
                    }
                )
            }
        }
    }
}