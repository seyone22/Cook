package com.seyone22.cook.ui.screen.home

import android.content.Context
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.SharedViewModel
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
    modifier: Modifier = Modifier,
    // Note: If you migrate to Hilt, change this to: viewModel: HomeViewModel = hiltViewModel()
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AppViewModelProvider.Factory),
    sharedViewModel: SharedViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AppViewModelProvider.Factory),
    context: Context = LocalContext.current,
    navController: NavController,
    navigateToScreen: (String) -> Unit,
    setOverlayStatus: (Boolean) -> Unit = {},
    snackbarHostState: SnackbarHostState,
) {
    // 1. Observe the "Source of Truth" from the ViewModel
    // use collectAsStateWithLifecycle() if you have the dependency, otherwise collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isImporting by sharedViewModel.isLoading.collectAsState()

    // ----------------------------
    // Clipboard auto-detect (Event based logic)
    // ----------------------------
    val clipboardManager = LocalClipboardManager.current
    var lastCheckedUrl by remember { mutableStateOf<String?>(null) }

//    LaunchedEffect(Unit) {
//        val clipText = clipboardManager.getText()?.text
//        if (!clipText.isNullOrBlank() && clipText.startsWith("http") && clipText != lastCheckedUrl) {
//            lastCheckedUrl = clipText
//            val result = snackbarHostState.showSnackbar(
//                message = "Found a recipe URL in clipboard",
//                actionLabel = "Import",
//                duration = SnackbarDuration.Short
//            )
//            if (result == SnackbarResult.ActionPerformed) {
//                sharedViewModel.setLoading(true)
//                val success = sharedViewModel.importAndSaveRecipe(clipText)
//                sharedViewModel.setLoading(false)
//
//                if (success) {
//                    navigateToScreen(ImportRecipeDestination.route)
//                } else {
//                    Toast.makeText(context, "Failed to fetch recipe", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }

    Scaffold(
        topBar = {
            CookTopBar(
                navController = navController,
                currentActivity = "search",
                recipeList = uiState.allRecipes, // TopBar likely searches full list
                tagList = uiState.tags,
                recipeTags = uiState.recipeTags,
                setOverlayStatus = setOverlayStatus
            )
        },
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // ----------------------------
                    // Tag filter chips
                    // ----------------------------
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 16.dp, top = 0.dp, end = 16.dp),
                    ) {
                        // Only show tags that are actually attached to at least one recipe
                        val activeTags = uiState.tags.filter { tag ->
                            uiState.recipeTags.any { rt -> rt.tagId == tag.id }
                        }

                        items(activeTags.size) { index ->
                            val tag = activeTags[index]
                            val isSelected = uiState.selectedFilters.contains(tag)

                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleFilter(tag) },
                                label = { Text(text = tag.name) },
                                trailingIcon = {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove Tag"
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // ----------------------------
                    // Recipes grid
                    // ----------------------------
                    if (uiState.filteredRecipes.isNotEmpty()) {
                        LazyVerticalStaggeredGrid(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(horizontal = 16.dp),
                            verticalItemSpacing = 16.dp,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            columns = StaggeredGridCells.Adaptive(minSize = 180.dp),
                            content = {
                                items(uiState.filteredRecipes.size) { index ->
                                    val recipe = uiState.filteredRecipes[index]
                                    // SAFE CALL: No more !! operator
                                    val image = uiState.images.find { it.recipeId == recipe.id }

                                    RecipeCard(
                                        recipe = recipe,
                                        image = image,
                                        modifier = Modifier.clickable {
                                            navController.navigate("${RecipeDetailDestination.route}/${recipe.id}")
                                        }
                                    )
                                }
                                item { Spacer(modifier = Modifier.padding(bottom = 84.dp)) }
                            }
                        )
                    } else {
                        // Empty State
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
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

            // Loading Overlay for Import
            if (isImporting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}