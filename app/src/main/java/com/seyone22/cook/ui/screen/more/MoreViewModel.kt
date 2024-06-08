package com.seyone22.cook.ui.screen.more

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.helper.DataHelper
import kotlinx.coroutines.launch

class MoreViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val instructionRepository: InstructionRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val ingredientRepository: IngredientRepository,
) : ViewModel() {
    fun importRecipe(context: Context, it: Uri) {
        val dataHelper = DataHelper()
        viewModelScope.launch {
            dataHelper.importRecipe(
                context,
                it,
                recipeRepository,
                instructionRepository,
                recipeIngredientRepository,
                recipeImageRepository,
                ingredientRepository
            )
        }
    }
}