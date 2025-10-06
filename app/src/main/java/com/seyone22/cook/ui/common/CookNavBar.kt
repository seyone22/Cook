package com.seyone22.cook.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FoodBank
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.seyone22.cook.ui.navigation.BottomNavigationDestination
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination
import com.seyone22.cook.ui.screen.meals.MealsDestination
import com.seyone22.cook.ui.screen.more.MoreDestination

val destinations = listOf(
    BottomNavigationDestination(HomeDestination, Icons.Default.Fastfood, Icons.Filled.Fastfood),
    BottomNavigationDestination(
        IngredientsDestination, Icons.Default.ShoppingBasket, Icons.Filled.ShoppingBasket
    ),
    BottomNavigationDestination(MoreDestination, Icons.Default.MoreHoriz, Icons.Filled.MoreHoriz),
    BottomNavigationDestination(MealsDestination, Icons.Default.FoodBank, Icons.Filled.FoodBank)
)

@Composable
fun CookNavBar(
    currentActivity: String?, navigateToScreen: (screen: String) -> Unit, visible: Boolean = true
) {
    if (visible) {
        if ((currentActivity == HomeDestination.route) or (currentActivity == IngredientsDestination.route) or (currentActivity == MoreDestination.route) or (currentActivity == MealsDestination.route)) {
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
                        })
                }
            }
        }
    }
}