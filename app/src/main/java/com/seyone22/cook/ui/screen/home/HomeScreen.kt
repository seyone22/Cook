package com.seyone22.cook.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.CookTopBar
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.home.detail.RecipeDetailDestination

object HomeDestination : NavigationDestination {
    override val route = "Recipes"
    override val titleRes = R.string.app_name
    override val routeId = 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController,
    setOverlayStatus: (Boolean) -> Unit = {},
) {


    // Call the ViewModel function to fetch ingredients when the screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    // Observe the ingredientList StateFlow to display ingredients
    val homeViewState by viewModel.homeViewState.collectAsState()

    val recipes = homeViewState.recipes
    val images = homeViewState.images


    Scaffold(topBar = {
        CookTopBar(
            navController = navController, currentActivity = "search", recipeList = recipes, setOverlayStatus = setOverlayStatus
        )
    }) {
        it
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(top = 72.dp)
        ) {
            LazyVerticalStaggeredGrid(modifier = Modifier
                .padding(8.dp, 0.dp)
                .fillMaxHeight(),
                columns = StaggeredGridCells.Adaptive(minSize = 240.dp),
                content = {
                    items(count = recipes.size, itemContent = {
                        RecipeItem(recipe = recipes[it]!!,
                            image = images.find { img -> img!!.recipeId == recipes[it]!!.id },
                            modifier = Modifier.clickable { navController.navigate("${RecipeDetailDestination.route}/${recipes[it]?.id}") })
                    })
                })
        }
    }
}

@Composable
fun RecipeItem(modifier: Modifier, recipe: Recipe, image: RecipeImage?) {
    OutlinedCard(
        modifier = modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (image != null) {
                AsyncImage(
                    model = image.imagePath,
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(216.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            Text(
                text = recipe.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 0.dp),
            )
            Text(
                text = recipe.description ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp),
            )
        }
    }
}