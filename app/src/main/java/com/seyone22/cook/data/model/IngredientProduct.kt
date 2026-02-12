package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.seyone22.cook.helper.UuidSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(tableName = "ingredient_variants")
data class IngredientProduct(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ingredientId: String,
    val uniqueId: String,
    val productName: String,
    val source: String?,
    val item_unit: String?,
    val price: Double?,
    val quantity: Double,
    val currency: String,
    val last_fetched: String,
    val image: String,
)

data class IngredientProductDetails(
    val id: Long = 0,
    val uniqueId: String = "",
    val ingredientId: String = "",
    val variantName: String = "",
    val brand: String = "",
    val type: String = "",
    val price: String = "",
    val quantity: String = "",
    val currency: String = "",
    val last_fetched: String = "",
    val image: String = "",
)

fun IngredientProductDetails.toIngredientVariant(): IngredientProduct = IngredientProduct(
    id = id,
    uniqueId = uniqueId,
    ingredientId = ingredientId,
    productName = variantName,
    source = brand,
    item_unit = type,
    price = price.toDoubleOrNull() ?: 0.0,
    quantity = quantity.toDoubleOrNull() ?: 0.0,
    currency = currency,
    last_fetched = last_fetched,
    image = image,
)

fun IngredientProduct.toIngredientVariantDetails(): IngredientProductDetails =
    IngredientProductDetails(
        id = id,
        uniqueId = uniqueId,
        ingredientId = ingredientId,
        variantName = productName,
        brand = source ?: "",
        type = item_unit ?: "",
        price = price?.toString() ?: "",
        quantity = quantity.toString(),
        currency = currency,
        last_fetched = last_fetched,
        image = image,
    )
