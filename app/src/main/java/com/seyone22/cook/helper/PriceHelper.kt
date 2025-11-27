package com.seyone22.cook.helper

import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.service.getIngredientPrices
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.seyone22.cook.provider.KtorClientProvider.client

object PriceHelper {

    private fun priceOfUnit(price: Double, quantity: Double, desiredQuantity: Double): Double {
        return (price / quantity) * desiredQuantity
    }

    suspend fun getCheapestPriceFromServer(
        ingredientId: String,
        quantity: Double,
        client: HttpClient
    ): Double = withContext(Dispatchers.IO) {
        val prices = getIngredientPrices(ingredientId, client)
        val cheapest = prices.minByOrNull { it.price / it.quantity }
        cheapest?.let {
            priceOfUnit(it.price, it.quantity, quantity)
        } ?: -1.0
    }

    suspend fun getCostOfRecipe(
        recipeIngredients: List<RecipeIngredient?>,
        scaleFactor: Double,
        client: HttpClient
    ): Double {
        var total = 0.0
        for (ingredient in recipeIngredients) {
            if (ingredient == null) continue

            val id = ingredient.foodDbId
            val qty = ingredient.quantity * scaleFactor
            total += getCheapestPriceFromServer(id, qty, client)
        }
        return total
    }
}
