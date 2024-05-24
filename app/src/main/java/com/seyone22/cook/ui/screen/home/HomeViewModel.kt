package com.seyone22.cook.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val instructionRepository: InstructionRepository
): ViewModel() {
    private val _homeViewState = MutableStateFlow(HomeViewState())
    val homeViewState: StateFlow<HomeViewState> get() = _homeViewState

    fun fetchData() {
        viewModelScope.launch {
            val recipes = recipeRepository.getAllRecipes().first()
            val images = recipeImageRepository.getAllRecipeImages().first()
            val instructions = instructionRepository.getAllInstructions().first()
            _homeViewState.value = HomeViewState(recipes, images, instructions)
        }
    }

}

data class HomeViewState(
    val recipes: List<Recipe?> = emptyList(),
    val images: List<RecipeImage?> = emptyList(),
    val instructions: List<Instruction?> = emptyList()
)