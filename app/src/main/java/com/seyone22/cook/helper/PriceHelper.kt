package com.seyone22.cook.helper

import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.RecipeIngredient
import java.util.UUID

object PriceHelper {
    private fun priceOf(
        variant: IngredientVariant, quantity: Double
    ): Double { // Should pass quantity from scale (or default value), and price from ingredientVariety
        return (variant.price?.div(variant.quantity))?.times(quantity) ?: -1.0
    }

    private suspend fun getCheapestVariant(
        ingredientId: UUID?, variants: List<IngredientVariant?>
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
        ingredientId: UUID?, variants: List<IngredientVariant?>, quantity: Double
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

    suspend fun getCostOfRecipeUnavailable(
        recipeIngredients: List<RecipeIngredient?>,
        ingredientList: List<Ingredient?>,
        variantsList: List<IngredientVariant?>,
        scaleFactor: Double
    ): Double {
        var totalCost: Double = 0.0
        recipeIngredients.forEach { recipeIngredient ->
            if ((recipeIngredient != null) && !ingredientList.find {
                    (it?.id ?: -1) == recipeIngredient.ingredientId
                }?.stocked!!) {
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