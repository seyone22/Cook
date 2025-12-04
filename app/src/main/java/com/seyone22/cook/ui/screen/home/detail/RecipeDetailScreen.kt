package com.seyone22.cook.ui.screen.home.detail

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.DialogAction
import com.seyone22.cook.helper.RecipeFileHandler
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.CookToolbar
import com.seyone22.cook.ui.common.dialog.GenericDialog
import com.seyone22.cook.ui.common.dialog.action.AddAllToShoppingListDialogAction
import com.seyone22.cook.ui.common.dialog.action.DeleteDialogAction
import com.seyone22.cook.ui.common.dialog.action.ScaleRecipeDialogAction
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.crud.recipe.EditRecipeDestination
import com.seyone22.cook.ui.screen.home.HomeViewModel
import com.seyone22.cook.ui.screen.home.composables.ExpandableDescription
import com.seyone22.cook.ui.screen.home.composables.HeaderImage
import com.seyone22.cook.ui.screen.home.composables.IngredientsList
import com.seyone22.cook.ui.screen.home.composables.InstructionList
import com.seyone22.cook.ui.screen.home.composables.RecipeStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RecipeDetailDestination : NavigationDestination {
    override val route = "Recipe Details"
    override val titleRes = R.string.app_name
    override val routeId = 21
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    // Manual DI Injection
    viewModel: RecipeDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController,
    context: Context = LocalContext.current
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // Dialog State
    var activeDialogAction by remember { mutableStateOf<DialogAction?>(null) }

    activeDialogAction?.let { action ->
        GenericDialog(dialogAction = action, onDismiss = { activeDialogAction = null })
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val floatingToolbarScrollBehavior = FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = FloatingToolbarExitDirection.End)
    val tabs = listOf("Ingredients", "Instructions")

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .nestedScroll(floatingToolbarScrollBehavior),
        floatingActionButton = {
            if (uiState.recipe != null) {
                CookToolbar(
                    recipeLink = uiState.recipe!!.reference,
                    scrollBehavior = floatingToolbarScrollBehavior,
                    context = context,
                    onMadeItClicked = {
                        viewModel.incrementMakeCounter()
                        Toast.makeText(context, "You made it another time!", Toast.LENGTH_SHORT).show()
                    },
                    onScaleRecipeClicked = {
                        activeDialogAction = ScaleRecipeDialogAction(
                            initialEntry = uiState.scaleFactor,
                            itemName = "Scale Factor",
                            onAdd = { newFactor -> viewModel.updateScaleFactor(newFactor) }
                        )
                    },
                    onCookingModeClicked = {
                        navController.navigate("Cooking/${uiState.recipe!!.id}")
                    },
                    onAddToShoppingListClicked = {
                        activeDialogAction = AddAllToShoppingListDialogAction(
                            shoppingLists = uiState.shoppingLists,
                            itemName = "Shopping List",
                            onAdd = { listId -> viewModel.addAllToShoppingList(listId.toLong()) }
                        )
                    }
                )
            }
        },
        topBar = {
            MediumTopAppBar(
                title = { Text(text = uiState.recipe?.name ?: "") },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    Icon(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 24.dp)
                            .clickable { navController.popBackStack() },
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                    )
                },
                actions = {
                    // Share Button
                    IconButton(onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val recipe = uiState.recipe ?: return@launch

                            val zipFile = RecipeFileHandler.exportRecipe(
                                context = context,
                                recipe = recipe,
                                instructions = uiState.instructions,
                                recipeIngredients = uiState.ingredients,
                                ingredients = uiState.baseIngredients,
                                images = uiState.images
                            )

                            val uri = FileProvider.getUriForFile(
                                context, "${context.packageName}.provider", zipFile
                            )

                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                type = "application/zip"
                            }

                            launch(Dispatchers.Main) {
                                context.startActivity(Intent.createChooser(sendIntent, "Share Recipe"))
                            }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }

                    // Overflow Menu
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, "More options")
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    expanded = false
                                    uiState.recipe?.let { r ->
                                        navController.navigate("${EditRecipeDestination.route}/${r.id}")
                                    }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    expanded = false
                                    activeDialogAction = DeleteDialogAction(
                                        itemName = uiState.recipe?.name ?: "",
                                        onDelete = {
                                            viewModel.deleteRecipe()
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.recipe != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item {
                    HeaderImage(
                        images = uiState.images,
                        title = uiState.recipe!!.name,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                item {
                    ExpandableDescription(
                        text = uiState.recipe!!.description,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                item {
                    RecipeStats(
                        recipe = uiState.recipe!!,
                        cost = uiState.totalCost,
                        scaleFactor = uiState.scaleFactor,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                item {
                    val pagerState = rememberPagerState(pageCount = { 2 })
                    var selectedTabIndex by remember { mutableIntStateOf(0) }

                    // Sync tab state with pager
                    androidx.compose.runtime.LaunchedEffect(pagerState.currentPage) {
                        selectedTabIndex = pagerState.currentPage
                    }

                    SecondaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = TabRowDefaults.primaryContainerColor,
                        contentColor = TabRowDefaults.primaryContentColor,
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                text = { Text(title) },
                                selected = selectedTabIndex == index,
                                onClick = {
                                    selectedTabIndex = index
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(top = 16.dp)
                    ) { page ->
                        when (page) {
                            0 -> IngredientsList(
                                list = uiState.ingredients,
                                measures = uiState.measures,
                                ingredients = uiState.baseIngredients,
                                scaleFactor = uiState.scaleFactor,
                                serves = uiState.recipe!!.servingSize,
                                variants = emptyList(),
                                navController = navController,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                ingredientPrices = uiState.ingredientPrices
                            )
                            1 -> InstructionList(
                                list = uiState.instructions,
                                sections = uiState.instructionSections,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}