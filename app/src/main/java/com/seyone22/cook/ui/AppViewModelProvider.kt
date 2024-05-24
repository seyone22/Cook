package com.seyone22.cook.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.seyone22.cook.CookApplication
import com.seyone22.cook.ui.screen.crud.AddIngredientViewModel
import com.seyone22.cook.ui.screen.home.HomeViewModel
import com.seyone22.cook.ui.screen.ingredients.IngredientsViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
            )
        }
        initializer {
            IngredientsViewModel(
                ingredientRepository = cookApplication().container.ingredientRepository,
                ingredientVariantRepository = cookApplication().container.ingredientVariantRepository,
                ingredientImageRepository = cookApplication().container.ingredientImageRepository
            )
        }
        initializer {
            AddIngredientViewModel(
                ingredientRepository = cookApplication().container.ingredientRepository,
                ingredientVariantRepository = cookApplication().container.ingredientVariantRepository,
                ingredientImageRepository = cookApplication().container.ingredientImageRepository
            )
        }
/*        initializer {
            IngredientDetailViewModel(
                ingredientRepository = cookApplication().container.ingredientRepository,
                ingredientVariantRepository = cookApplication().container.ingredientVariantRepository,
                ingredientImageRepository = cookApplication().container.ingredientImageRepository
            )
        }*/
    }
}

fun CreationExtras.cookApplication(): CookApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as CookApplication)