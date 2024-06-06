package com.seyone22.cook.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val instructionRepository: InstructionRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val ingredientVariantRepository: IngredientVariantRepository,
    private val measureRepository: MeasureRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {
    private val _homeViewState = MutableStateFlow(HomeViewState())
    val homeViewState: StateFlow<HomeViewState> get() = _homeViewState

    fun fetchData() {
        viewModelScope.launch {
            val recipes = recipeRepository.getAllRecipes().first()
            val images = recipeImageRepository.getAllRecipeImages().first()
            val instructions = instructionRepository.getAllInstructions().first()
            val recipeIngredients = recipeIngredientRepository.getAllRecipeIngredients().first()
            val measures = measureRepository.getAllMeasures().first()
            val ingredients = ingredientRepository.getAllIngredients().first()
            val variants = ingredientVariantRepository.getAllIngredientVariants().first()

            _homeViewState.value = HomeViewState(recipes, images, instructions, recipeIngredients, measures, ingredients, variants)
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipe)
            recipeImageRepository.deleteImagesForRecipe(recipe.id.toInt())
            recipeIngredientRepository.deleteIngredientsForRecipe(recipe.id.toInt())
        }
    }

    fun incrementMakeCounter(recipeId: Long) {
        viewModelScope.launch {
            recipeRepository.incrementTimesMade(recipeId)
        }
    }

}

data class HomeViewState(
    val recipes: List<Recipe?> = emptyList(),
    val images: List<RecipeImage?> = emptyList(),
    val instructions: List<Instruction?> = emptyList(),
    val recipeIngredients: List<RecipeIngredient?> = emptyList(),
    val measures: List<Measure?> = emptyList(),
    val ingredients: List<Ingredient?> = emptyList(),
    val variants: List<IngredientVariant?> = emptyList()
)