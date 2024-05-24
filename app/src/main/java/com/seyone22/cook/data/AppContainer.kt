package com.seyone22.cook.data

import android.content.Context
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredient.OfflineIngredientRepository
import com.seyone22.cook.data.repository.ingredientImage.IngredientImageRepository
import com.seyone22.cook.data.repository.ingredientImage.OfflineIngredientImageRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.ingredientVariant.OfflineIngredientVariantRepository

interface AppContainer {
    val ingredientRepository: IngredientRepository
    val ingredientVariantRepository: IngredientVariantRepository
    val ingredientImageRepository: IngredientImageRepository
}

/**
 * [AppContainer] implementation that provides instance of OfflineItemsRepository
 */
class AppDataContainer(private val context: Context) : AppContainer {
    override val ingredientRepository: IngredientRepository by lazy {
        OfflineIngredientRepository(CookDatabase.getDatabase(context).ingredientDao())
    }
    override val ingredientVariantRepository: IngredientVariantRepository by lazy {
        OfflineIngredientVariantRepository(CookDatabase.getDatabase(context).ingredientVariantDao())
    }
    override val ingredientImageRepository: IngredientImageRepository by lazy {
        OfflineIngredientImageRepository(CookDatabase.getDatabase(context).ingredientImageDao())
    }
}