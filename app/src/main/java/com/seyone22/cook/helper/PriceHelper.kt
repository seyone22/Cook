package com.seyone22.cook.helper

import android.util.Log
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeIngredient

object PriceHelper {
    private fun priceOf(
        variant: IngredientVariant,
        quantity: Double
    ): Double { // Should pass quantity from scale (or default value), and price from ingredientVariety
        return (variant.price?.div(variant.quantity))?.times(quantity) ?: -1.0
    }

    private suspend fun getCheapestVariant(
        ingredientId: Long?,
        variants: List<IngredientVariant?>
    ): IngredientVariant? {
        // Find the cheapest variant for the given ingredientId
        var cheapestVariant: IngredientVariant? = null
        variants.let { variantList ->
            if (variantList.isNotEmpty()) {
                cheapestVariant =
                    variantList.filter { it?.ingredientId == ingredientId } // Filter variants by ingredientId
                        .minByOrNull {
                            it?.price ?: 0.0
                        } // Find the cheapest variant among filtered variants
            }
        }
        return cheapestVariant
    }

    suspend fun getCheapestPrice(
        ingredientId: Long?,
        variants: List<IngredientVariant?>,
        quantity: Double
    ): Double {
        val cheapestVariant = getCheapestVariant(ingredientId, variants)
        return if (cheapestVariant != null) {
            priceOf(cheapestVariant, quantity)
        } else {
            -1.0
        }
    }

    suspend fun getCostOfRecipe(
        recipeIngredients: List<RecipeIngredient?>,
        variantsList: List<IngredientVariant?>,
        scaleFactor: Double
    ): Double {
        var totalCost: Double = 0.0
        recipeIngredients.forEach { recipeIngredient ->
            if (recipeIngredient != null) {
                totalCost += getCheapestPrice(
                    recipeIngredient.ingredientId,
                    variantsList,
                    (recipeIngredient.quantity * scaleFactor)
                )
            }
        }
        return totalCost
    }
}