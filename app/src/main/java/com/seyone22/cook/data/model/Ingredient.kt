package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.seyone22.cook.helper.UuidSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "ingredients")
data class Ingredient(
    @Serializable(with = UuidSerializer::class)
    @Contextual
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val nameEn: String,
    val nameSi: String,
    val nameTa: String,
    val description: String?,
    val stocked: Boolean = false
)

data class IngredientDetails(
    val id: UUID = UUID.randomUUID(),
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
