package com.seyone22.cook.ui.screen.home.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.helper.ImageHelper
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.home.HomeViewModel
import com.seyone22.cook.ui.screen.ingredients.detail.DeleteConfirmationDialog
import com.seyone22.cook.ui.screen.ingredients.detail.IngredientDetailDestination
import java.io.File

object RecipeDetailDestination : NavigationDestination {
    override val route = "Recipe Details"
    override val titleRes = R.string.app_name
    override val routeId = 21
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
    backStackEntry: String,
    navController: NavController
) {
    // Call the ViewModel function to fetch ingredients when the screen is first displayed
    viewModel.fetchData()

    // Observe the ingredientList StateFlow to display ingredients
    val homeViewState by viewModel.homeViewState.collectAsState()

    val recipe = homeViewState.recipes.find { r -> r?.id.toString() == backStackEntry }
    val images = homeViewState.images.filter { i -> i?.id.toString() == backStackEntry }
    val instructions =
        homeViewState.instructions.filter { i -> i?.recipeId.toString() == backStackEntry }
    val recipeIngredients =
        homeViewState.recipeIngredients.filter { i -> i?.recipeId.toString() == backStackEntry }
    val measures = homeViewState.measures
    val ingredients = homeViewState.ingredients
    val imageHelper = ImageHelper(LocalContext.current)
    var bitmap by remember { mutableStateOf(createBitmap(1, 1)) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(images) {
        if (images.isNotEmpty()) {
            bitmap = File(images[0]?.imagePath).takeIf { it.exists() }
                ?.let { imageHelper.loadImageFromUri(it.toUri()) }!!
        }
    }

    if (showDeleteConfirmationDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                // Handle delete action
                showDeleteConfirmationDialog = false
                viewModel.deleteRecipe(recipe!!)
                navController.popBackStack()
            },
            onDismiss = {
                showDeleteConfirmationDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(0.dp),
                title = { Text(text = recipe?.name ?: "") },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
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

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                expanded = false
                                // Handle edit action
                                if (recipe != null) {
                                    navController.navigate("Edit Recipe/${recipe.id}")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expanded = false
                                // Handle delete action
                                showDeleteConfirmationDialog = true
                            }
                        )
                    }
                },
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            if (recipe != null) {
                item {
                    Column(modifier = Modifier.padding(8.dp, 0.dp)) {
                        HeaderImage(bitmap = bitmap.asImageBitmap(), recipe.name)
                        RecipeDetail(recipe)
                    }
                }
            }
            if (ingredients.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(8.dp, 0.dp)) {
                        IngredientsList(
                            navController = navController,
                            list = recipeIngredients,
                            measures = measures,
                            ingredients = ingredients
                        )
                    }
                }
            }
            if (instructions.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(8.dp, 0.dp)) {
                        InstructionList(list = instructions)
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderImage(bitmap: ImageBitmap, title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp))
        )

        Text(
            text = title,
            color = Color.White,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp, 0.dp, 16.dp, 16.dp)
        )
    }
}

@Composable
fun RecipeDetail(recipe: Recipe) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 16.dp)
    ) {
        Text(
            modifier = Modifier.padding(0.dp, 8.dp),
            text = "${recipe.cookTime} mins to cook | ${recipe.prepTime} mins to prepare | ${recipe.servingSize} servings",
            style = MaterialTheme.typography.labelLarge
        )
        Text(
            modifier = Modifier.padding(0.dp, 8.dp),
            text = recipe.description ?: "",
            style = MaterialTheme.typography.bodyLarge
        )
        TextButton(
            modifier = Modifier.padding(0.dp, 0.dp),
            content = {
                Icon(
                    modifier = Modifier.padding(0.dp,0.dp,4.dp,0.dp),
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                )
                Text(text = (recipe.reference ?: ""), maxLines = 1, overflow = TextOverflow.Ellipsis)
                      },
            onClick = {
                val urlIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(recipe.reference)
                )
                context.startActivity(urlIntent)
            }
        )
    }
}

@Composable
fun IngredientsList(
    navController: NavController,
    list: List<RecipeIngredient?>,
    measures: List<Measure?>,
    ingredients: List<Ingredient?>
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 0.dp, 0.dp, 8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp)
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "Ingredients",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            list.forEach { ingredient ->
                Row(
                    modifier = Modifier.padding(0.dp),
                ) {
                    val checked = remember { mutableStateOf(false) }
                    Checkbox(
                        modifier = Modifier.height(32.dp),
                        checked = checked.value,
                        onCheckedChange = { checked.value = !checked.value }
                    )
                    Text(
                        modifier = Modifier
                            .padding(4.dp, 0.dp, 16.dp, 0.dp)
                            .align(Alignment.CenterVertically)
                            .width(160.dp)
                            .clickable {
                                navController.navigate("${IngredientDetailDestination.route}/${ingredient?.ingredientId}")
                            },
                        text = ingredients.find { i -> i?.id == ingredient?.ingredientId }?.nameEn
                            ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        modifier = Modifier
                            .padding(4.dp, 0.dp, 16.dp, 0.dp)
                            .align(Alignment.CenterVertically),
                        text = (ingredient?.quantity.toString() + measures.find { m -> m?.id == ingredient?.measureId }?.abbreviation),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun InstructionList(list: List<Instruction?>) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp, 0.dp)
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text = "Instructions",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            list.forEach { instruction ->
                Row(
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 16.dp),
                ) {
                    Text(
                        modifier = Modifier
                            .padding(4.dp, 0.dp, 16.dp, 12.dp)
                            .align(Alignment.Top),
                        text = instruction?.stepNumber.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = instruction?.description ?: ""
                    )
                }
            }
        }
    }
}


@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Delete Recipe") },
        text = { Text(text = "Are you sure you want to delete this recipe?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}