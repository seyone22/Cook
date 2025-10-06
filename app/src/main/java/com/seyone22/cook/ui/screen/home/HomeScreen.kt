package com.seyone22.cook.ui.screen.home

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.SharedViewModel
import com.seyone22.cook.data.model.Tag
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.CookTopBar
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.crud.recipe.ImportRecipeDestination
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
    sharedViewModel: SharedViewModel = viewModel(factory = AppViewModelProvider.Factory),
    context: Context = LocalContext.current,
    navController: NavController,
    navigateToScreen: (String) -> Unit,
    setOverlayStatus: (Boolean) -> Unit = {},
    snackbarHostState: SnackbarHostState,
) {
    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    val clipboardManager = LocalClipboardManager.current
    val homeViewState by viewModel.homeViewState.collectAsState()
    val recipes = homeViewState.recipes
    var filteredRecipes by remember { mutableStateOf(homeViewState.recipes) }
    val images = homeViewState.images
    var filters by remember { mutableStateOf<List<Tag>>(emptyList()) }

    LaunchedEffect(homeViewState) {
        filteredRecipes = homeViewState.recipes
    }

    // ----------------------------
// Clipboard auto-detect
// ----------------------------
    var lastCheckedUrl by remember { mutableStateOf<String?>(null) }
    val isImporting by sharedViewModel.isLoading.collectAsState()


    LaunchedEffect(Unit) {
        val clipText = clipboardManager.getText()?.text

        if (!clipText.isNullOrBlank() && clipText.startsWith("http") && clipText != lastCheckedUrl) {
            lastCheckedUrl = clipText

            val result = snackbarHostState.showSnackbar(
                message = "Found a recipe URL in clipboard", actionLabel = "Import", duration = SnackbarDuration.Short
            )

            if (result == SnackbarResult.ActionPerformed) {
                // User tapped Import
                sharedViewModel.setLoading(true)
                val success = sharedViewModel.importAndSaveRecipe(clipText)
                sharedViewModel.setLoading(false)

                if (success) {
                    navigateToScreen(ImportRecipeDestination.route)
                } else {
                    Toast.makeText(context, "Failed to fetch recipe", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    Scaffold(topBar = {
        CookTopBar(
            navController = navController,
            currentActivity = "search",
            recipeList = recipes,
            tagList = homeViewState.tags,
            recipeTags = homeViewState.recipeTags,
            setOverlayStatus = setOverlayStatus
        )
    }, snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) }) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                // ----------------------------
                // Tag filter chips
                // ----------------------------
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 16.dp, top = 0.dp, end = 16.dp),
                ) {
                    homeViewState.tags.forEach { tag ->
                        if (homeViewState.recipeTags.any { t -> t?.tagId == tag?.id }) {
                            item {
                                var isSelected by remember { mutableStateOf(false) }
                                FilterChip(selected = isSelected, onClick = {
                                    isSelected = !isSelected
                                    filters = if (isSelected) filters + tag!! else filters - tag!!
                                    filteredRecipes = if (filters.isNotEmpty()) {
                                        recipes.filter { recipe ->
                                            val recipeTagIds =
                                                homeViewState.recipeTags.filter { it?.recipeId == recipe?.id }
                                                    .map { it?.tagId }
                                            recipeTagIds.any { tagId -> filters.any { filterTag -> filterTag.id == tagId } }
                                        }
                                    } else recipes
                                }, label = { Text(text = tag?.name ?: "") }, trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Tag"
                                        )
                                    }
                                })
                            }
                        }
                    }
                }

                // ----------------------------
                // Recipes grid
                // ----------------------------
                if (filteredRecipes.isNotEmpty()) {
                    LazyVerticalStaggeredGrid(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 16.dp, top = 0.dp, end = 16.dp),
                        verticalItemSpacing = 16.dp,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        columns = StaggeredGridCells.Adaptive(minSize = 180.dp),
                        content = {
                            items(filteredRecipes.size) { index ->
                                RecipeCard(
                                    recipe = filteredRecipes[index]!!,
                                    image = images.find { it!!.recipeId == filteredRecipes[index]!!.id },
                                    modifier = Modifier.clickable {
                                        navController.navigate("${RecipeDetailDestination.route}/${filteredRecipes[index]?.id}")
                                    })
                            }
                            item { Spacer(modifier = Modifier.padding(bottom = 84.dp)) }
                        })
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 16.dp, top = 0.dp, end = 16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextButton(onClick = { navController.navigate("Settings/Data") }) {
                            Text(text = "Import Recipe")
                        }
                    }
                }
            }

            if (isImporting) {
                // Fullscreen semi-transparent overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
