package com.seyone22.cook.ui.screen.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination

object SearchDestination : NavigationDestination {
    override val route = "Search"
    override val titleRes = R.string.app_name
    override val routeId = 6
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController
) {
    viewModel.fetchData()

    val data by viewModel.searchViewState.collectAsState()
    var query by remember { mutableStateOf("") }

    var searchData: List<Recipe?> by remember { mutableStateOf(emptyList()) }

    Column {
        val onActiveChange = { }
        val colors1 = SearchBarDefaults.colors()
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = {
                        query = it
                        searchData = data.recipes.filter { r ->
                            r?.name?.contains(query, ignoreCase = true) == true
                        }
                        if (query.isEmpty()) {
                            searchData = listOf()
                        }
                    },
                    onSearch = {
                        searchData = data.recipes.filter { r ->
                            r?.name?.contains(query, ignoreCase = true) == true
                        }
                    },
                    expanded = true,
                    onExpandedChange = onActiveChange,
                    enabled = true,
                    placeholder = { Text(text = "Search for recipes") },
                    leadingIcon = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.clickable { navController.popBackStack() })
                    },
                    trailingIcon = null,
                    colors = colors1.inputFieldColors,
                    interactionSource = null,
                )
            },
            expanded = true,
            onExpandedChange = onActiveChange,
            modifier = Modifier,
            shape = SearchBarDefaults.inputFieldShape,
            colors = colors1,
            tonalElevation = SearchBarDefaults.TonalElevation,
            shadowElevation = SearchBarDefaults.ShadowElevation,
            windowInsets = SearchBarDefaults.windowInsets,
            content = fun ColumnScope.() {
                LazyColumn {
                    items(count = searchData.size) { entry ->
                        ListItem(headlineContent = { Text(searchData[entry]?.name ?: "") },
                            supportingContent = {
                                if (searchData[entry]?.description?.isNotEmpty() == true) {
                                    Text(
                                        text = searchData[entry]?.description ?: "",
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            modifier = Modifier.clickable { navController.navigate("Recipe Details/${searchData[entry]?.id}") })
                    }
                }
            },
        )
    }
}