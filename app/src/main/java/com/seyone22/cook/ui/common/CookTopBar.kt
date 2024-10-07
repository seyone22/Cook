package com.seyone22.cook.ui.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeTag
import com.seyone22.cook.data.model.Tag
import com.seyone22.cook.ui.screen.cooking.CookingDestination
import com.seyone22.cook.ui.screen.home.detail.RecipeDetailDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsDestination
import com.seyone22.cook.ui.screen.more.MoreDestination
import com.seyone22.cook.ui.screen.shoppingList.ShoppingListDestination
import com.seyone22.cook.ui.screen.shoppingList.detail.ShoppingListDetailDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookTopBar(
    currentActivity: String?,
    title: String = "",
    navController: NavController,
    recipeList: List<Recipe?> = listOf(),
    tagList: List<Tag?> = listOf(),
    recipeTags: List<RecipeTag?> = listOf(),
    setOverlayStatus: (Boolean) -> Unit = {},
    activityType: (String) -> Unit = {},
    context: Context = LocalContext.current
) {
    if (currentActivity == "search") {

        Log.d("TAG", "CookTopBar: $recipeList")
        var searchActive by remember { mutableStateOf(false) }
        var query by remember { mutableStateOf("") }

        var filteredRecipes =
            recipeList.filter { recipe -> (recipe?.name ?: "").contains(query, true) }

        val searchHistory = mutableListOf<String>()

        var filters by remember { mutableStateOf<List<Tag>>(emptyList()) }

        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = {
                        searchActive = false
                        setOverlayStatus(false)
                    },
                    expanded = searchActive,
                    onExpandedChange = {
                        searchActive = it
                        setOverlayStatus(searchActive)
                    },
                    enabled = true,
                    placeholder = { Text(text = "Search for recipes") },
                    leadingIcon = {
                        if (searchActive) {
                            IconButton(onClick = {
                                query = ""
                                searchActive = false
                                setOverlayStatus(false)
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        } else {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        }
                    },
                    trailingIcon = {
                        if (searchActive) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close, contentDescription = "Back"
                                )
                            }
                        }
                    },
                )
            },
            expanded = searchActive,
            onExpandedChange = { searchActive = it },
            modifier = if (searchActive) {
                Modifier
                    .padding(0.dp)
                    .fillMaxWidth()
            } else {
                Modifier
                    .padding(16.dp, 0.dp)
                    .fillMaxWidth()
            },
            shape = SearchBarDefaults.inputFieldShape,
            tonalElevation = SearchBarDefaults.TonalElevation,
            shadowElevation = SearchBarDefaults.ShadowElevation,
            windowInsets = SearchBarDefaults.windowInsets,
            content = {
                LazyRow {
                    tagList.forEach { tag ->
                        if (recipeTags.any { t -> t?.tagId == tag?.id }) {
                            item {
                                var isSelected by remember { mutableStateOf(false) }
                                FilterChip(modifier = Modifier.padding(start = 8.dp),
                                    selected = isSelected,  // Chips are not selected by default
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
                                            filteredRecipes = recipeList.filter { recipe ->
                                                // Get all tags associated with this recipe (from recipeTags relation)
                                                val recipeTagIds =
                                                    recipeTags.filter { recipeTag -> recipeTag?.recipeId == recipe?.id }
                                                        .map { recipeTag -> recipeTag?.tagId }

                                                // Check if any of the recipe's tags match the selected filters
                                                recipeTagIds.any { tagId ->
                                                    filters.any { filterTag -> filterTag.id == tagId }
                                                }
                                            }
                                        } else {
                                            filteredRecipes = recipeList
                                        }
                                    },
                                    label = {
                                        Text(text = tag?.name ?: "")
                                    },
                                    trailingIcon = {
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
                LazyColumn {
                    if (query.isNotBlank() && filteredRecipes.isNotEmpty()) {
                        item {
                            filteredRecipes.forEach {
                                ListItem(headlineContent = { Text(text = it?.name ?: "") },
                                    modifier = Modifier.clickable {
                                        navController.navigate("${RecipeDetailDestination.route}/${it?.id}")
                                        searchHistory.add(it?.name ?: "")
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }
                        }
                    } else {
                        item {
                            searchHistory.forEach {
                                ListItem(headlineContent = { Text(text = it) },
                                    modifier = Modifier.clickable { query = it },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    leadingContent = {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "History"
                                        )
                                    })
                            }
                        }
                    }
                    if (query.isNotBlank()) {
                        item {
                            ListItem(headlineContent = { Text(text = "Search the web for $query") },
                                modifier = Modifier.clickable {
                                    launchBrowser(
                                        context, query + " recipe"
                                    )
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Web Search"
                                    )
                                })
                        }
                    }
                }
            },
        )
    }

    if ((currentActivity == IngredientsDestination.route)) {
        CenterAlignedTopAppBar(title = { Text(text = "All $currentActivity") }, actions = {})
    }

    if (currentActivity == MoreDestination.route) {
        CenterAlignedTopAppBar(title = { Text(text = "More") }, actions = {})
    }

    if (currentActivity == CookingDestination.route) {
        CenterAlignedTopAppBar(title = { Text(text = "") }, actions = {

        }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                )
            }
        })
    }

    if (currentActivity == ShoppingListDestination.route) {
        TopAppBar(title = { Text(text = "Shopping List") }, actions = {}, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                )
            }
        })
    }

    if (currentActivity == ShoppingListDetailDestination.route) {
        TopAppBar(title = { Text(text = title) }, navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
                )
            }
        }, actions = {
            var showMenu by remember { mutableStateOf(false) }

            IconButton(onClick = { showMenu = !showMenu }) {
                Icon(
                    imageVector = Icons.Default.MoreVert, contentDescription = "More"
                )
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = !showMenu }) {
                DropdownMenuItem(text = { Text(text = "Delete") },
                    onClick = { activityType("delete") })
                DropdownMenuItem(text = { Text(text = "Rename") },
                    onClick = { activityType("rename") })
                DropdownMenuItem(text = { Text(text = "Mark Complete") },
                    onClick = { activityType("complete") })
            }
        })
    }
}

fun launchBrowser(context: Context, query: String) {
    val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
        putExtra("query", query)
    }

    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.duckduckgo.com/?q=$query&ia=web"))
        context.startActivity(browserIntent)
    }

}