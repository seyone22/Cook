package com.seyone22.cook.ui.screen.crud.ingredient

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.data.model.IngredientDetails
import com.seyone22.cook.data.model.IngredientVariantDetails
import com.seyone22.cook.data.model.toIngredient
import com.seyone22.cook.data.model.toIngredientVariant
import com.seyone22.cook.helper.ImageStorageHelper
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination

object AddIngredientDestination : NavigationDestination {
    override val route = "Add Ingredient"
    override val titleRes = R.string.app_name
    override val routeId = 10
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientScreen(
    viewModel: IngredientOperationsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController,
    ingredientName: String = ""
) {
    viewModel.fetchData()
    val context = LocalContext.current

    val data by viewModel.addIngredientViewState.collectAsState()

    var showAltNames by remember { mutableStateOf(false) }

    var photos by remember { mutableStateOf(listOf<Uri>()) }
    var ingredient by remember { mutableStateOf(IngredientDetails()) }
    val variants = remember { mutableStateListOf(IngredientVariantDetails()) }

    // Launcher for selecting images
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            photos = photos + uri
        }
    }
    val imageHelper = ImageStorageHelper(context)

    LaunchedEffect(Unit) {
        if (ingredientName.isNotBlank()) {
            ingredient = ingredient.copy(nameEn = ingredientName)
        }
    }

    Scaffold(topBar = {
        TopAppBar(modifier = Modifier.padding(0.dp),
            title = { Text(text = "Add Ingredient") },
            navigationIcon = {
                Icon(
                    modifier = Modifier
                        .padding(16.dp, 0.dp, 24.dp, 0.dp)
                        .clickable { navController.popBackStack() },
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                )
            },
            actions = {
                Button(modifier = Modifier.padding(24.dp, 0.dp, 16.dp, 0.dp),
                    content = { Text("Save") },
                    onClick = {
                        viewModel.saveIngredient(
                            ingredient.toIngredient(),
                            variants.map { it.toIngredientVariant() },
                            photos,
                            context
                        )
                        navController.popBackStack()
                    })
            })
    }) {
        LazyColumn(modifier = Modifier.padding(it)) {
            item {
                Column(modifier = Modifier.padding(12.dp, 0.dp)) {
                    // Section for Photos
                    Column(
                        modifier = Modifier
                            .width(346.dp)
                            .padding(36.dp, 0.dp, 0.dp, 0.dp)
                    ) {
                        Text(text = "Photos")

                        LazyRow(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                photos.forEach { uri ->
                                    val bitmap = imageHelper.loadImageFromUri(uri)
                                    bitmap?.let {
                                        Row(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .size(100.dp)
                                                .align(Alignment.CenterHorizontally)
                                        ) {
                                            IconButton(onClick = { photos = photos - uri },
                                                modifier = Modifier
                                                    .align(Alignment.Top)
                                                    .size(24.dp),
                                                content = {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = null
                                                    )
                                                })
                                            Image(
                                                bitmap = it.asImageBitmap(),
                                                contentDescription = null,
                                                modifier = Modifier.size(100.dp)
                                            )

                                        }
                                    }
                                }
                            }
                        }

                        TextButton(onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                            Text(text = "Add Photo")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section for general data
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                            Icon(
                                modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                            )
                        }
                        OutlinedTextField(
                            modifier = Modifier.width(310.dp),
                            value = ingredient.nameEn,
                            onValueChange = { ingredient = ingredient.copy(nameEn = it) },
                            label = { Text("Name") },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )
                        Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                            IconButton(modifier = Modifier
                                .width(48.dp)
                                .height(48.dp),
                                onClick = { showAltNames = !showAltNames },
                                content = {
                                    Icon(
                                        imageVector = if (showAltNames) {
                                            Icons.Default.ArrowDropUp
                                        } else {
                                            Icons.Default.ArrowDropDown
                                        },
                                        contentDescription = null,
                                    )
                                })
                        }
                    }

                    // Collapsible section for alternate names
                    if (showAltNames) {
                        Column(
                            modifier = Modifier
                                .width(346.dp)
                                .padding(36.dp, 0.dp, 0.dp, 0.dp)
                        ) {
                            OutlinedTextField(
                                value = ingredient.nameSi,
                                onValueChange = { ingredient = ingredient.copy(nameSi = it) },
                                label = { Text("Name (Sinhala)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                            )
                            OutlinedTextField(
                                value = ingredient.nameTa,
                                onValueChange = { ingredient = ingredient.copy(nameTa = it) },
                                label = { Text("Name (Tamil)") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                            )
                        }
                    }

                    // Section for description
                    Column(
                        modifier = Modifier
                            .width(346.dp)
                            .padding(36.dp, 0.dp, 0.dp, 0.dp)
                    ) {
                        OutlinedTextField(
                            value = ingredient.description,
                            onValueChange = { ingredient = ingredient.copy(description = it) },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section for Variants
                    variants.forEachIndexed { index, variant ->
                        var measuresExpanded by remember { mutableStateOf(false) }

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                                    Icon(
                                        modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                                        imageVector = Icons.Outlined.Tag,
                                        contentDescription = null,
                                    )
                                }
                                OutlinedTextField(
                                    modifier = Modifier.width(310.dp),
                                    value = variant.variantName,
                                    onValueChange = { newVariantName ->
                                        variants[index] = variant.copy(variantName = newVariantName)
                                    },
                                    label = { Text("Name") },
                                )
                                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                                    IconButton(modifier = Modifier
                                        .width(48.dp)
                                        .height(48.dp),
                                        onClick = { variants.remove(variant) },
                                        content = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                            )
                                        })
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .width(346.dp)
                                    .padding(36.dp, 0.dp, 0.dp, 0.dp)
                            ) {
                                OutlinedTextField(
                                    value = variant.brand.toString(),
                                    onValueChange = { newVariantBrand ->
                                        variants[index] = variant.copy(brand = newVariantBrand)
                                    },
                                    label = { Text("Brand") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = variant.type.toString(),
                                    onValueChange = { newVariantType ->
                                        variants[index] = variant.copy(type = newVariantType)
                                    },
                                    label = { Text("Purchased From") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row {
                                    OutlinedTextField(
                                        value = variant.price.toString(),
                                        onValueChange = { newVariantPrice ->
                                            variants[index] = variant.copy(price = newVariantPrice)
                                        },
                                        label = { Text("Price") },
                                        modifier = Modifier
                                            .width(140.dp)
                                            .padding(0.dp, 0.dp, 8.dp, 0.dp),
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            imeAction = ImeAction.Done,
                                            keyboardType = KeyboardType.Number
                                        )
                                    )
                                    OutlinedTextField(
                                        value = variant.quantity.toString(),
                                        onValueChange = { newVariantQuantity ->
                                            variants[index] =
                                                variant.copy(quantity = newVariantQuantity)
                                        },
                                        label = { Text("Per") },
                                        modifier = Modifier
                                            .width(84.dp)
                                            .padding(0.dp, 0.dp, 8.dp, 0.dp),
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            imeAction = ImeAction.Done,
                                            keyboardType = KeyboardType.Number
                                        )
                                    )
                                    ExposedDropdownMenuBox(expanded = measuresExpanded,
                                        onExpandedChange = {
                                            measuresExpanded = !measuresExpanded
                                        }) {
                                        OutlinedTextField(modifier = Modifier
                                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                                            .clickable(enabled = true) {
                                                measuresExpanded = true
                                            },
                                            value = data.measures.find { m -> m?.id?.toInt() == variant.unitId.toInt() }?.abbreviation
                                                ?: "",
                                            readOnly = true,
                                            onValueChange = { },
                                            label = { Text("") },
                                            singleLine = true,
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = measuresExpanded)
                                            })

                                        ExposedDropdownMenu(expanded = measuresExpanded,
                                            onDismissRequest = { measuresExpanded = false }) {
                                            data.measures.forEach { measure ->
                                                measure?.let {
                                                    DropdownMenuItem(text = { Text(measure.abbreviation) },
                                                        onClick = {
                                                            variants[index] =
                                                                variant.copy(unitId = measure.id)
                                                            measuresExpanded = false
                                                        })
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    TextButton(onClick = {
                        variants.add(
                            IngredientVariantDetails(
                                brand = "",
                                ingredientId = 0,
                                price = "",
                                type = "",
                                variantName = "",
                                quantity = "",
                                unitId = 0
                            )
                        )
                    }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                        Text(text = "Add Variant")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}