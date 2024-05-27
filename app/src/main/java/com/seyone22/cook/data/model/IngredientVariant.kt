package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredient_variants")
data class IngredientVariant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ingredientId: Long,
    val variantName: String,
    val brand: String?,
    val type: String?,
    val price: Double?,
    val quantity: Int,
    val unitId: Long
)

data class IngredientVariantDetails(
    val id: Long = 0,
    val ingredientId: Long = 0,
    val variantName: String = "",
    val brand: String = "",
    val type: String = "",
    val price: String = "",
    val quantity: String = "",
    val unitId: Long = 0
)

fun IngredientVariantDetails.toIngredientVariant(): IngredientVariant = IngredientVariant(
    id = id,
    ingredientId = ingredientId,
    variantName = variantName,
    brand = brand,
    type = type,
    price = price.toDoubleOrNull() ?: 0.0,
    quantity = quantity.toIntOrNull() ?: 0,
    unitId = unitId
)

fun IngredientVariant.toIngredientVariantDetails(): IngredientVariantDetails = IngredientVariantDetails(
    id = id,
    ingredientId = ingredientId,
    variantName = variantName,
    brand = brand ?: "",
    type = type ?: "",
    price = price?.toString() ?: "",
    quantity = quantity.toString(),
    unitId = unitId
)
