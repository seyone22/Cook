package com.seyone22.cook.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.seyone22.cook.ui.screen.cooking.CookingDestination
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination
import com.seyone22.cook.ui.screen.more.MoreDestination
import com.seyone22.cook.ui.screen.shoppingList.ShoppingListDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookTopBar(
    currentActivity: String?,
    navController: NavController,
    searchAction: () -> Unit = {}
) {
    if ((currentActivity == HomeDestination.route) or (currentActivity == IngredientsDestination.route)) {
        CenterAlignedTopAppBar(
            title = { Text(text = "All $currentActivity") },
            actions = {
            }
        )
    }

    if (currentActivity == MoreDestination.route) {
        CenterAlignedTopAppBar(
            title = { Text(text = "More") },
            actions = {
            }
        )
    }

    if (currentActivity == CookingDestination.route) {
        CenterAlignedTopAppBar(
            title = { Text(text = "Cooking Mode") },
            actions = {
            }
        )
    }

    if (currentActivity == ShoppingListDestination.route) {
        TopAppBar(
            title = { Text(text = "Shopping List") },
            actions = {},
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
    }
}