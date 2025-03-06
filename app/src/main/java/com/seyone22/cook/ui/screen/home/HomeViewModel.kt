package com.seyone22.cook.ui.screen.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.model.ShoppingListItem
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.data.repository.recipeTag.RecipeTagRepository
import com.seyone22.cook.data.repository.shoppingList.ShoppingListRepository
import com.seyone22.cook.data.repository.tag.TagRepository
import com.seyone22.cook.ui.common.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.log

class HomeViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val instructionRepository: InstructionRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val ingredientVariantRepository: IngredientVariantRepository,
    private val measureRepository: MeasureRepository,
    private val ingredientRepository: IngredientRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val tagRepository: TagRepository,
    private val recipeTagRepository: RecipeTagRepository
) : ViewModel() {
    private val _homeViewState = MutableStateFlow(ViewState())
    val homeViewState: StateFlow<ViewState> get() = _homeViewState

    fun fetchData() {
        viewModelScope.launch {
            val recipes = recipeRepository.getAllRecipes().first()
            val images = recipeImageRepository.getAllRecipeImages().first()
            val instructions = instructionRepository.getAllInstructions().first()
            val recipeIngredients = recipeIngredientRepository.getAllRecipeIngredients().first()
            val measures = measureRepository.getAllMeasures().first()
            val ingredients = ingredientRepository.getAllIngredients().first()
            val variants = ingredientVariantRepository.getAllIngredientVariants().first()
            val shoppingLists = shoppingListRepository.getAllShoppingLists().first()
            val tags = tagRepository.getAllTags().first()
            val recipeTags = recipeTagRepository.getAllRecipeTags().first()

            _homeViewState.value = ViewState(
                recipes = recipes,
                images = images,
                instructions = instructions,
                recipeIngredients = recipeIngredients,
                measures = measures,
                ingredients = ingredients,
                variants = variants,
                shoppingLists = shoppingLists,
                tags = tags,
                recipeTags = recipeTags
            )
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipe)
        }
    }

    fun incrementMakeCounter(recipeId: UUID) {
        viewModelScope.launch {
            recipeRepository.incrementTimesMade(recipeId)
        }
    }

    fun addAllToShoppingList(ingredients: List<RecipeIngredient?>, it: Long) {
        viewModelScope.launch {
            ingredients.forEach { ingredient ->
                shoppingListRepository.insertItem(
                    ShoppingListItem(
                        ingredientId = (ingredient?.ingredientId ?: UUID.randomUUID()),
                        quantity = ingredient?.quantity ?: 0.0,
                        measureId = (ingredient?.measureId?.toInt() ?: 0).toLong(),
                        shoppingListId = it
                    )
                )
            }
        }
    }
}