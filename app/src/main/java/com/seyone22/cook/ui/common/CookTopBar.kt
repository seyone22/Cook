package com.seyone22.cook.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.ui.screen.crud.AddIngredientDestination
import com.seyone22.cook.ui.screen.crud.AddRecipeDestination
import com.seyone22.cook.ui.screen.home.HomeDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookTopBar(
    currentActivity: String?,
    navController: NavController,
    saveAction: () -> Unit
) {
    if ((currentActivity == HomeDestination.route) or (currentActivity == IngredientsDestination.route)) {
        CenterAlignedTopAppBar(title = { Text(stringResource(id = R.string.app_name)) })
    } else if ((currentActivity != AddIngredientDestination.route) and (currentActivity != AddRecipeDestination.route)) {
        TopAppBar(
            modifier = Modifier.padding(0.dp),
            title = { Text(text = "Ingredient Details") },
            navigationIcon = {
                Icon(
                    modifier = Modifier
                        .padding(16.dp, 0.dp, 24.dp, 0.dp)
                        .clickable { navController.popBackStack() },
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                )
            },
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        )
    }
}