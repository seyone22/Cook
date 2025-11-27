package com.seyone22.cook.ui.screen.ingredients.detail

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.seyone22.cook.R
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientImage
import com.seyone22.cook.data.model.IngredientProduct
import com.seyone22.cook.data.model.IngredientProductDetails
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.ShoppingList
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.common.dialog.action.DeleteDialogAction
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.ingredients.IngredientsViewModel
import com.seyone22.expensetracker.ui.common.dialogs.GenericDialog
import kotlinx.coroutines.CoroutineScope
import java.util.UUID

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
    viewModel.fetchData()

    val currentDialog by viewModel.currentDialog
    currentDialog?.let {
        GenericDialog(dialogAction = it, onDismiss = { viewModel.dismissDialog() })
    }

    // Observe the ingredientList StateFlow to display ingredients
    val ingredientsViewState by viewModel.ingredientsViewState.collectAsState()

    val ingredient =
        ingredientsViewState.ingredients.find { i -> i?.foodDbId.toString() == backStackEntry }
    val images =
        ingredientsViewState.ingredientImages.filter { i -> i?.ingredientId.toString() == backStackEntry }
    val variants =
        ingredientsViewState.variants.filter { i -> i?.ingredientId.toString() == backStackEntry }
    val measures = ingredientsViewState.measures

    Scaffold(topBar = {
        TopAppBar(
            modifier = Modifier.padding(0.dp),
            title = { Text(text = ingredient?.name ?: "") },
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
                        imageVector = Icons.Default.MoreVert, contentDescription = "More options"
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
                        viewModel.showDialog(
                            DeleteDialogAction(
                                itemName = ingredient?.name ?: "", onDelete = {

                                })
                        )
                    })
                    DropdownMenuItem(text = { Text("Update") }, onClick = {
                        if (ingredient != null) {
                            viewModel.updateIngredient(ingredient.foodDbId)
                        } else {
                            Toast.makeText(context, "Ingredient not found", Toast.LENGTH_SHORT).show()
                        }
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
                        if (images.isNotEmpty()) {
                            HeaderImage(images = images)
                        }
                        IngredientOptionRow(
                            viewModel,
                            ingredientsViewState.shoppingLists,
                            ingredient,
                            ingredientsViewState.measures
                        )
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
fun DeleteDialogAction(itemName: String, onDelete: () -> Unit) {
    TODO("Not yet implemented")
}

@Composable
fun IngredientOptionRow(
    viewModel: IngredientsViewModel,
    shoppingLists: List<ShoppingList?>,
    ingredient: Ingredient,
    measures: List<Measure?>
) {
    var x: Boolean by remember { mutableStateOf(true) }

    var showSubstituteDialog by remember { mutableStateOf(false) }
    var showVariantDialog by remember { mutableStateOf(false) }
    var showAddShoppingListDialog by remember { mutableStateOf(false) }

    if (showVariantDialog) {
        NewVariantDialog(measures = measures, onConfirm = {
            viewModel.addVariant(ingredient.foodDbId, variant = it)
            showVariantDialog = false
            viewModel.fetchData()
        }, onDismiss = { showVariantDialog = false })
    }
    if (showSubstituteDialog) {
        NewSubstituteDialog(
            onConfirm = { showSubstituteDialog = false },
            onDismiss = { showSubstituteDialog = false })
    }
    if (showAddShoppingListDialog) {
        AddToShoppingListDialog(
            ingredientId = ingredient.id,
            shoppingLists = shoppingLists,
            measures = measures,
            onConfirm = {
                viewModel.addToShoppingList(it)
                showAddShoppingListDialog = false
            },
            onDismiss = { showAddShoppingListDialog = false })
    }

    LazyRow {
        item {
            AssistChip(modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp), onClick = {
                showAddShoppingListDialog = true
            }, label = { Text("Add to Shopping list") }, leadingIcon = {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart, contentDescription = null
                )
            })
        }
        item {
            AssistChip(
                modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                onClick = { showSubstituteDialog = true },
                label = { Text("Add Substitute") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AlternateEmail, contentDescription = null
                    )
                })
        }
        item {
            AssistChip(modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp), onClick = {
                showVariantDialog = true
            }, label = { Text("Add Variant") }, leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Tag, contentDescription = null
                )
            })
        }
    }
}

@Composable
fun IngredientDetails(ingredient: Ingredient) {
    Column(
        modifier = Modifier.padding(8.dp, 0.dp, 8.dp, 16.dp)
    ) {
        if ((ingredient.comment != null) and (ingredient.comment?.isNotBlank() == true)) {
            Text(text = ingredient.comment!!)
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                    text = "No comment given.",
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderImage(images: List<IngredientImage?>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        if (images.isEmpty()) {
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
        } else {
            HorizontalUncontainedCarousel(
                state = rememberCarouselState { images.size },
                itemWidth = if (images.size > 1) 320.dp else 400.dp,
                itemSpacing = 8.dp,
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(221.dp),
            ) { i ->
                AsyncImage(
                    model = images[i]?.imagePath,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
            }
        }
    }
}

@Composable
fun VariantsList(list: List<IngredientProduct?>, measures: List<Measure?>) {
    Column {
        Text(
            modifier = Modifier.padding(8.dp),
            text = "Variants",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        list.forEach { variant ->
            if (variant?.productName?.isNotBlank()!!) {
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
fun VariantCard(variant: IngredientProduct, measures: List<Measure?>) {
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
                    text = variant.productName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 16.dp, 0.dp)
                        .align(Alignment.CenterHorizontally)
                        .width(160.dp),
                    text = "${variant.item_unit} | ${variant.source}",
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
                    text = ("Rs.${variant.price} per ${variant.quantity}${variant.item_unit}"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewVariantDialog(
    measures: List<Measure?>, onConfirm: (IngredientProductDetails) -> Unit, onDismiss: () -> Unit
) {
    var variant by remember { mutableStateOf(IngredientProductDetails()) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Add new Variant") },
        text = {
            Column {
                // Section for Variants
                var measuresExpanded by remember { mutableStateOf(false) }

                Column {
                    OutlinedTextField(
                        modifier = Modifier.width(310.dp),
                        value = variant.variantName,
                        onValueChange = { newVariantName ->
                            variant = variant.copy(variantName = newVariantName)
                        },
                        label = { Text("Name") },
                    )
                    Column(
                        modifier = Modifier
                            .width(346.dp)
                            .padding(0.dp, 0.dp, 0.dp, 0.dp)
                    ) {
                        OutlinedTextField(
                            value = variant.brand,
                            onValueChange = { newVariantBrand ->
                                variant = variant.copy(brand = newVariantBrand)
                            },
                            label = { Text("Brand") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = variant.type,
                            onValueChange = { newVariantType ->
                                variant = variant.copy(type = newVariantType)
                            },
                            label = { Text("Purchased From") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row {
                            OutlinedTextField(
                                value = variant.price,
                                onValueChange = { newVariantPrice ->
                                    variant = variant.copy(price = newVariantPrice)
                                },
                                label = { Text("Price") },
                                modifier = Modifier
                                    .width(140.dp)
                                    .padding(0.dp, 0.dp, 8.dp, 0.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done, keyboardType = KeyboardType.Number
                                )
                            )
                            OutlinedTextField(
                                value = variant.quantity,
                                onValueChange = { newVariantQuantity ->
                                    variant = variant.copy(quantity = newVariantQuantity)
                                },
                                label = { Text("Per") },
                                modifier = Modifier
                                    .width(84.dp)
                                    .padding(0.dp, 0.dp, 8.dp, 0.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done, keyboardType = KeyboardType.Number
                                )
                            )

                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(variant) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        })
}

@Composable
fun NewSubstituteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Add new Substitute") },
        text = {

        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToShoppingListDialog(
    ingredientId: UUID,
    shoppingLists: List<ShoppingList?>,
    measures: List<Measure?>,
    onConfirm: (ShoppingListItem) -> Unit,
    onDismiss: () -> Unit
) {
    var shoppingListExpanded by remember { mutableStateOf(false) }
    var measuresExpanded by remember { mutableStateOf(false) }

    var selectedShoppingListIndex by remember { mutableIntStateOf(0) }
    var quantity by remember { mutableStateOf("") }
    var selectedMeasureId by remember { mutableLongStateOf(-1) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Add to a Shopping List") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ExposedDropdownMenuBox(expanded = shoppingListExpanded, onExpandedChange = {
                        shoppingListExpanded = !shoppingListExpanded
                    }) {
                        OutlinedTextField(
                            modifier = Modifier
                                .padding(
                                    0.dp, 0.dp, 8.dp, 0.dp
                                )
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                                .clickable(enabled = true) {
                                    shoppingListExpanded = true
                                },
                            value = if (shoppingLists.isNotEmpty()) {
                                shoppingLists[selectedShoppingListIndex]?.name ?: ""
                            } else {
                                ""
                            },
                            readOnly = true,
                            onValueChange = { },
                            label = { Text("") },
                            singleLine = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = shoppingListExpanded)
                            })

                        ExposedDropdownMenu(
                            expanded = shoppingListExpanded,
                            onDismissRequest = { shoppingListExpanded = false }) {
                            shoppingLists.forEachIndexed { index, shoppingList ->
                                shoppingList?.let {
                                    DropdownMenuItem(text = { Text(shoppingList.name) }, onClick = {
                                        selectedShoppingListIndex = index
                                        shoppingListExpanded = false
                                    })
                                }
                            }
                        }
                    }
                    Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                        IconButton(
                            modifier = Modifier
                                .width(48.dp)
                                .height(48.dp),
                            onClick = { /*TODO*/ },
                            content = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                )
                            })
                    }
                }
                Row {
                    OutlinedTextField(
                        modifier = Modifier
                            .width(64.dp)
                            .padding(0.dp, 0.dp, 8.dp, 0.dp),
                        value = quantity,
                        singleLine = true,
                        onValueChange = { newQty ->
                            quantity = newQty
                        },
                        label = { Text("No") },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                        )
                    )
                    ExposedDropdownMenuBox(expanded = measuresExpanded, onExpandedChange = {
                        measuresExpanded = !measuresExpanded
                    }) {
                        OutlinedTextField(
                            modifier = Modifier
                                .padding(
                                    0.dp, 0.dp, 8.dp, 0.dp
                                )
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                                .width(80.dp)
                                .clickable(enabled = true) {
                                    measuresExpanded = true
                                },
                            value = measures.find { m -> m?.id?.toInt() == selectedMeasureId.toInt() }?.abbreviation
                                ?: "",
                            readOnly = true,
                            onValueChange = { },
                            label = { Text("") },
                            singleLine = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = measuresExpanded)
                            })

                        ExposedDropdownMenu(
                            expanded = measuresExpanded,
                            onDismissRequest = { measuresExpanded = false }) {
                            measures.forEach { measure ->
                                measure?.let {
                                    DropdownMenuItem(
                                        text = { Text(measure.abbreviation) },
                                        onClick = {
                                            selectedMeasureId = measure.id
                                            measuresExpanded = false
                                        })
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                shoppingLists[selectedShoppingListIndex]?.id?.let {
                    onConfirm(
                        ShoppingListItem(
                            0,
                            shoppingLists[selectedShoppingListIndex]?.id ?: -1,
                            ingredientId,
                            quantity.toDouble(),
                            selectedMeasureId
                        )
                    )
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        })
}