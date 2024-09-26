package com.seyone22.cook.ui.common

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.seyone22.cook.data.model.Recipe
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
    searchAction: () -> Unit = {},
    recipeList: List<Recipe?> = listOf(),
    setOverlayStatus: (Boolean) -> Unit = {},
) {
    if (currentActivity == "search") {

        Log.d("TAG", "CookTopBar: $recipeList")
        var searchActive by remember { mutableStateOf(false) }
        var query by remember { mutableStateOf("") }

        val filteredRecipes =
            recipeList.filter { recipe -> (recipe?.name ?: "").contains(query, true) }

        val searchHistory = mutableListOf<String>()

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
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back"
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
        }, actions = {})
    }
}