package com.seyone22.cook.data

import android.content.Context
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredient.OfflineIngredientRepository
import com.seyone22.cook.data.repository.ingredientImage.IngredientImageRepository
import com.seyone22.cook.data.repository.ingredientImage.OfflineIngredientImageRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.ingredientVariant.OfflineIngredientVariantRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.instruction.OfflineInstructionRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.data.repository.measure.OfflineMeasureRepository
import com.seyone22.cook.data.repository.measureConversion.MeasureConversionRepository
import com.seyone22.cook.data.repository.measureConversion.OfflineMeasureConversionRepository
import com.seyone22.cook.data.repository.recipe.OfflineRecipeRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.OfflineRecipeImageRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.OfflineRecipeIngredientRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.data.repository.shoppingList.OfflineShoppingListRepository
import com.seyone22.cook.data.repository.shoppingList.ShoppingListRepository
import kotlinx.coroutines.CoroutineScope

interface AppContainer {
    val ingredientRepository: IngredientRepository
    val ingredientVariantRepository: IngredientVariantRepository
    val ingredientImageRepository: IngredientImageRepository
    val recipeImageRepository: RecipeImageRepository
    val measureRepository: MeasureRepository
    val measureConversionRepository: MeasureConversionRepository
    val recipeRepository: RecipeRepository
    val instructionRepository: InstructionRepository
    val recipeIngredientRepository: RecipeIngredientRepository
    val shoppingListRepository: ShoppingListRepository
}

/**
 * [AppContainer] implementation that provides instance of OfflineItemsRepository
 */
class AppDataContainer(private val context: Context, private val scope: CoroutineScope) : AppContainer {
    override val ingredientRepository: IngredientRepository by lazy {
        OfflineIngredientRepository(CookDatabase.getDatabase(context, scope).ingredientDao())
    }
    override val ingredientVariantRepository: IngredientVariantRepository by lazy {
        OfflineIngredientVariantRepository(CookDatabase.getDatabase(context, scope).ingredientVariantDao())
    }
    override val ingredientImageRepository: IngredientImageRepository by lazy {
        OfflineIngredientImageRepository(CookDatabase.getDatabase(context, scope).ingredientImageDao())
    }
    override val recipeImageRepository: RecipeImageRepository by lazy {
        OfflineRecipeImageRepository(CookDatabase.getDatabase(context, scope).recipeImageDao())
    }
    override val measureRepository: MeasureRepository by lazy {
        OfflineMeasureRepository(CookDatabase.getDatabase(context, scope).measureDao())
    }
    override val measureConversionRepository: MeasureConversionRepository by lazy {
        OfflineMeasureConversionRepository(CookDatabase.getDatabase(context, scope).measureConversionDao())
    }
    override val recipeRepository: RecipeRepository by lazy {
        OfflineRecipeRepository(CookDatabase.getDatabase(context, scope).recipeDao())
    }
    override val instructionRepository: InstructionRepository by lazy {
        OfflineInstructionRepository(CookDatabase.getDatabase(context, scope).instructionDao())
    }
    override val recipeIngredientRepository: RecipeIngredientRepository by lazy {
        OfflineRecipeIngredientRepository(CookDatabase.getDatabase(context, scope).recipeIngredientDao())
    }
    override val shoppingListRepository: ShoppingListRepository by lazy {
        OfflineShoppingListRepository(CookDatabase.getDatabase(context, scope).shoppingListDao())
    }
}