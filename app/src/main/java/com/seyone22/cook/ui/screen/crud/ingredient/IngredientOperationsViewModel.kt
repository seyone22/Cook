package com.seyone22.cook.ui.screen.crud.ingredient

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientImage
import com.seyone22.cook.data.model.IngredientVariant
import com.seyone22.cook.data.model.Measure
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientImage.IngredientImageRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.data.repository.measure.MeasureRepository
import com.seyone22.cook.helper.ImageHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class IngredientOperationsViewModel(
    private val ingredientRepository: IngredientRepository,
    private val ingredientVariantRepository: IngredientVariantRepository,
    private val ingredientImageRepository: IngredientImageRepository,
    private val measureRepository: MeasureRepository
) : ViewModel() {
    private val _addIngredientViewState = MutableStateFlow(AddIngredientViewState())
    val addIngredientViewState: StateFlow<AddIngredientViewState> get() = _addIngredientViewState

    fun fetchData(id: Long = -1) {
        viewModelScope.launch {
            val measures = measureRepository.getAllMeasures().first()
            var ingredient: Ingredient? = null
            var variants: List<IngredientVariant?> = emptyList()
            var photos: List<IngredientImage?> = emptyList()
            if (id.toInt() != -1) {
                ingredient = ingredientRepository.getIngredientById(id.toInt()).first()
                variants = ingredientVariantRepository.getVariantsForIngredient(id.toInt()).first()
                photos = ingredientImageRepository.getImagesForIngredient(id.toInt()).first()
            }
            _addIngredientViewState.value =
                AddIngredientViewState(measures, ingredient, variants, photos)
        }
    }

    fun saveIngredient(
        ingredient: Ingredient,
        ingredientVariantList: List<IngredientVariant>,
        images: List<Uri>?,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                // Insert the ingredient into the database
                val ingredientId = ingredientRepository.insertIngredient(ingredient)

                // Update the ingredientId for each variant and insert them into the database
                val updatedVariants = ingredientVariantList.map { variant ->
                    variant.copy(ingredientId = ingredientId)
                }
                updatedVariants.forEach { variant ->
                    ingredientVariantRepository.insertIngredientVariant(variant)
                }
                // Save the images
                val imageHelper = ImageHelper(context)
                images?.forEachIndexed { index, image ->
                    val imageBitmap = imageHelper.loadImageFromUri(image)!!
                    val imagePath = imageHelper.saveImageToInternalStorage(
                        imageBitmap,
                        "ingredient_${ingredientId}_${index}_${System.currentTimeMillis()}.jpg"
                    )
                    val ingredientImage = IngredientImage(
                        ingredientId = ingredientId, imagePath = imagePath ?: "NULL"
                    )
                    ingredientImageRepository.insertIngredientImage(ingredientImage)
                }
            } catch (e: Exception) {
                // Handle any errors that might occur during the database operations
                e.printStackTrace()
            }
        }
    }

    fun updateIngredient(
        ingredient: Ingredient,
        newVariants: List<IngredientVariant?>,
        newImages: List<IngredientImage?>,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    Log.d("TAG", "updateIngredient: Start")

                    // Update the ingredient in the database
                    ingredientRepository.updateIngredient(ingredient)
                    Log.d("TAG", "updateIngredient: Ingredient updated")

                    // Fetch the current variants and images from the database
                    val currentVariants =
                        ingredientVariantRepository.getVariantsForIngredient(ingredient.id.toInt())
                            .first()
                    val currentImages =
                        ingredientImageRepository.getImagesForIngredient(ingredient.id.toInt())
                            .first()
                    Log.d("TAG", "updateIngredient: Fetched current variants and images")

                    // Determine variants to add, update, and delete
                    val variantsToAdd =
                        newVariants.filter { it != null && currentVariants.find { i -> i!!.id == it.id } == null }
                    val variantsToUpdate =
                        newVariants.filter { it != null && currentVariants.find { i -> i!!.id == it.id } != null }
                    val variantsToDelete =
                        currentVariants.filter { it != null && newVariants.find { i -> i!!.id == it.id } == null }

                    Log.d("TAG", "updateIngredient: variantsToAdd = $variantsToAdd")
                    Log.d("TAG", "updateIngredient: variantsToUpdate = $variantsToUpdate")
                    Log.d("TAG", "updateIngredient: variantsToDelete = $variantsToDelete")

                    // Perform variant operations
                    variantsToAdd.forEach { variant ->
                        variant?.let {
                            Log.d("TAG", "Adding variant: $it")
                            ingredientVariantRepository.insertIngredientVariant(it)
                        }
                    }

                    variantsToUpdate.forEach { variant ->
                        variant?.let {
                            Log.d("TAG", "Updating variant: $it")
                            ingredientVariantRepository.updateIngredientVariant(it)
                        }
                    }

                    variantsToDelete.forEach { variant ->
                        Log.d("TAG", "Deleting variant: $variant")
                        ingredientVariantRepository.deleteIngredientVariant(variant!!)
                    }

                    // Determine images to add, update, and delete
                    val imagesToAdd = newImages.filter { it != null && !currentImages.contains(it) }
                    val imagesToUpdate =
                        newImages.filter { it != null && currentImages.contains(it) }
                    val imagesToDelete = currentImages.filter { !newImages.contains(it) }

                    Log.d("TAG", "updateIngredient: imagesToAdd = $imagesToAdd")
                    Log.d("TAG", "updateIngredient: imagesToUpdate = $imagesToUpdate")
                    Log.d("TAG", "updateIngredient: imagesToDelete = $imagesToDelete")

                    val imageHelper = ImageHelper(context)

                    // Perform image operations
                    imagesToAdd.forEachIndexed { index, image ->
                        image?.let {
                            try {
                                val imageBitmap =
                                    imageHelper.loadImageFromUri(Uri.parse(image.imagePath))
                                Log.d("TAG", "Loaded image bitmap: $imageBitmap")

                                imageBitmap?.let {
                                    val imagePath = imageHelper.saveImageToInternalStorage(
                                        it,
                                        "ingredient_${ingredient.id}_${index}_${System.currentTimeMillis()}.jpg"
                                    )

                                    Log.d("TAG", "Saved image to internal storage: $imagePath")

                                    val ingredientImage = IngredientImage(
                                        ingredientId = ingredient.id,
                                        imagePath = imagePath ?: "NULL"
                                    )
                                    Log.d("TAG", "updateIngredient: $ingredientImage")
                                    ingredientImageRepository.insertIngredientImage(ingredientImage)
                                }
                            } catch (e: Exception) {
                                Log.e("TAG", "Error adding image: ${e.message}", e)
                            }
                        }
                    }

                    imagesToDelete.forEach { image ->
                        image?.let {
                            try {
                                ingredientImageRepository.deleteIngredientImage(it)
                                Log.d("TAG", "Deleted image: $it")

                                imageHelper.deleteImageFromInternalStorage(Uri.parse(it.imagePath))
                            } catch (e: Exception) {
                                Log.e("TAG", "Error deleting image: $image", e)
                            }
                        }
                    }

                    Log.d("TAG", "updateIngredient: Completed successfully")
                }
            } catch (e: CancellationException) {
                Log.e("TAG", "updateIngredient: Job was cancelled", e)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", "updateIngredient: Error occurred", e)
            }
        }
    }

}

// Define a data class to hold the list of measures
data class AddIngredientViewState(
    val measures: List<Measure?> = emptyList(),
    val ingredient: Ingredient? = null,
    val variants: List<IngredientVariant?> = emptyList(),
    val photos: List<IngredientImage?> = emptyList()
)