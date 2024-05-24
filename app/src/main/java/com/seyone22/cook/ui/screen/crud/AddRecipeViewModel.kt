package com.seyone22.cook.ui.screen.crud

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.helper.ImageHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddRecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val measureRepository: MeasureRepository,
    private val instructionRepository: InstructionRepository,
    private val ingredientRepository: IngredientRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository
) : ViewModel() {
    private val _addRecipeViewState = MutableStateFlow(AddRecipeViewState())
    val addRecipeViewState: StateFlow<AddRecipeViewState> get() = _addRecipeViewState

    fun fetchData() {
        viewModelScope.launch {
            val measures = measureRepository.getAllMeasures().first()
            val ingredients = ingredientRepository.getAllIngredients().first()
            _addRecipeViewState.value = AddRecipeViewState(measures, ingredients)
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
                val recipeId = recipeRepository.insertRecipe(recipe)

                // Update the recipeId for each instruction and insert them into the database
                val updatedInstructions = instructions.map { instruction ->
                    instruction.copy(recipeId = recipeId)
                }
                updatedInstructions.forEach { instruction ->
                    instructionRepository.insertInstruction(instruction)
                }

                // Update the recipeId for each recipeIngredient and insert them into the database
                val updatedRecipeIngredients = recipeIngredients.map { recipeIngredient ->
                    recipeIngredient.copy(recipeId = recipeId)
                }
                updatedRecipeIngredients.forEach { recipeIngredient ->
                    recipeIngredientRepository.insertRecipeIngredient(recipeIngredient)
                }

                // Save the images
                val imageHelper = ImageHelper(context)
                images?.forEach { image ->
                    val imageBitmap = imageHelper.loadImageFromUri(image)!!
                    val imagePath = imageHelper.saveImageToInternalStorage(
                        imageBitmap,
                        "'recipe_${recipeId}_${System.currentTimeMillis()}.jpg"
                    )
                    val recipeImage =
                        RecipeImage(recipeId = recipeId, imagePath = imagePath ?: "NULL")
                    recipeImageRepository.insertRecipeImage(recipeImage)
                }
            } catch (e: Exception) {
                // Handle any errors that might occur during the database operations
                e.printStackTrace()
            }
        }
    }
}

// Define a data class to hold the list of measures
data class AddRecipeViewState(
    val measures: List<Measure?> = emptyList(),
    val ingredients: List<Ingredient?> = emptyList()
)
