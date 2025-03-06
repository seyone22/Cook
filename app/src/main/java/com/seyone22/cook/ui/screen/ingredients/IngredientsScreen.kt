package com.seyone22.cook.ui.screen.ingredients

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.seyone22.cook.R
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientImage
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.ingredients.composables.IngredientListItem
import com.seyone22.cook.ui.screen.ingredients.detail.IngredientDetailDestination

object IngredientsDestination : NavigationDestination {
    override val route = "Ingredients"
    override val titleRes = R.string.app_name
    override val routeId = 1
}

@Composable
fun IngredientsScreen(
    modifier: Modifier,
    viewModel: IngredientsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController
) {
    // Call the ViewModel function to fetch ingredients when the screen is first displayed
    viewModel.fetchData()

    // Observe the ingredientList StateFlow to display ingredients
    val ingredientsViewState by viewModel.ingredientsViewState.collectAsState()

    val ingredients = ingredientsViewState.ingredients
    val images = ingredientsViewState.ingredientImages

    // Implement the UI for the Ingredients screen using Jetpack Compose
    LazyVerticalStaggeredGrid(modifier = modifier.padding(8.dp),
        columns = StaggeredGridCells.Adaptive(minSize = 320.dp),
        content = {
            items(count = ingredients.size, itemContent = {
                IngredientListItem(ingredient = ingredients[it]!!,
                    image = images.find { img -> img!!.ingredientId == ingredients[it]!!.id },
                    modifier = Modifier.clickable { navController.navigate("${IngredientDetailDestination.route}/${ingredients[it]?.id}") })
            })
        })
}
