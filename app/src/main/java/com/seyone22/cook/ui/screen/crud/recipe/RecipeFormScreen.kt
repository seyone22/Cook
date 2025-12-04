package com.seyone22.cook.ui.screen.crud.recipe

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.seyone22.cook.R
import com.seyone22.cook.SharedViewModel
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.InstructionSection
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredientDetails
import com.seyone22.cook.data.model.Tag
import com.seyone22.cook.data.model.TagType
import com.seyone22.cook.data.model.toRecipeIngredient
import com.seyone22.cook.data.model.toRecipeIngredientDetails
import com.seyone22.cook.ui.AppViewModelProvider
import com.seyone22.cook.ui.navigation.NavigationDestination
import com.seyone22.cook.ui.screen.crud.recipe.composables.RecipeFormFieldsSection
import com.seyone22.cook.ui.screen.crud.recipe.composables.RecipeFormImageCarousel
import com.seyone22.cook.ui.screen.crud.recipe.composables.RecipeFormIngredientSection
import com.seyone22.cook.ui.screen.crud.recipe.composables.RecipeFormInstructionSection
import kotlinx.coroutines.launch
import recipeimporter.model.HowToSection
import recipeimporter.model.HowToStep
import java.util.UUID

// ------------------------- Navigation Destinations -------------------------
object AddRecipeDestination : NavigationDestination {
    override val route = "add_recipe"
    override val titleRes = R.string.add_recipe
    override val routeId = 10
}

object EditRecipeDestination : NavigationDestination {
    override val route = "edit_recipe"
    const val recipeIdArg = "recipeId"
    val routeWithArgs = "$route/{$recipeIdArg}"
    override val titleRes = R.string.edit_recipe
    override val routeId = 11
}

object ImportRecipeDestination : NavigationDestination {
    override val route = "import_recipe"
    override val titleRes = R.string.import_recipe
    override val routeId = 12
}

enum class RecipeFormMode { ADD, IMPORT, EDIT }

// ------------------------- State Holder -------------------------
data class RecipeFormState(
    var name: String = "",
    var description: String = "",
    var cookTime: String = "",
    var prepTime: String = "",
    var servingSize: String = "",
    var reference: String = "",
    var photos: List<Uri> = emptyList(),
    var instructions: SnapshotStateList<Instruction> = mutableStateListOf(),
    var instructionSections: SnapshotStateList<InstructionSection> = mutableStateListOf(),
    var recipeIngredients: SnapshotStateList<RecipeIngredientDetails> = mutableStateListOf(),
    var recipeTags: SnapshotStateList<Tag> = mutableStateListOf()
) {
    fun toRecipe(id: UUID? = null, author: String = "Anonymous", videoUrl: String? = null): Recipe =
        Recipe(
            id = id ?: UUID.randomUUID(),
            name = name,
            description = description,
            cookTime = cookTime.toIntOrNull() ?: 0,
            prepTime = prepTime.toIntOrNull() ?: 0,
            servingSize = servingSize.toIntOrNull() ?: 1,
            reference = reference,
            author = author,
            videoUrl = videoUrl
        )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeFormScreen(
    mode: RecipeFormMode,
    sharedViewModel: SharedViewModel,
    navController: NavController,
    recipeId: UUID? = null,
    viewModel: RecipeOperationsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Shared ViewState
    val data by viewModel.addRecipeViewState.collectAsState()
    val prefillRecipe by sharedViewModel.importedRecipe.collectAsState()
    val ingredients by sharedViewModel.ingredients.collectAsState()
    val stringData by sharedViewModel.valueString.collectAsState()

    // Local state
    val formState = remember { mutableStateOf(RecipeFormState()) }

    // --- SEARCH STATE ---
    var showSearchSheet by remember { mutableStateOf(false) }
    var activeIngredientIndex by remember { mutableIntStateOf(-1) }
    var searchInitialQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()

    // ----------------- Initialize State -----------------
    LaunchedEffect(mode, recipeId, prefillRecipe, stringData, data.recipe, ingredients) {
        when (mode) {
            RecipeFormMode.ADD -> {
                // nothing, defaults
            }

            RecipeFormMode.IMPORT -> {
                prefillRecipe?.let { recipe ->
                    val currentIngredients = ingredients

                    // Map to your UI model
                    val parsedIngredients = currentIngredients.map { it.toRecipeIngredientDetails() }

                    Log.d("RecipeFormScreen", "Populating Form with ${parsedIngredients.size} ingredients")

                    formState.value = RecipeFormState(
                        name = recipe.title,
                        description = recipe.description ?: "",
                        cookTime = recipe.cookTimeMinutes?.toString() ?: "",
                        prepTime = recipe.prepTimeMinutes?.toString() ?: "",
                        servingSize = recipe.yield ?: "",
                        reference = recipe.reference ?: "",
                        photos = recipe.imageUrls.map { it.toUri() },
                        instructions = mutableStateListOf(),
                        instructionSections = mutableStateListOf(),
                        recipeIngredients = parsedIngredients.toMutableStateList(),
                        recipeTags = mutableStateListOf<Tag>().apply {
                            recipe.cuisine.forEach {
                                add(Tag(name = it, category = TagType.CUISINE))
                            }
                            recipe.categories.forEach {
                                add(Tag(name = it, category = TagType.CATEGORY))
                            }
                        }
                    )

                    // Instructions
                    var sectionCounter = 0
                    recipe.recipeInstructions.forEach { instruction ->
                        when (instruction) {
                            is HowToSection -> {
                                sectionCounter++
                                val section = InstructionSection(
                                    recipeId = UUID.randomUUID(),
                                    name = instruction.name ?: "Section $sectionCounter",
                                    sectionNumber = sectionCounter
                                )
                                formState.value.instructionSections.add(section)
                                instruction.steps.forEachIndexed { idx, step ->
                                    formState.value.instructions.add(
                                        Instruction(
                                            id = 0L,
                                            description = step.text,
                                            stepNumber = idx,
                                            sectionId = sectionCounter,
                                            recipeId = UUID.randomUUID()
                                        )
                                    )
                                }
                            }
                            is HowToStep -> {
                                sectionCounter++
                                val section = InstructionSection(
                                    recipeId = UUID.randomUUID(),
                                    name = "Step $sectionCounter",
                                    sectionNumber = sectionCounter
                                )
                                formState.value.instructionSections.add(section)
                                formState.value.instructions.add(
                                    Instruction(
                                        id = 0L,
                                        description = instruction.text,
                                        stepNumber = 0,
                                        sectionId = sectionCounter,
                                        recipeId = UUID.randomUUID()
                                    )
                                )
                            }
                        }
                    }
                }

                stringData?.let { str ->
                    formState.value = formState.value.copy(description = str)
                }
            }

            RecipeFormMode.EDIT -> {
                viewModel.fetchData(recipeId!!)
            }
        }
    }

    // Load DB data into state for EDIT
    LaunchedEffect(data.recipe) {
        if (mode == RecipeFormMode.EDIT && data.recipe != null) {
            formState.value = RecipeFormState(
                name = data.recipe?.name ?: "",
                description = data.recipe?.description ?: "",
                cookTime = data.recipe?.cookTime.toString(),
                prepTime = data.recipe?.prepTime.toString(),
                servingSize = data.recipe?.servingSize.toString(),
                reference = data.recipe?.reference ?: "",
                photos = data.images.mapNotNull { it?.imagePath?.toUri() },
                instructions = mutableStateListOf<Instruction>().apply {
                    data.instructions.mapNotNull { it }.forEach { add(it) }
                },
                recipeIngredients = mutableStateListOf<RecipeIngredientDetails>().apply {
                    data.recipeIngredients.mapNotNull { it?.toRecipeIngredientDetails() }
                        .forEach { add(it) }
                },
                recipeTags = mutableStateListOf<Tag>().apply {
                    data.recipeTags.mapNotNull { rel ->
                        data.tags.find { tag -> tag?.id == rel?.tagId }
                    }.forEach { add(it) }
                })
        }
    }

    // ----------------- Image Picker -----------------
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        formState.value = formState.value.copy(photos = formState.value.photos + uris)
    }

    // ----------------- Scaffold -----------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (mode == RecipeFormMode.EDIT) "Edit Recipe" else "Add Recipe") },
                navigationIcon = {
                    Icon(
                        modifier = Modifier
                            .padding(16.dp, 0.dp, 24.dp, 0.dp)
                            .clickable {
                                if (mode == RecipeFormMode.IMPORT) sharedViewModel.clearImportedRecipe()
                                navController.popBackStack()
                            }, imageVector = Icons.Default.Close, contentDescription = null
                    )
                },
                actions = {
                    Button(
                        modifier = Modifier.padding(24.dp, 0.dp, 16.dp, 0.dp), onClick = {
                            coroutineScope.launch {
                                if (mode == RecipeFormMode.EDIT) {
                                    viewModel.updateRecipe(
                                        formState.value.toRecipe(recipeId),
                                        formState.value.photos.map { uri ->
                                            RecipeImage(
                                                imagePath = uri.toString(), recipeId = recipeId!!
                                            )
                                        },
                                        formState.value.instructions.map { it.copy(recipeId = recipeId!!) },
                                        formState.value.recipeIngredients.map {
                                            it.copy(recipeId = recipeId!!).toRecipeIngredient()
                                        },
                                        formState.value.recipeTags,
                                        context
                                    )
                                } else {
                                    viewModel.saveRecipe(
                                        formState.value.toRecipe(
                                            author = prefillRecipe?.author ?: "Anonymous",
                                            videoUrl = prefillRecipe?.videoUrl
                                        ),
                                        formState.value.photos,
                                        formState.value.instructions,
                                        formState.value.recipeIngredients.map { it.toRecipeIngredient() },
                                        formState.value.recipeTags,
                                        context,
                                        formState.value.instructionSections
                                    )
                                }
                                navController.popBackStack()
                            }
                        }) { Text("Save") }
                })
        }) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                // --- UI (shared for all modes) ---
                Column(modifier = Modifier.padding(12.dp, 0.dp)) {
                    RecipeFormImageCarousel(
                        imageUris = formState.value.photos,
                        onAddImage = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onRemoveImage = { uri ->
                            formState.value = formState.value.copy(
                                photos = formState.value.photos.toMutableList()
                                    .also { it.remove(uri) })
                        }, modifier = Modifier
                            .width(346.dp)
                            .padding(start = 36.dp, bottom = 16.dp)
                    )

                    RecipeFormFieldsSection(
                        formState = formState.value,
                        onFormStateChange = { formState.value = it },
                        allTags = data.tags.filterNotNull(),
                        navController = navController
                    )

                    RecipeFormIngredientSection(
                        recipeIngredients = formState.value.recipeIngredients,
                        allMeasures = data.measures, // Passed real measures
                        onAddRecipeIngredient = { newIngredient ->
                            formState.value.recipeIngredients.add(newIngredient)
                        },
                        onUpdateRecipeIngredient = { index, updatedIngredient ->
                            formState.value.recipeIngredients[index] = updatedIngredient
                        },
                        onRemoveRecipeIngredient = { ingredient ->
                            formState.value.recipeIngredients.remove(ingredient)
                        },
                        onTriggerIngredientSearch = { index, currentName ->
                            activeIngredientIndex = index
                            searchInitialQuery = currentName
                            showSearchSheet = true
                        },
                        modifier = Modifier
                    )

                    RecipeFormInstructionSection(
                        instructions = formState.value.instructions,
                        instructionSections = formState.value.instructionSections,
                        onRemoveInstruction = { instr ->
                            formState.value.instructions.remove(instr)
                        },
                        onAddInstruction = {
                            val lastSectionId =
                                formState.value.instructionSections.lastOrNull()?.sectionNumber ?: 1
                            formState.value.instructions.add(
                                Instruction(
                                    id = 0L,
                                    description = "",
                                    stepNumber = formState.value.instructions.count { it.sectionId == lastSectionId },
                                    sectionId = lastSectionId,
                                    recipeId = UUID.randomUUID()
                                )
                            )
                        },
                        onUpdateInstruction = { index, updatedInstr ->
                            formState.value.instructions[index] = updatedInstr
                        })
                }
            }
        }

        // Search Bottom Sheet
        if (showSearchSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSearchSheet = false },
                sheetState = sheetState
            ) {
                // Placeholder for future "IngredientSearchBottomSheet"
                Text(
                    text = "Search Database for '${searchInitialQuery}'",
                    modifier = Modifier.padding(16.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                )

                // Example of how you will update the selected row when a result is clicked:
                /*
                Button(onClick = {
                    if (activeIngredientIndex != -1) {
                        val currentItem = formState.value.recipeIngredients[activeIngredientIndex]
                        formState.value.recipeIngredients[activeIngredientIndex] = currentItem.copy(
                            // Update fields based on search result
                            foodDbId = "new_db_id",
                            // Keep original name or overwrite based on user choice
                        )
                    }
                    showSearchSheet = false
                }) { Text("Select Match") }
                */
            }
        }
    }
}