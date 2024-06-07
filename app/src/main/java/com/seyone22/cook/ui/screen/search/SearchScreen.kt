package com.seyone22.cook.ui.screen.search

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
        SearchBar(
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
            placeholder = { Text(text = "Search for recipes") },
            active = true,
            onActiveChange = { },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.clickable { navController.popBackStack() })
            },
            content = {
                LazyColumn {
                    items(count = searchData.size) { entry ->
                        Log.d("TAG", "SearchScreen: ${searchData[entry]?.name ?: ""}")

                        ListItem(
                            headlineContent = { Text(searchData[entry]?.name ?: "") },
                            modifier = Modifier
                                .clickable { navController.navigate("Recipe Details/${searchData[entry]?.id}") }
                        )
                    }
                }
            }
        )
    }
}