package com.seyone22.cook.ui.screen.crud.recipe

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.seyone22.cook.R
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeIngredientDetails
import com.seyone22.cook.data.model.Tag
import com.seyone22.cook.data.model.toRecipeIngredient
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination
import java.util.UUID

object AddRecipeDestination : NavigationDestination {
    override val route = "Add Recipe"
    override val titleRes = R.string.app_name
    override val routeId = 10
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    viewModel: RecipeOperationsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController
) {
    viewModel.fetchData()

    val data by viewModel.addRecipeViewState.collectAsState()

    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var photos by remember { mutableStateOf(listOf<Uri>()) }
    val instructions = remember {
        mutableStateListOf<Instruction>()
    }
    val recipeIngredients = remember {
        mutableStateListOf<RecipeIngredientDetails>()
    }
    // Launcher for selecting images
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            photos = photos + uri
        }
    }
    var recipeTags by remember { mutableStateOf(listOf<Tag>()) }

    Scaffold(topBar = {
        TopAppBar(modifier = Modifier.padding(0.dp),
            title = { Text(text = "Add Recipe") },
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
                        Log.d("TAG", "AddRecipeScreen: $photos")
                        viewModel.saveRecipe(
                            Recipe(
                                name = name,
                                description = description,
                                cookTime = if (cookTime.isEmpty()) 0 else cookTime.toInt(),
                                prepTime = if (prepTime.isEmpty()) 0 else prepTime.toInt(),
                                servingSize = if (servingSize.isEmpty()) 1 else servingSize.toInt(),
                                reference = reference
                            ),
                            photos,
                            instructions,
                            recipeIngredients.map { i -> i.toRecipeIngredient() },
                            recipeTags,
                            context
                        )
                        navController.popBackStack()
                    })
            })
    }) {
        LazyColumn(modifier = Modifier.padding(it)) {
            item {
                var tagsExpanded by remember { mutableStateOf(false) }
                var tagFilter by remember { mutableStateOf("") }
                val filteredTags = data.tags.filter {
                    (it?.name ?: "").contains(
                        tagFilter, true
                    )
                }

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
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.size(100.dp)
                                    )
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
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )
                    }

                    // Section for quick descriptions
                    Column(
                        modifier = Modifier
                            .width(346.dp)
                            .padding(36.dp, 0.dp, 0.dp, 0.dp)
                    ) {
                        Row {
                            OutlinedTextField(
                                value = prepTime,
                                onValueChange = { prepTime = it.filter { it.isDigit() } },
                                label = { Text("Prep") },
                                modifier = Modifier
                                    .width(107.dp)
                                    .padding(0.dp, 0.dp, 8.dp, 0.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                                )
                            )
                            OutlinedTextField(
                                value = cookTime,
                                onValueChange = { cookTime = it.filter { it.isDigit() } },
                                label = { Text("Cook") },
                                modifier = Modifier
                                    .width(107.dp)
                                    .padding(0.dp, 0.dp, 8.dp, 0.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                                )
                            )
                            OutlinedTextField(
                                value = servingSize,
                                onValueChange = { servingSize = it.filter { it.isDigit() } },
                                label = { Text("Serves") },
                                modifier = Modifier.width(107.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                                )
                            )
                        }
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            modifier = Modifier.width(310.dp),
                            value = reference,
                            onValueChange = { reference = it },
                            label = { Text("Reference URL") },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )

                        ExposedDropdownMenuBox(expanded = tagsExpanded, onExpandedChange = {
                            tagsExpanded = !tagsExpanded
                        }) {
                            OutlinedTextField(modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                                .clickable(enabled = true) {
                                    tagsExpanded = true
                                },
                                value = tagFilter,
                                readOnly = false,
                                onValueChange = { tagFilter = it },
                                label = { Text("Select a tag") },
                                singleLine = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = tagsExpanded)
                                })

                            ExposedDropdownMenu(expanded = tagsExpanded, onDismissRequest = { }) {
                                if (filteredTags.isNotEmpty()) {
                                    filteredTags.forEach { tag ->
                                        tag?.let {
                                            DropdownMenuItem(text = { Text(tag.name) }, onClick = {
                                                // Check if the tag is already in the list
                                                if (!recipeTags.any { it.id == tag.id }) {
                                                    // Add the new tag if it doesn't exist
                                                    recipeTags = recipeTags + tag
                                                }
                                                tagsExpanded = false
                                            })
                                        }
                                    }
                                } else {
                                    DropdownMenuItem(text = { Text("Add $tagFilter to database") },
                                        onClick = {
                                            navController.navigate("Add Tag/$tagFilter")
                                        })
                                }
                            }
                        }
                        LazyRow {
                            recipeTags.forEach { tag ->
                                item(key = tag.id) {
                                    FilterChip(
                                        modifier = Modifier.padding(end= 4.dp),
                                        selected = true,  // Chips are not selected by default
                                        onClick = {
                                            // Remove the tag from the list when clicked
                                            recipeTags = recipeTags.filter { it != tag }
                                        }, label = {
                                            Text(text = tag.name)
                                        }, trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,  // Close icon for the chip
                                                contentDescription = "Remove Tag"
                                            )
                                        })
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section for Recipe Ingredients
                    recipeIngredients.forEachIndexed { index, recipeIngredient ->
                        var measuresExpanded by remember { mutableStateOf(false) }
                        var ingredientExpanded by remember { mutableStateOf(false) }

                        var ingredientFilter by remember { mutableStateOf("") }
                        val filteredIngredients = data.ingredients.filter {
                            (it?.nameEn ?: "").contains(
                                ingredientFilter,
                                true
                            )
                        }

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
                                Row {
                                    ExposedDropdownMenuBox(
                                        expanded = ingredientExpanded,
                                        onExpandedChange = {
                                            ingredientExpanded = !ingredientExpanded
                                        }) {
                                        OutlinedTextField(modifier = Modifier
                                            .padding(
                                                0.dp, 0.dp, 8.dp, 0.dp
                                            )
                                            .width(156.dp)
                                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                                            .clickable(enabled = true) {
                                                ingredientExpanded = true
                                            },
                                            value = ingredientFilter,
                                            onValueChange = { v -> ingredientFilter = v },
                                            label = { Text("") },
                                            singleLine = true,
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = ingredientExpanded)
                                            })

                                        ExposedDropdownMenu(
                                            expanded = ingredientExpanded,
                                            onDismissRequest = { }
                                        ) {
                                            if (filteredIngredients.isNotEmpty()) {
                                                filteredIngredients.forEach { ingredient ->
                                                    ingredient?.let {
                                                        DropdownMenuItem(text = { Text(ingredient.nameEn) },
                                                            onClick = {
                                                                ingredientFilter = ingredient.nameEn
                                                                recipeIngredients[index] =
                                                                    recipeIngredient.copy(
                                                                        ingredientId = ingredient.id
                                                                    )
                                                                ingredientExpanded = false
                                                            })
                                                    }
                                                }
                                            } else {
                                                DropdownMenuItem(text = { Text("Add $ingredientFilter to database") },
                                                    onClick = {
                                                        navController.navigate("Add Ingredient/$ingredientFilter")
                                                    })
                                            }
                                        }
                                    }
                                    OutlinedTextField(
                                        modifier = Modifier
                                            .width(64.dp)
                                            .padding(0.dp, 0.dp, 8.dp, 0.dp),
                                        value = recipeIngredient.quantity,
                                        singleLine = true,
                                        onValueChange = { newQty ->
                                            recipeIngredients[index] =
                                                recipeIngredient.copy(quantity = newQty)
                                        },
                                        label = { Text("No") },
                                        keyboardOptions = KeyboardOptions.Default.copy(
                                            imeAction = ImeAction.Next,
                                            keyboardType = KeyboardType.Number
                                        )
                                    )
                                    ExposedDropdownMenuBox(expanded = measuresExpanded,
                                        onExpandedChange = {
                                            measuresExpanded = !measuresExpanded
                                        }) {
                                        OutlinedTextField(modifier = Modifier
                                            .padding(
                                                0.dp, 0.dp, 8.dp, 0.dp
                                            )
                                            .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                                            .width(80.dp)
                                            .clickable(enabled = true) {
                                                measuresExpanded = true
                                            },
                                            value = data.measures.find { m -> m?.id?.toInt() == recipeIngredient.measureId.toInt() }?.abbreviation
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
                                                            recipeIngredients[index] =
                                                                recipeIngredient.copy(measureId = measure.id)
                                                            measuresExpanded = false
                                                        })
                                                }
                                            }
                                        }
                                    }
                                }
                                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                                    IconButton(modifier = Modifier
                                        .width(48.dp)
                                        .height(48.dp),
                                        onClick = { recipeIngredients.remove(recipeIngredient) },
                                        content = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                            )
                                        })
                                }
                            }
                        }
                    }

                    TextButton(onClick = {
                        recipeIngredients.add(
                            RecipeIngredientDetails(
                                ingredientId = -1,
                                measureId = -1,
                                quantity = "",
                                recipeId = UUID.randomUUID()
                            )
                        )
                    }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                        Text(text = "Add Ingredient")
                    }

                    // Section for Instructions
                    instructions.forEachIndexed { index, instruction ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                                    Text(
                                        modifier = Modifier.padding(8.dp, 0.dp, 16.dp, 0.dp),
                                        text = instruction.stepNumber.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                OutlinedTextField(
                                    modifier = Modifier.width(310.dp),
                                    value = instruction.description,
                                    onValueChange = { newInstructionDescription ->
                                        instructions[index] =
                                            instruction.copy(description = newInstructionDescription)
                                    },
                                    label = { Text("Describe the step") },
                                )
                                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                                    IconButton(modifier = Modifier
                                        .width(48.dp)
                                        .height(48.dp),
                                        onClick = { instructions.remove(instruction) },
                                        content = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = null,
                                            )
                                        })
                                }
                            }
                        }
                    }
                    TextButton(onClick = {
                        instructions.add(
                            Instruction(
                                description = "",
                                stepNumber = instructions.size + 1,
                                recipeId = UUID.randomUUID()
                            )
                        )
                    }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                        Text(text = "Add Step")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}