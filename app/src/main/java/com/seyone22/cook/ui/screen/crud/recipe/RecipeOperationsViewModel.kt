package com.seyone22.cook.ui.screen.crud.recipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.InstructionSection
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.model.RecipeTag
import com.seyone22.cook.data.model.Tag
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.instructionsection.InstructionSectionRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.data.repository.recipeTag.RecipeTagRepository
import com.seyone22.cook.data.repository.tag.TagRepository
import com.seyone22.cook.helper.RecipeFileHandler.compressImageFile
import com.seyone22.cook.helper.ImageStorageHelper
import com.seyone22.cook.helper.loadBitmapFromUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class RecipeOperationsViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val measureRepository: MeasureRepository,
    private val instructionRepository: InstructionRepository,
    private val instructionSectionRepository: InstructionSectionRepository,
    private val ingredientRepository: IngredientRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val tagRepository: TagRepository,
    private val recipeTagRepository: RecipeTagRepository
) : ViewModel() {
    private val _addRecipeViewState = MutableStateFlow(AddRecipeViewState())
    val addRecipeViewState: StateFlow<AddRecipeViewState> get() = _addRecipeViewState

    fun fetchData(id: UUID = UUID.randomUUID()) {
        viewModelScope.launch {
            val measures = measureRepository.getAllMeasures().first()
            val ingredients = ingredientRepository.getAllIngredients().first()
            val recipe = recipeRepository.getRecipeById(id).first()
            val images = recipeImageRepository.getImagesForRecipe(id).first()
            val instructions = instructionRepository.getInstructionsForRecipe(id).first()
            val recipeIngredients =
                recipeIngredientRepository.getRecipeIngredientsForRecipe(id).first()
            val tags = tagRepository.getAllTags().first()
            val recipeTags = recipeTagRepository.getRecipeTagByRecipeId(id).first()
            _addRecipeViewState.value = AddRecipeViewState(
                measures,
                ingredients,
                recipe,
                images,
                instructions,
                recipeIngredients,
                tags,
                recipeTags
            )
        }
    }

    suspend fun saveRecipe(
        recipe: Recipe,
        images: List<Uri>?,
        instructions: List<Instruction>,
        recipeIngredients: List<RecipeIngredient>,
        recipeTags: List<Tag>,
        context: Context,
        instructionSections: SnapshotStateList<InstructionSection>
    ) {
        try {
            recipeRepository.insertRecipe(recipe)

            instructions.filterNotNull().forEach {
                instructionRepository.insertInstruction(it.copy(recipeId = recipe.id))
            }

            for (section in instructionSections.filterNotNull()) {
                instructionSectionRepository.insertSection(section.copy(recipeId = recipe.id))
            }

            recipeIngredients.filterNotNull().forEach {
                recipeIngredientRepository.insertRecipeIngredient(it.copy(recipeId = recipe.id))
            }

            // ✅ Handle tags properly
            for (tag in recipeTags) {
                val finalTag = if (tag.id == 0L || tag.id == null) {
                    // Try to find existing tag by name
                    val existingTag = tagRepository.getTagByName(tag.name).firstOrNull()
                    if (existingTag != null) {
                        existingTag
                    } else {
                        // Insert new tag
                        val newId = tagRepository.insertTag(tag)
                        tag.copy(id = newId)
                    }
                } else {
                    tag
                }

                recipeTagRepository.insertRecipeTag(
                    RecipeTag(recipeId = recipe.id, tagId = finalTag.id)
                )
            }

            val imageHelper = ImageStorageHelper(context)
            images?.forEach { image ->
                val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                    if (image.toString().startsWith("http")) loadBitmapFromUrl(image.toString())
                    else loadImage(context, image)
                }

                bitmap?.let {
                    val compressedBytes = compressImageFile(it, 60)
                    val savedPath = imageHelper.saveImageToInternalStorage(
                        BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size),
                        "recipe_${recipe.id}_${System.currentTimeMillis()}.jpg"
                    )
                    recipeImageRepository.insertRecipeImage(
                        RecipeImage(recipeId = recipe.id, imagePath = savedPath ?: "NULL")
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("TAG", "saveRecipe error: $e")
        }
    }


    suspend fun updateRecipe(
        recipe: Recipe,
        images: List<RecipeImage?>,
        instructions: List<Instruction?>,
        recipeIngredients: List<RecipeIngredient?>,
        recipeTags: List<Tag>,
        context: Context
    ): Boolean {
            try {
                withContext(Dispatchers.IO) {
                    val imageHelper = ImageStorageHelper(context)

                    // Update Recipe main details
                    recipeRepository.updateRecipe(recipe)

                    // Fetch current data for modification
                    val currentInstructions =
                        instructionRepository.getInstructionsForRecipe(recipe.id).first()
                    val currentRecipeIngredients =
                        recipeIngredientRepository.getRecipeIngredientsForRecipe(recipe.id).first()
                    val currentImages =
                        recipeImageRepository.getImagesForRecipe(recipe.id).first()
                    val currentTags =
                        recipeTagRepository.getRecipeTagByRecipeId(recipe.id).first()

                    // Instructions operations (Add/Update/Delete)
                    val instructionsToAdd =
                        instructions.filter { it != null && currentInstructions.find { i -> i!!.id == it.id } == null }
                    val instructionsToUpdate =
                        instructions.filter { it != null && currentInstructions.find { i -> i!!.id == it.id } != null }
                    val instructionsToDelete =
                        currentInstructions.filter { it != null && instructions.find { i -> i!!.id == it.id } == null }

                    instructionsToAdd.forEach { instruction ->
                        instructionRepository.insertInstruction(instruction!!)
                    }
                    instructionsToUpdate.forEach { instruction ->
                        instructionRepository.updateInstruction(instruction!!)
                    }
                    instructionsToDelete.forEach { instruction ->
                        instructionRepository.deleteInstruction(instruction!!)
                    }

                    // Recipe Ingredients operations (Add/Update/Delete)
                    val recipeIngredientsToAdd =
                        recipeIngredients.filter { it != null && currentRecipeIngredients.find { i -> i!!.id == it.id } == null }
                    val recipeIngredientsToUpdate =
                        recipeIngredients.filter { it != null && currentRecipeIngredients.find { i -> i!!.id == it.id } != null }
                    val recipeIngredientsToDelete =
                        currentRecipeIngredients.filter { it != null && recipeIngredients.find { i -> i!!.id == it.id } == null }

                    recipeIngredientsToAdd.forEach { recipeIngredient ->
                        recipeIngredientRepository.insertRecipeIngredient(recipeIngredient!!)
                    }
                    recipeIngredientsToUpdate.forEach { recipeIngredient ->
                        recipeIngredientRepository.updateRecipeIngredient(recipeIngredient!!)
                    }
                    recipeIngredientsToDelete.forEach { recipeIngredient ->
                        recipeIngredientRepository.deleteRecipeIngredient(recipeIngredient!!)
                    }

                    // Images operations (Add/Update/Delete)
                    val imagesToAdd =
                        images.filter { it != null && currentImages.find { i -> i!!.id == it.id } == null }
                    val imagesToDelete =
                        currentImages.filter { it != null && images.find { i -> i!!.id == it.id } == null }

                    imagesToDelete.forEach { image ->
                        recipeImageRepository.deleteRecipeImage(image!!)
                        imageHelper.deleteImageFromInternalStorage(Uri.parse(image.imagePath))
                    }

                    imagesToAdd.forEach { image ->
                        Log.d("TAG", "$image")

                        // Ensure image path is not null
                        image?.let {
                            val imageBitmap = imageHelper.loadImageFromUri(Uri.parse(it.imagePath))
                                ?: return@forEach
                            val imagePath = imageHelper.saveImageToInternalStorage(
                                imageBitmap,
                                "recipe_${recipe.id}_${System.currentTimeMillis()}.jpg"
                            )

                            imagePath?.let { path ->
                                val recipeImage =
                                    RecipeImage(recipeId = recipe.id, imagePath = path)
                                Log.d("TAG", "updateRecipe: $recipeImage")

                                recipeImageRepository.insertRecipeImage(recipeImage)
                            } ?: run {
                                Log.e("TAG", "Failed to save image to internal storage.")
                            }
                        }
                    }

                    // Tag operations (Add/Delete)
                    val tagsToAdd =
                        recipeTags.map { RecipeTag(recipeId = recipe.id, tagId = it.id) }.filter { currentTags.find { i -> i!!.recipeId == it.recipeId && i.tagId == it.tagId } == null }
                    val tagsToDelete =
                        currentTags.filter { it != null && recipeTags.find { i -> i.id == it.tagId && recipe.id == it.recipeId } == null }

                    tagsToAdd.forEach { tag ->
                        recipeTagRepository.insertRecipeTag(tag)
                    }
                    tagsToDelete.forEach { tag ->
                        recipeTagRepository.deleteRecipeTag(tag!!)
                    }
                }
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
    }

    suspend fun loadImage(context: Context, imageUri: Uri): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                when (imageUri.scheme) {
                    "content", "file" -> {
                        // Local file or content URI
                        context.contentResolver.openInputStream(imageUri)?.use {
                            BitmapFactory.decodeStream(it)
                        }
                    }
                    "http", "https" -> {
                        // Web URL
                        val connection = URL(imageUri.toString()).openConnection() as HttpURLConnection
                        connection.doInput = true
                        connection.connect()
                        connection.inputStream.use { BitmapFactory.decodeStream(it) }
                    }
                    else -> null
                }
            } catch (e: Exception) {
                Log.e("ImageHelper", "Failed to load image: $imageUri", e)
                null
            }
        }
    }



}

// Define a data class to hold the list of measures
data class AddRecipeViewState(
    val measures: List<Measure?> = emptyList(),
    val ingredients: List<Ingredient?> = emptyList(),
    val recipe: Recipe? = null,
    val images: List<RecipeImage?> = emptyList(),
    val instructions: List<Instruction?> = emptyList(),
    val recipeIngredients: List<RecipeIngredient?> = emptyList(),
    val tags: List<Tag?> = emptyList(),
    val recipeTags: List<RecipeTag?> = emptyList()
)