package com.seyone22.cook.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookTopBar(
    currentActivity: String?,
    navController: NavController,
    searchAction: () -> Unit
) {
    if ((currentActivity == HomeDestination.route) or (currentActivity == IngredientsDestination.route)) {
        CenterAlignedTopAppBar(
            title = { Text(text = "All $currentActivity") },
            actions = {
            }
        )
    }
}