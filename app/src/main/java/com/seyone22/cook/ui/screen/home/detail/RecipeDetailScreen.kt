package com.seyone22.cook.ui.screen.home.detail

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.seyone22.cook.helper.PriceHelper
import com.seyone22.cook.helper.RecipeFileHandler
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.CookToolbar
import com.seyone22.cook.ui.common.dialog.action.AddAllToShoppingListDialogAction
import com.seyone22.cook.ui.common.dialog.action.DeleteDialogAction
import com.seyone22.cook.ui.common.dialog.action.ScaleRecipeDialogAction
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.home.HomeViewModel
import com.seyone22.cook.ui.screen.home.composables.ExpandableDescription
import com.seyone22.cook.ui.screen.home.composables.HeaderImage
import com.seyone22.cook.ui.screen.home.composables.IngredientsList
import com.seyone22.cook.ui.screen.home.composables.InstructionList
import com.seyone22.cook.ui.screen.home.composables.RecipeStats
import com.seyone22.expensetracker.ui.common.dialogs.GenericDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

object RecipeDetailDestination : NavigationDestination {
    override val route = "Recipe Details"
    override val titleRes = R.string.app_name
    override val routeId = 21
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    backStackEntry: String,
    navController: NavController,
    context: Context = LocalContext.current,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {
    // Call the ViewModel function to fetch ingredients when the screen is first displayed
    viewModel.fetchData()

    // Observe the ingredientList StateFlow to display ingredients
    val homeViewState by viewModel.homeViewState.collectAsState()

    val recipe = homeViewState.recipes.find { r -> r?.id.toString() == backStackEntry }
    val variants = homeViewState.variants
    val images = homeViewState.images.filter { i -> i?.recipeId == UUID.fromString(backStackEntry) }
    val instructions =
        homeViewState.instructions.filter { i -> i?.recipeId.toString() == backStackEntry }
    val instructionSections =
        homeViewState.instructionSections.filter { i -> i?.recipeId.toString() == backStackEntry }
    val recipeIngredients =
        homeViewState.recipeIngredients.filter { i -> i?.recipeId.toString() == backStackEntry }
    val measures = homeViewState.measures
    val ingredients = homeViewState.ingredients

    var scaleFactor by remember { mutableDoubleStateOf(1.0) }
    var cost by remember { mutableDoubleStateOf(0.0) }

    val currentDialog by viewModel.currentDialog
    currentDialog?.let {
        GenericDialog(dialogAction = it, onDismiss = { viewModel.dismissDialog() })
    }

    val tabs = listOf("Ingredients", "Instructions")

    LaunchedEffect(recipe) {
        scaleFactor = recipe?.servingSize?.toDouble() ?: -1.0
    }
    LaunchedEffect(key1 = scaleFactor) {
        cost = PriceHelper.getCostOfRecipe(recipeIngredients, variants, scaleFactor)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val floatingToolbarScrollBehavior =
        FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = FloatingToolbarExitDirection.End)

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .nestedScroll(floatingToolbarScrollBehavior), floatingActionButton = {
            CookToolbar(
                recipeLink = recipe?.reference,
                scrollBehavior = floatingToolbarScrollBehavior,
                onMadeItClicked = {
                    viewModel.incrementMakeCounter(recipe?.id!!)
                    viewModel.fetchData()
                    Toast.makeText(context, "You made it another time!", Toast.LENGTH_SHORT).show()
                },
                onScaleRecipeClicked = {
                    viewModel.showDialog(
                        ScaleRecipeDialogAction(
                            onAdd = { sF ->
                                scaleFactor = sF
                            }, initialEntry = scaleFactor, itemName = "Scale Factor"
                        )
                    )
                },
                onCookingModeClicked = {
                    navController.navigate("Cooking/${recipe?.id!!}")
                },
                onAddToShoppingListClicked = {
                    viewModel.showDialog(
                        AddAllToShoppingListDialogAction(
                            onAdd = {
                                viewModel.addAllToShoppingList(recipeIngredients, it.toLong())
                            },
                            shoppingLists = homeViewState.shoppingLists,
                            itemName = "Shopping List"
                        )
                    )
                },
                context = context
            )
        }, topBar = {
            MediumTopAppBar(
                modifier = Modifier.padding(0.dp),
                title = { Text(text = recipe?.name ?: "") },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    Icon(
                        modifier = Modifier
                            .padding(16.dp, 0.dp, 24.dp, 0.dp)
                            .clickable { navController.popBackStack() },
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                },
                actions = @Composable {
                    // Share button
                    IconButton(onClick = {
                        // Handle share action
                        CoroutineScope(Dispatchers.Main).launch {
                            val recipeIngredientIds =
                                recipeIngredients.map { it?.ingredientId } // Get a list of ingredient IDs from recipeIngredients

                            val ingredientsWithMatchingIds = ingredients.filter { ingredient ->
                                recipeIngredientIds.contains(ingredient?.id)
                            }

                            val zipFile = RecipeFileHandler.exportRecipe(
                                context,
                                recipe!!,
                                instructions,
                                recipeIngredients,
                                ingredientsWithMatchingIds,
                                images
                            )
                            val uri = FileProvider.getUriForFile(
                                context, "${context.packageName}.provider", zipFile
                            )

                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                type = "application/zip"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)

                            context.startActivity(Intent.createChooser(shareIntent, "Share Recipe"))
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }

                    // Overflow menu
                    var expanded by remember { mutableStateOf(false) }

                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = {
                            expanded = false
                            // Handle edit action
                            if (recipe != null) {
                                navController.navigate("Edit Recipe/${recipe.id}")
                            }
                        })
                        DropdownMenuItem(text = { Text("Delete") }, onClick = {
                            viewModel.showDialog(
                                DeleteDialogAction(
                                    itemName = recipe?.name ?: "", onDelete = {
                                        if (recipe != null) viewModel.deleteRecipe(recipe)
                                        navController.popBackStack()
                                    })
                            )
                        })
                    }
                },
            )
        }) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (recipe != null) {
                item {
                    HeaderImage(images = images, title = recipe.name, modifier = Modifier.padding(16.dp, 0.dp))
                }
                item {
                    ExpandableDescription(text = recipe.description, modifier = Modifier.padding(16.dp, 0.dp))
                }
                item {
                    RecipeStats(recipe = recipe, cost = cost, scaleFactor = scaleFactor, modifier = Modifier.padding(16.dp, 0.dp))
                }
            }

            item {
                val pagerState = rememberPagerState(pageCount = { 2 })
                var state by remember { mutableIntStateOf(0) }

                // Tab headers
                SecondaryTabRow(
                    state,
                    Modifier,
                    TabRowDefaults.primaryContainerColor,
                    TabRowDefaults.primaryContentColor,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            text = { Text(title) },
                            selected = state == index,
                            onClick = {
                                state = index
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            }
                        )
                    }
                }

                // Tab content
                HorizontalPager(
                    state = pagerState,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxHeight().padding(top = 16.dp)
                ) { page ->
                    state = page
                    when (page) {
                        0 -> {
                            // Ingredients tab
                            IngredientsList(
                                list = recipeIngredients,
                                measures = measures,
                                ingredients = ingredients,
                                scaleFactor = scaleFactor,
                                serves = recipe?.servingSize ?: 1,
                                variants = variants,
                                navController = navController,
                                modifier = Modifier.padding(16.dp, 0.dp)
                            )
                        }

                        1 -> {
                            // Instructions tab
                            InstructionList(
                                list = instructions,
                                sections = instructionSections,
                                modifier = Modifier.padding(16.dp, 0.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}