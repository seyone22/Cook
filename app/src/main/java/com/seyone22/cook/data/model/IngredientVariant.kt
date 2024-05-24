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