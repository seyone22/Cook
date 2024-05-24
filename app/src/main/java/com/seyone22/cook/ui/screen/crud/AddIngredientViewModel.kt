package com.seyone22.cook.ui.screen.crud

import android.content.Context
import android.net.Uri
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddIngredientViewModel(
    private val ingredientRepository: IngredientRepository,
    private val ingredientVariantRepository: IngredientVariantRepository,
    private val ingredientImageRepository: IngredientImageRepository,
    private val measureRepository: MeasureRepository
): ViewModel() {
    private val _addIngredientViewState = MutableStateFlow(AddIngredientViewState(emptyList()))
    val addIngredientViewState: StateFlow<AddIngredientViewState> get() = _addIngredientViewState

    fun fetchData() {
        viewModelScope.launch {
            val measures = measureRepository.getAllMeasures().first()
            _addIngredientViewState.value = AddIngredientViewState(measures)
        }
    }

    fun saveIngredient(ingredient: Ingredient, ingredientVariantList: List<IngredientVariant>, images: List<Uri>?, context: Context) {
        viewModelScope.launch {
            try {
                // Insert the ingredient into the database
                val ingredientId = ingredientRepository.insertIngredient(ingredient)

                // Update the ingredientId for each variant and insert them into the database
                val updatedVariants = ingredientVariantList.map { variant ->
                    variant.copy(ingredientId = ingredientId)
                }
                updatedVariants.forEach {variant ->
                    ingredientVariantRepository.insertIngredientVariant(variant)
                }
                // Save the images
                val imageHelper = ImageHelper(context)
                images?.forEach { image ->
                    val imageBitmap = imageHelper.loadImageFromUri(image)!!
                    val imagePath = imageHelper.saveImageToInternalStorage(imageBitmap, "'ingredient_${ingredientId}_${System.currentTimeMillis()}.jpg")
                    val ingredientImage = IngredientImage(ingredientId = ingredientId, imagePath = imagePath ?: "NULL")
                    ingredientImageRepository.insertIngredientImage(ingredientImage)
                }
            } catch (e: Exception) {
                // Handle any errors that might occur during the database operations
                e.printStackTrace()
            }
        }
    }
}

// Define a data class to hold the list of measures
data class AddIngredientViewState(
    val measures: List<Measure?>
)
