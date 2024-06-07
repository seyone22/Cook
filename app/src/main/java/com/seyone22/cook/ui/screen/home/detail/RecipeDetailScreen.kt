package com.seyone22.cook.ui.screen.home.detail

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.helper.DataHelper
import com.seyone22.cook.helper.ImageHelper
import com.seyone22.cook.helper.PriceHelper
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.home.HomeViewModel
import com.seyone22.cook.ui.screen.ingredients.detail.DeleteConfirmationDialog
import com.seyone22.cook.ui.screen.ingredients.detail.IngredientDetailDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    navController: NavController,
    context: Context = LocalContext.current
) {
    val imageHelper = ImageHelper(LocalContext.current)
    // Call the ViewModel function to fetch ingredients when the screen is first displayed
    viewModel.fetchData()

    // Observe the ingredientList StateFlow to display ingredients
    val homeViewState by viewModel.homeViewState.collectAsState()

    val recipe = homeViewState.recipes.find { r -> r?.id.toString() == backStackEntry }
    val variants = homeViewState.variants
    val images = homeViewState.images.filter { i -> i?.id.toString() == backStackEntry }
    val instructions =
        homeViewState.instructions.filter { i -> i?.recipeId.toString() == backStackEntry }
    val recipeIngredients =
        homeViewState.recipeIngredients.filter { i -> i?.recipeId.toString() == backStackEntry }

    val measures = homeViewState.measures
    val ingredients = homeViewState.ingredients
    var scaleFactor by remember { mutableDoubleStateOf(1.0) }
    var cost by remember { mutableDoubleStateOf(0.0) }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showScaleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(images) {
        if (images.isNotEmpty()) {
            bitmap = File(images[0]?.imagePath).takeIf { it.exists() }
                ?.let { imageHelper.loadImageFromUri(it.toUri()) }!!
        }
        scaleFactor = recipe?.servingSize?.toDouble() ?: -1.0
    }

    LaunchedEffect(key1 = scaleFactor) {
        cost = PriceHelper.getCostOfRecipe(recipeIngredients, variants, scaleFactor)
    }

    if (showDeleteConfirmationDialog) {
        DeleteConfirmationDialog(onConfirm = {
            // Handle delete action
            showDeleteConfirmationDialog = false
            viewModel.deleteRecipe(recipe!!)
            navController.popBackStack()
        }, onDismiss = {
            showDeleteConfirmationDialog = false
        })
    }

    if (showScaleDialog) {
        ScaleDialog(onConfirm = { sF ->
            // Handle scale action
            scaleFactor = sF
            showScaleDialog = false
        }, onDismiss = {
            showScaleDialog = false
        })
    }

    Scaffold(topBar = {
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
                    val dataHelper = DataHelper()
                    // Handle share action
                    CoroutineScope(Dispatchers.Main).launch {
                        val zipFile = dataHelper.exportRecipe(context, recipe!!, instructions, recipeIngredients, images )
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", zipFile)

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
                        imageVector = Icons.Default.MoreVert, contentDescription = "More options"
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
                        expanded = false
                        // Handle delete action
                        showDeleteConfirmationDialog = true
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
                    Column(modifier = Modifier.padding(8.dp, 0.dp)) {
                        HeaderImage(bitmap = bitmap, recipe.name)
                        RecipeDetail(viewModel, recipe, cost, scaleFactor, onScaleClick = { showScaleDialog = true })
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
                            ingredients = ingredients,
                            scaleFactor = scaleFactor,
                            serves = recipe?.servingSize ?: -1,
                            variants = variants
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
fun HeaderImage(bitmap: Bitmap?, title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(24.dp))
            )
        } else {
            val image: Painter = painterResource(id = R.drawable.placeholder)
            Image(
                painter = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        Text(
            text = title,
            color = if (bitmap!=null) Color.White else Color.Black,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier
                .padding(16.dp, 0.dp, 16.dp, 16.dp)
                .align(Alignment.BottomStart)
        )
    }

}

@Composable
fun RecipeDetail(viewModel: HomeViewModel, recipe: Recipe, cost: Double, scaleFactor: Double, onScaleClick: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 16.dp)
    ) {
        RecipeOptionRow(
            viewModel = viewModel, recipe = recipe, context = context, onScaleClick = onScaleClick
        )
        LazyRow(
            verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)
        ) {
            item {
                Row {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.height(20.dp)
                    )
                    Text(
                        text = "Cook ${recipe.cookTime}min",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                    )
                }
            }
            item {
                Row {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.height(20.dp)
                    )
                    Text(
                        text = "Prep ${recipe.prepTime}min",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                    )
                }
            }
            item {
                Row {
                    Icon(
                        imageVector = Icons.Default.RiceBowl,
                        contentDescription = null,
                        modifier = Modifier.height(20.dp)
                    )
                    Text(
                        text = "Serves ${scaleFactor.toInt()}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                    )
                }
            }
            item {
                Row {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.height(20.dp)
                    )
                    Text(
                        text = "${recipe.timesMade} times",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                    )
                }
            }
            item {
                Row {
                    Icon(
                        imageVector = Icons.Default.PriceCheck,
                        contentDescription = null,
                        modifier = Modifier.height(20.dp)
                    )
                    Text(
                        text = "Rs. $cost",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
        if (!recipe.description.isNullOrEmpty()) {
            Text(
                modifier = Modifier.padding(0.dp, 8.dp),
                text = recipe.description,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally),
                text = "No description given.",
                fontStyle = FontStyle.Italic,
                color = Color.Gray
            )
        }
        if (recipe.reference?.isNotBlank() == true && recipe.reference.isNotEmpty()) {
            TextButton(modifier = Modifier.padding(0.dp, 0.dp), content = {
                Icon(
                    modifier = Modifier.padding(0.dp, 0.dp, 4.dp, 0.dp),
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                )
                Text(
                    text = (recipe.reference), maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }, onClick = {
                val urlIntent = Intent(
                    Intent.ACTION_VIEW, Uri.parse(recipe.reference)
                )
                context.startActivity(urlIntent)
            })
        }
    }
}

@Composable
fun IngredientsList(
    navController: NavController,
    list: List<RecipeIngredient?>,
    measures: List<Measure?>,
    ingredients: List<Ingredient?>,
    variants: List<IngredientVariant?>,
    scaleFactor: Double,
    serves: Int
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
            list.forEach { recipeIngredient ->
                Row(
                    modifier = Modifier.padding(0.dp),
                ) {
                    val checked = remember { mutableStateOf(ingredients.find { i -> i?.id == recipeIngredient?.ingredientId }?.stocked ?: false) }
                    val quantity : Double = ((recipeIngredient?.quantity?.div(serves))?.times(scaleFactor)) ?: 0.0
                    var price by remember { mutableDoubleStateOf(0.0) }

                    LaunchedEffect(key1 = quantity) {
                        price = PriceHelper.getCheapestPrice(recipeIngredient?.ingredientId, variants, quantity)
                    }

                    Checkbox(modifier = Modifier.height(32.dp),
                        enabled = !(ingredients.find { i -> i?.id == recipeIngredient?.ingredientId }?.stocked ?: false),
                        checked = checked.value,
                        onCheckedChange = { checked.value = !checked.value })
                    Text(modifier = Modifier
                        .padding(4.dp, 0.dp, 16.dp, 0.dp)
                        .align(Alignment.CenterVertically)
                        .width(120.dp)
                        .clickable {
                            navController.navigate("${IngredientDetailDestination.route}/${recipeIngredient?.ingredientId}")
                        },
                        text = ingredients.find { i -> i?.id == recipeIngredient?.ingredientId }?.nameEn
                            ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        modifier = Modifier
                            .padding(4.dp, 0.dp, 16.dp, 0.dp)
                            .align(Alignment.CenterVertically),
                        text = "${String.format("%.2f", quantity)} ${measures.find { m -> m?.id == recipeIngredient?.measureId }?.abbreviation}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        modifier = Modifier
                            .padding(4.dp, 0.dp, 16.dp, 0.dp)
                            .align(Alignment.CenterVertically),
                        text = "Rs.${String.format("%.2f", price)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun RecipeOptionRow(viewModel: HomeViewModel, context: Context, recipe: Recipe, onScaleClick: () -> Unit) {
    LazyRow {
        item {
            AssistChip(modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp), onClick = {
                viewModel.incrementMakeCounter(recipe.id)
                viewModel.fetchData()
                Toast.makeText(context, "You made it another time!", Toast.LENGTH_SHORT).show()
            }, label = { Text("I made it!") }, leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ThumbUpOffAlt, contentDescription = null
                )
            })
        }
        item {
            AssistChip(modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                onClick = { onScaleClick() },
                label = { Text("Scale Recipe") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                        contentDescription = null
                    )
                })
        }
        item {
            AssistChip(modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                onClick = { /*TODO*/ },
                label = { Text("Cooking Mode") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FitScreen, contentDescription = null
                    )
                })
        }
        item {
            AssistChip(modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                onClick = { /*TODO*/ },
                label = { Text("Add all to Shopping list") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart, contentDescription = null
                    )
                })
        }
    }
}


@Composable
fun InstructionList(list: List<Instruction?>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
    AlertDialog(onDismissRequest = { onDismiss() },
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
        })
}

@Composable
fun ScaleDialog(onConfirm: (Double) -> Unit, onDismiss: () -> Unit) {
    var scaleFactor by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "How many people are we serving?") },
        text = {
            TextField(
                value = scaleFactor,
                onValueChange = { sF -> scaleFactor = sF },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                label = { Text("People count") }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(scaleFactor.toDouble()) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// TODO: View .recipe files, before importing
// TODO: Import dialog in settings
// TODO: Cooking view, TTS and STT capability
// TODO: Shopping list, STT and price guides
