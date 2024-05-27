package com.seyone22.cook.ui.screen.ingredients.detail

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.helper.ImageHelper
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.home.detail.DeleteConfirmationDialog
import com.seyone22.cook.ui.screen.home.detail.HeaderImage
import com.seyone22.cook.ui.screen.ingredients.IngredientsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

object IngredientDetailDestination : NavigationDestination {
    override val route = "Ingredient Details"
    override val titleRes = R.string.app_name
    override val routeId = 20
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailScreen(
    viewModel: IngredientsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    backStackEntry: String,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    context: Context = LocalContext.current,
    navController: NavController
) {
    // Call the ViewModel function to fetch ingredients when the screen is first displayed
    viewModel.fetchIngredientsAndImages()

    // Observe the ingredientList StateFlow to display ingredients
    val ingredientsViewState by viewModel.ingredientsViewState.collectAsState()

    val ingredient =
        ingredientsViewState.ingredients.find { i -> i?.id.toString() == backStackEntry }
    val images =
        ingredientsViewState.images.filter { i -> i?.ingredientId.toString() == backStackEntry }
    val variants =
        ingredientsViewState.variants.filter { i -> i?.ingredientId.toString() == backStackEntry }
    val measures = ingredientsViewState.measures

    val imageHelper = ImageHelper(LocalContext.current)

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(images) {
        if (images.isNotEmpty()) {
            bitmap = File(images[0]?.imagePath).takeIf { it.exists() }
                ?.let { imageHelper.loadImageFromUri(it.toUri()) }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(onConfirm = {
            if (ingredient != null) {
                coroutineScope.launch {
                    showDeleteDialog = false
                    if (viewModel.deleteIngredient(ingredient)) {
                        navController.popBackStack()
                        Toast.makeText(context, "Successfully Deleted!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            context,
                            "This ingredient is in use by some recipe!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        }, onDismiss = { showDeleteDialog = false })
    }

    Scaffold(topBar = {
        TopAppBar(
            modifier = Modifier.padding(0.dp),
            title = { Text(text = ingredient?.nameEn ?: "") },
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

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = {
                        expanded = false
                        // Handle edit action
                        if (ingredient != null) {
                            navController.navigate("Edit Ingredient/${ingredient.id}")
                        }
                    })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = {
                        expanded = false
                        // Handle delete action
                        showDeleteDialog = true
                    })
                }
            },
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        )
    }) {
        LazyColumn(
            modifier = Modifier.padding(it)
        ) {
            if (ingredient != null) {
                item {
                    Column(modifier = Modifier.padding(8.dp, 0.dp)) {
                        HeaderImage(bitmap = bitmap)
                        IngredientOptionRow()
                        IngredientDetails(ingredient)
                    }
                }
            }
            item {
                Column(modifier = Modifier.padding(8.dp, 0.dp)) {
                    VariantsList(variants, measures)
                }

            }
        }
    }
}

@Composable
fun IngredientOptionRow() {
    var x: Boolean by remember { mutableStateOf(false) }

    LazyRow(
    ) {
        if (x) {
            item {
                AssistChip(
                    modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                    onClick = { x = false },
                    label = { Text("Mark Ran Out") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null
                        )
                    }
                )
            }
        } else {
            item {
                AssistChip(
                    modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                    onClick = { x = true },
                    label = { Text("Mark Restocked") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null
                        )
                    }
                )
            }
        }
        item {
            AssistChip(
                modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                onClick = { /*TODO*/ },
                label = { Text("Add to Shopping list") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AddShoppingCart,
                        contentDescription = null
                    )
                }
            )
        }
        item {
            AssistChip(
                modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                onClick = { /*TODO*/ },
                label = { Text("Add Substitute") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AlternateEmail,
                        contentDescription = null
                    )
                }
            )
        }
        item {
            AssistChip(
                modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                onClick = { /*TODO*/ },
                label = { Text("Add Variant") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Composable
fun IngredientDetails(ingredient: Ingredient) {
    Column(
        modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 16.dp)
    ) {
        Text(text = ingredient.nameSi, style = MaterialTheme.typography.headlineSmall)
        Text(text = ingredient.nameTa, style = MaterialTheme.typography.headlineSmall)

        if ((ingredient.description != null) and (ingredient.description?.isNotBlank() == true) ) {
            Text(text = ingredient.description!!)
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    text = "No description given.",
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun HeaderImage(bitmap: Bitmap?) {

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
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
}

@Composable
fun VariantsList(list: List<IngredientVariant?>, measures: List<Measure?>) {
    Column {
        Text(
            modifier = Modifier.padding(8.dp),
            text = "Variants",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        list.forEach { variant ->
            if (variant?.variantName?.isNotBlank()!!) {
                VariantCard(variant = variant, measures = measures)
            }
        }
        if (list.isEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    text = "No variants found",
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun VariantCard(variant: IngredientVariant, measures: List<Measure?>) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 0.dp, 0.dp, 8.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 16.dp)
        ) {
            Column {
                Text(
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 16.dp, 0.dp)
                        .align(Alignment.CenterHorizontally)
                        .width(160.dp),
                    text = variant.variantName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 16.dp, 0.dp)
                        .align(Alignment.CenterHorizontally)
                        .width(160.dp),
                    text = "${variant.type} | ${variant.brand}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 16.dp, 0.dp)
                        .align(Alignment.End),
                    text = ("Rs.${variant.price} per ${variant.quantity}${measures.find { m -> m?.id == variant.unitId }?.abbreviation}"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = { onDismiss() },
        title = { Text(text = "Delete Ingredient") },
        text = { Text(text = "Are you sure you want to delete this ingredient?") },
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
