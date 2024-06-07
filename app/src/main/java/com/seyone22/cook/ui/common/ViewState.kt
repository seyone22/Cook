package com.seyone22.cook.ui.common

import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredient

data class ViewState(
    val recipes: List<Recipe?> = emptyList(),
    val images: List<RecipeImage?> = emptyList(),
    val instructions: List<Instruction?> = emptyList(),
    val recipeIngredients: List<RecipeIngredient?> = emptyList(),
    val measures: List<Measure?> = emptyList(),
    val ingredients: List<Ingredient?> = emptyList(),
    val variants: List<IngredientVariant?> = emptyList()
)