package com.seyone22.cook.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.model.Tag
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.CookTopBar
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.home.composables.RecipeCard
import com.seyone22.cook.ui.screen.home.detail.RecipeDetailDestination

object HomeDestination : NavigationDestination {
    override val route = "Recipes"
    override val titleRes = R.string.app_name
    override val routeId = 0
}

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
    var filteredRecipes by remember { mutableStateOf(homeViewState.recipes) }
    val images = homeViewState.images

    LaunchedEffect(homeViewState) {
        filteredRecipes = homeViewState.recipes
    }

    var filters by remember { mutableStateOf<List<Tag>>(emptyList()) }

    Scaffold(topBar = {
        CookTopBar(
            navController = navController,
            currentActivity = "search",
            recipeList = recipes,
            tagList = homeViewState.tags,
            recipeTags = homeViewState.recipeTags,
            setOverlayStatus = setOverlayStatus
        )
    }) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp, 0.dp),
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                homeViewState.tags.forEach { tag ->
                    if (homeViewState.recipeTags.any { t -> t?.tagId == tag?.id }) {
                        item {
                            var isSelected by remember { mutableStateOf(false) }
                            FilterChip(selected = isSelected,  // Chips are not selected by default
                                onClick = {
                                    // Toggle selection
                                    isSelected = !isSelected

                                    // Update filters list: Add tag if selected, remove if deselected
                                    filters = if (isSelected) {
                                        filters + tag!!  // Add selected tag to filters
                                    } else {
                                        filters - tag!!  // Remove deselected tag from filters
                                    }

                                    if (filters.isNotEmpty()) {
                                        // Now filter recipes based on the selected tags
                                        filteredRecipes = recipes.filter { recipe ->
                                            // Get all tags associated with this recipe (from recipeTags relation)
                                            val recipeTagIds =
                                                homeViewState.recipeTags.filter { recipeTag -> recipeTag?.recipeId == recipe?.id }
                                                    .map { recipeTag -> recipeTag?.tagId }

                                            // Check if any of the recipe's tags match the selected filters
                                            recipeTagIds.any { tagId ->
                                                filters.any { filterTag -> filterTag.id == tagId }
                                            }
                                        }
                                    } else {
                                        filteredRecipes = homeViewState.recipes
                                    }
                                }, label = {
                                    Text(text = tag?.name ?: "")
                                }, trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Close,  // Close icon for the chip
                                            contentDescription = "Remove Tag"
                                        )
                                    }
                                })

                        }
                    }
                }
            }
            if (filteredRecipes.isNotEmpty()) {
                LazyVerticalStaggeredGrid(modifier = Modifier.fillMaxHeight(),
                    verticalItemSpacing = 16.dp,
                    columns = StaggeredGridCells.Adaptive(minSize = 240.dp),
                    content = {
                        items(count = filteredRecipes.size, itemContent = {
                            RecipeCard(recipe = filteredRecipes[it]!!,
                                image = images.find { img -> img!!.recipeId == filteredRecipes[it]!!.id },
                                modifier = Modifier.clickable { navController.navigate("${RecipeDetailDestination.route}/${filteredRecipes[it]?.id}") })
                        })
                    })
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(onClick = { navController.navigate("Settings/Data") }) {
                        Text(text = "Import Recipe")
                    }
                }
            }
        }
    }
}