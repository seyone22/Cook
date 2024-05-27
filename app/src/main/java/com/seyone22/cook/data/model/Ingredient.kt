package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameEn: String,
    val nameSi: String,
    val nameTa: String,
    val description: String?,
    val stocked: Boolean = false
)

data class IngredientDetails(
    val id: Long = 0,
    val nameEn: String = "",
    val nameSi: String = "",
    val nameTa: String = "",
    val description: String = "",
    val stocked: Boolean = false
)

fun IngredientDetails.toIngredient(): Ingredient = Ingredient(
    id = id,
    nameEn = nameEn,
    nameSi = nameSi,
    nameTa = nameTa,
    description = description,
    stocked = stocked
)

fun Ingredient.toIngredientDetails(): IngredientDetails = IngredientDetails(
    id = id,
    nameEn = nameEn,
    nameSi = nameSi,
    nameTa = nameTa,
    description = description ?: "",
    stocked = stocked
)
