package com.seyone22.cook.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.seyone22.cook.CookApplication
import com.seyone22.cook.ui.screen.cooking.CookingViewModel
import com.seyone22.cook.ui.screen.crud.ingredient.IngredientOperationsViewModel
import com.seyone22.cook.ui.screen.crud.recipe.RecipeOperationsViewModel
import com.seyone22.cook.ui.screen.home.HomeViewModel
import com.seyone22.cook.ui.screen.ingredients.IngredientsViewModel
import com.seyone22.cook.ui.screen.more.MoreViewModel
import com.seyone22.cook.ui.screen.search.SearchViewModel
import com.seyone22.cook.ui.screen.shoppingList.ShoppingListViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                recipeRepository = cookApplication().container.recipeRepository,
                recipeImageRepository = cookApplication().container.recipeImageRepository,
                instructionRepository = cookApplication().container.instructionRepository,
                recipeIngredientRepository = cookApplication().container.recipeIngredientRepository,
                measureRepository = cookApplication().container.measureRepository,
                ingredientRepository = cookApplication().container.ingredientRepository,
                ingredientVariantRepository = cookApplication().container.ingredientVariantRepository,
                )
        }
        initializer {
            IngredientsViewModel(
                ingredientRepository = cookApplication().container.ingredientRepository,
                ingredientVariantRepository = cookApplication().container.ingredientVariantRepository,
                ingredientImageRepository = cookApplication().container.ingredientImageRepository,
                recipeIngredientRepository = cookApplication().container.recipeIngredientRepository,
                measureRepository = cookApplication().container.measureRepository
            )
        }
        initializer {
            IngredientOperationsViewModel(
                ingredientRepository = cookApplication().container.ingredientRepository,
                ingredientVariantRepository = cookApplication().container.ingredientVariantRepository,
                ingredientImageRepository = cookApplication().container.ingredientImageRepository,
                measureRepository = cookApplication().container.measureRepository
            )
        }
        initializer {
            RecipeOperationsViewModel(
                recipeRepository = cookApplication().container.recipeRepository,
                recipeImageRepository = cookApplication().container.recipeImageRepository,
                measureRepository = cookApplication().container.measureRepository,
                instructionRepository = cookApplication().container.instructionRepository,
                ingredientRepository = cookApplication().container.ingredientRepository,
                recipeIngredientRepository = cookApplication().container.recipeIngredientRepository
            )
        }
        initializer {
            CookingViewModel(
                recipeRepository = cookApplication().container.recipeRepository,
                recipeImageRepository = cookApplication().container.recipeImageRepository,
                instructionRepository = cookApplication().container.instructionRepository,
                recipeIngredientRepository = cookApplication().container.recipeIngredientRepository,
                measureRepository = cookApplication().container.measureRepository,
                ingredientRepository = cookApplication().container.ingredientRepository,
                ingredientVariantRepository = cookApplication().container.ingredientVariantRepository,
            )
        }
        initializer {
            MoreViewModel(

            )
        }
        initializer {
            SearchViewModel(
                recipeRepository = cookApplication().container.recipeRepository,
                recipeImageRepository = cookApplication().container.recipeImageRepository,
            )
        }
        initializer {
            ShoppingListViewModel(
                recipeRepository = cookApplication().container.recipeRepository,
                recipeImageRepository = cookApplication().container.recipeImageRepository,
                instructionRepository = cookApplication().container.instructionRepository,
                recipeIngredientRepository = cookApplication().container.recipeIngredientRepository,
                measureRepository = cookApplication().container.measureRepository,
                ingredientRepository = cookApplication().container.ingredientRepository,
                ingredientVariantRepository = cookApplication().container.ingredientVariantRepository,
            )
        }
    }
}

fun CreationExtras.cookApplication(): CookApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as CookApplication)