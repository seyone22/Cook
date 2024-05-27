package com.seyone22.cook.ui.screen.crud.recipe

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.model.RecipeIngredientDetails
import com.seyone22.cook.data.model.toRecipe
import com.seyone22.cook.data.model.toRecipeDetails
import com.seyone22.cook.data.model.toRecipeIngredient
import com.seyone22.cook.data.model.toRecipeIngredientDetails
import com.seyone22.cook.helper.ImageHelper
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination

object EditRecipeDestination : NavigationDestination {
    override val route = "Edit Recipe"
    override val titleRes = R.string.app_name
    override val routeId = 10
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeScreen(
    recipeId: Long,
    viewModel: RecipeOperationsViewModel = viewModel(factory = AppViewModelProvider.Factory),
    navController: NavController,
) {
    val context = LocalContext.current

    // Fetch existing recipe data based on the provided recipeId
    LaunchedEffect(recipeId) {
        viewModel.fetchData(recipeId)
    }

    val data by viewModel.addRecipeViewState.collectAsState()
    val dataRecipe = data.recipe
    val dataInstructions = data.instructions
    val dataImages = data.images
    val dataRecipeIngredients = data.recipeIngredients

    var recipe by remember { mutableStateOf(dataRecipe?.toRecipeDetails()) }
    var images by remember { mutableStateOf(listOf<RecipeImage>()) }
    var instructions by remember { mutableStateOf(listOf<Instruction>()) }
    var recipeIngredients by remember { mutableStateOf(listOf<RecipeIngredientDetails>()) }

    var showAltNames by remember { mutableStateOf(false) }

    // Populate fields with existing data when recipe data is loaded
    LaunchedEffect(dataRecipe) {
        dataRecipe?.let {
            recipe = it.toRecipeDetails()
        }
        images = dataImages.map { i -> i!! }
        instructions = dataInstructions.map { i -> i!! }
        recipeIngredients = dataRecipeIngredients.map { i -> i!!.toRecipeIngredientDetails() }
    }

    // Launcher for selecting images
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            images = images + RecipeImage(imagePath = uri.toString(), recipeId = recipeId)
        }
    }
    val imageHelper = ImageHelper(context)

    Scaffold(topBar = {
        TopAppBar(modifier = Modifier.padding(0.dp),
            title = { Text(text = "Edit Recipe") },
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
                        viewModel.updateRecipe(
                            recipe!!.toRecipe(),
                            images,
                            instructions.map { i -> i.copy(recipeId = recipeId) },
                            recipeIngredients.map { i -> i.copy(recipeId = recipeId).toRecipeIngredient() },
                            context
                        )
                        navController.popBackStack()
                    })
            })
    }) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
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
                                images.forEach { image ->
                                    val bitmap =
                                        imageHelper.loadImageFromUri(Uri.parse(image.imagePath))
                                    bitmap?.let {
                                        Row(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .size(100.dp)
                                                .align(Alignment.CenterHorizontally)
                                        ) {
                                            IconButton(onClick = { images = images - image },
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
                            modifier = Modifier.width(230.dp).padding(end = 8.dp),
                            value = recipe?.name ?: "",
                            onValueChange = { recipe = recipe?.copy(name = it) },
                            label = { Text("Name") },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            modifier = Modifier.width(80.dp),
                            value = recipe?.timesMade.toString() ?: "",
                            onValueChange = { recipe = recipe?.copy(timesMade = it) },
                            label = { Text("Count") },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )
                    }

                    // Section for quick descriptions
                    Column(
                        modifier = Modifier
                            .width(346.dp)
                            .padding(36.dp, 0.dp, 0.dp, 0.dp)
                    ) {
                        Row() {
                            OutlinedTextField(
                                value = recipe?.prepTime.toString(),
                                onValueChange = { recipe = recipe?.copy(prepTime = it) },
                                label = { Text("Prep") },
                                modifier = Modifier.width(107.dp).padding(0.dp, 0.dp, 8.dp, 0.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                                )
                            )
                            OutlinedTextField(
                                value = recipe?.cookTime.toString(),
                                onValueChange = { recipe = recipe?.copy(cookTime = it) },
                                label = { Text("Cook") },
                                modifier = Modifier.width(107.dp).padding(0.dp, 0.dp, 8.dp, 0.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                                )
                            )
                            OutlinedTextField(
                                value = recipe?.servingSize.toString(),
                                onValueChange = { recipe = recipe?.copy(servingSize = it) },
                                label = { Text("Serves") },
                                modifier = Modifier.width(107.dp),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                                )
                            )
                        }
                        OutlinedTextField(
                            value = recipe?.description ?: "",
                            onValueChange = { recipe = recipe?.copy(description = it) },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )
                        OutlinedTextField(
                            modifier = Modifier.width(310.dp),
                            value = recipe?.reference ?: "",
                            onValueChange = { recipe = recipe?.copy(reference = it) },
                            label = { Text("Reference URL") },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section for Recipe Ingredients
                    recipeIngredients.forEachIndexed { index, recipeIngredient ->
                        var measuresExpanded by remember { mutableStateOf(false) }
                        var ingredientExpanded by remember { mutableStateOf(false) }

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
                                            .padding(0.dp, 0.dp, 8.dp, 0.dp)
                                            .width(156.dp)
                                            .menuAnchor()
                                            .clickable(enabled = true) {
                                                ingredientExpanded = true
                                            },
                                            value = data.ingredients.find { m -> m?.id?.toInt() == recipeIngredient.ingredientId.toInt() }?.nameEn
                                                ?: "",
                                            readOnly = true,
                                            onValueChange = { },
                                            label = { Text("") },
                                            singleLine = true,
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = ingredientExpanded)
                                            })

                                        ExposedDropdownMenu(expanded = ingredientExpanded,
                                            onDismissRequest = { ingredientExpanded = false }) {
                                            data.ingredients.forEach { ingredient ->
                                                ingredient?.let {
                                                    DropdownMenuItem(text = { Text(ingredient.nameEn) },
                                                        onClick = {
                                                            recipeIngredients =
                                                                recipeIngredients.mapIndexed { i, recipeIngredient ->
                                                                    if (i == index) {
                                                                        recipeIngredient.copy(
                                                                            ingredientId = ingredient.id
                                                                        )
                                                                    } else {
                                                                        recipeIngredient
                                                                    }
                                                                }
                                                            ingredientExpanded = false
                                                        })
                                                }
                                            }
                                        }
                                    }
                                    OutlinedTextField(
                                        modifier = Modifier.width(64.dp).padding(0.dp, 0.dp, 8.dp, 0.dp),
                                        value = recipeIngredient.quantity.toString(),
                                        singleLine = true,
                                        onValueChange = { newQty ->
                                            recipeIngredients =
                                                recipeIngredients.mapIndexed { i, recipeIngredient ->
                                                    if (i == index) {
                                                        recipeIngredient.copy(quantity = newQty)
                                                    } else {
                                                        recipeIngredient
                                                    }
                                                }
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
                                            .padding(0.dp, 0.dp, 8.dp, 0.dp)
                                            .menuAnchor()
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
                                                            recipeIngredients =
                                                                recipeIngredients.mapIndexed { i, recipeIngredient ->
                                                                    if (i == index) {
                                                                        recipeIngredient.copy(
                                                                            measureId = measure.id
                                                                        )
                                                                    } else {
                                                                        recipeIngredient
                                                                    }
                                                                }
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
                                        onClick = {
                                            recipeIngredients = recipeIngredients - recipeIngredient
                                        },
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
                        recipeIngredients = recipeIngredients + RecipeIngredientDetails(
                            ingredientId = -1, measureId = -1, quantity = "", recipeId = -1
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
                                        instructions = instructions.mapIndexed { i, instruction ->
                                            if (i == index) {
                                                instruction.copy(
                                                    description = newInstructionDescription
                                                )
                                            } else {
                                                instruction
                                            }
                                        }
                                    },
                                    label = { Text("Describe the step") },
                                )
                                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                                    IconButton(modifier = Modifier
                                        .width(48.dp)
                                        .height(48.dp),
                                        onClick = { instructions = instructions - instruction },
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
                        instructions = instructions +
                                Instruction(
                                    description = "",
                                    stepNumber = instructions.size + 1,
                                    recipeId = 0
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


