package com.seyone22.cook.ui.screen.crud.recipe

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.helper.DataHelper.compressImageFile
import com.seyone22.cook.helper.ImageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.log

class RecipeOperationsViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val measureRepository: MeasureRepository,
    private val instructionRepository: InstructionRepository,
    private val ingredientRepository: IngredientRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository
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
            _addRecipeViewState.value = AddRecipeViewState(
                measures,
                ingredients,
                recipe,
                images,
                instructions,
                recipeIngredients
            )
        }
    }

    fun saveRecipe(
        recipe: Recipe,
        images: List<Uri>?,
        instructions: List<Instruction>,
        recipeIngredients: List<RecipeIngredient>,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                // Insert the recipe into the database
                recipeRepository.insertRecipe(recipe)

                // Update the recipeId for each instruction and insert them into the database
                val updatedInstructions = instructions.map { instruction ->
                    instruction.copy(recipeId = recipe.id)
                }
                updatedInstructions.forEach { instruction ->
                    instructionRepository.insertInstruction(instruction)
                }

                // Update the recipeId for each recipeIngredient and insert them into the database
                val updatedRecipeIngredients = recipeIngredients.map { recipeIngredient ->
                    recipeIngredient.copy(recipeId = recipe.id)
                }
                updatedRecipeIngredients.forEach { recipeIngredient ->
                    recipeIngredientRepository.insertRecipeIngredient(recipeIngredient)
                }

                // Save the images
                val imageHelper = ImageHelper(context)
                images?.forEach { image ->
                    Log.d("TAG", "saveRecipe: $image")


                    val imageBitmap = imageHelper.loadImageFromUri(image)!!
                    val compressedImageBytes = compressImageFile(imageBitmap, 60)

                    val imagePath = imageHelper.saveImageToInternalStorage(
                        BitmapFactory.decodeByteArray(compressedImageBytes, 0, compressedImageBytes.size),
                        "recipe_${recipe.id}_${System.currentTimeMillis()}.jpg"
                    )
                    val recipeImage =
                        RecipeImage(recipeId = recipe.id, imagePath = imagePath ?: "NULL")

                    recipeImageRepository.insertRecipeImage(recipeImage)
                }
            } catch (e: Exception) {
                // Handle any errors that might occur during the database operations
                e.printStackTrace()
            }
        }
    }

    suspend fun updateRecipe(
        recipe: Recipe,
        images: List<RecipeImage?>,
        instructions: List<Instruction?>,
        recipeIngredients: List<RecipeIngredient?>,
        context: Context
    ): Boolean {
            try {
                withContext(Dispatchers.IO) {
                    val imageHelper = ImageHelper(context)

                    // Update Recipe main details
                    recipeRepository.updateRecipe(recipe)

                    // Fetch current data for modification
                    val currentInstructions =
                        instructionRepository.getInstructionsForRecipe(recipe.id).first()
                    val currentRecipeIngredients =
                        recipeIngredientRepository.getRecipeIngredientsForRecipe(recipe.id).first()
                    val currentImages =
                        recipeImageRepository.getImagesForRecipe(recipe.id).first()

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
                }
                return true
            } catch (e: Exception) {
                e.printStackTrace()
                return false
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
    val recipeIngredients: List<RecipeIngredient?> = emptyList()
)