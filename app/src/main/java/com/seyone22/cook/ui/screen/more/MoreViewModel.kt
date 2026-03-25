package com.seyone22.cook.ui.screen.more

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks.await
import com.seyone22.cook.BaseViewModel
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import com.seyone22.cook.data.repository.tag.TagRepository
import com.seyone22.cook.helper.RecipeFileHandler
import com.seyone22.cook.service.RecipeImportService
import com.seyone22.cook.service.getIngredientPrices
import com.seyone22.cook.ui.common.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import com.seyone22.cook.provider.KtorClientProvider.client
import com.seyone22.cook.service.getProducts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

class MoreViewModel(
    private val recipeRepository: RecipeRepository,
    private val recipeImageRepository: RecipeImageRepository,
    private val instructionRepository: InstructionRepository,
    private val recipeIngredientRepository: RecipeIngredientRepository,
    private val ingredientProductRepository: IngredientVariantRepository,
    private val ingredientRepository: IngredientRepository,
    private val tagRepository: TagRepository,
    private val recipeImportService: RecipeImportService = RecipeImportService() // default instance
) : BaseViewModel() {
    private val _moreViewState = MutableStateFlow(ViewState())
    val moreViewState: StateFlow<ViewState> get() = _moreViewState

    fun fetchTags() {
        viewModelScope.launch {
            val tags = tagRepository.getAllTags().first()
            _moreViewState.value = _moreViewState.value.copy(tags = tags,)
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

    fun updateIngredients(context: Context) {
        viewModelScope.launch {
            try {
                //_moreViewState.value = _moreViewState.value.copy(isLoading = true)

                // 1️⃣ Get all product variants from local DB
                val allIngredients = ingredientRepository.getAllIngredients().firstOrNull() ?: emptyList()
                if (allIngredients.isEmpty()) {
                    Toast.makeText(context, "No ingredients found in DB.", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                try {
                    val batchSize = 50
                    // We'll collect all updates to run a single DB transaction per batch
                    val ingredientsToUpdate = mutableListOf<Ingredient>()

                    allIngredients.chunked(batchSize).forEach { batch ->
                        val productIds = batch.mapNotNull { it?.foodDbId?.takeIf { id -> id.isNotBlank() } }
                        val updatedProductsJson = getProducts(client, productIds)

                        Log.d("Log", "Nigger")


                        updatedProductsJson.forEach { prodJson ->
                            try {
                                val prodId = prodJson["_id"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                                val localProduct = batch.firstOrNull { it?.foodDbId == prodId } ?: return@forEach



                                val updatedProduct = localProduct.copy(
                                    country = prodJson["country"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: localProduct.country,
                                    region = prodJson["region"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: localProduct.region,
                                    cuisine = prodJson["cuisine"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: localProduct.cuisine,
                                    flavor_profile = prodJson["flavor_profile"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: localProduct.flavor_profile,
                                    dietary_flags = prodJson["dietary_flags"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: localProduct.dietary_flags,
                                    comment = prodJson["comment"]?.jsonPrimitive?.contentOrNull ?: localProduct.comment,
                                    category = prodJson["category"]?.jsonPrimitive?.contentOrNull ?: localProduct.category,
                                    product_unit = prodJson["unit"]?.jsonPrimitive?.contentOrNull ?: localProduct.product_unit,
                                    price = prodJson["price"]?.jsonPrimitive?.doubleOrNull ?: localProduct.price,
                                    currency = prodJson["currency"]?.jsonPrimitive?.contentOrNull ?: localProduct.currency,
                                    image = prodJson["url"]?.jsonPrimitive?.contentOrNull ?: localProduct.image
                                )

                                ingredientsToUpdate.add(updatedProduct)
                            } catch (e: Exception) {
                                Log.e("UpdateIngredients", "Error parsing product: ${e.localizedMessage}")
                            }
                        }

                        Log.d("Tag", ingredientsToUpdate.toString())

                        // Batch update the DB for this chunk
                        if (ingredientsToUpdate.isNotEmpty()) {
                            ingredientRepository.upsertIngredients(ingredientsToUpdate)
                            ingredientsToUpdate.clear() // Clear for next batch
                        }
                    }

                    // Show success once at the end on the Main thread
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "All ingredients refreshed!", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Log.e("UpdateIngredients", "Global Error: ${e.localizedMessage}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to refresh ingredients.", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("UpdateIngredients", "Error updating ingredients: ${e.localizedMessage}")
                Toast.makeText(context, "Failed to refresh ingredients.", Toast.LENGTH_SHORT).show()
            } finally {
                //_moreViewState.value = _moreViewState.value.copy(isLoading = false)
            }
        }
    }




    override fun onCleared() {
        super.onCleared()
        recipeImportService.close() // close client when ViewModel is destroyed
    }
}