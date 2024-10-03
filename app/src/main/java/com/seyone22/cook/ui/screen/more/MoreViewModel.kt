package com.seyone22.cook.ui.screen.more

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.data.repository.tag.TagRepository
import com.seyone22.cook.helper.RecipeFileHandler
import com.seyone22.cook.ui.common.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MoreViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val instructionRepository: InstructionRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val ingredientRepository: IngredientRepository,
    private val tagRepository: TagRepository,
) : ViewModel() {
    private val _moreViewState = MutableStateFlow(ViewState())
    val moreViewState: StateFlow<ViewState> get() = _moreViewState

    fun fetchTags() {
        viewModelScope.launch {
            val tags = tagRepository.getAllTags().first()
            _moreViewState.value = _moreViewState.value.copy(tags = tags)
        }
    }

    fun importRecipe(context: Context, it: Uri) {
        viewModelScope.launch {
            try {
                RecipeFileHandler.importRecipe(
                    context,
                    it,
                    recipeRepository,
                    instructionRepository,
                    recipeIngredientRepository,
                    recipeImageRepository,
                    ingredientRepository
                )
                Toast.makeText(context, "Successfully imported recipe!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                //Handle the exception here
                Toast.makeText(context, "Unable to import: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("ImportError", "Error importing recipe: ", e)
            }
        }
    }
}