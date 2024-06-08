package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.seyone22.cook.helper.UUIDSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @Contextual
    @Serializable(with = UUIDSerializer::class)
    val recipeId: UUID,
    val ingredientId: Long,
    val quantity: Double,
    val measureId: Long
)

data class RecipeIngredientDetails(
    val id: Long = 0,
    val recipeId: UUID,
    val ingredientId: Long,
    val quantity: String,
    val measureId: Long
)

fun RecipeIngredientDetails.toRecipeIngredient(): RecipeIngredient = RecipeIngredient(
    id = id,
    recipeId = recipeId,
    ingredientId = ingredientId,
    quantity = quantity.toDoubleOrNull() ?: 0.0,
    measureId = measureId
)

fun RecipeIngredient.toRecipeIngredientDetails(): RecipeIngredientDetails = RecipeIngredientDetails(
    id = id,
    recipeId = recipeId,
    ingredientId = ingredientId,
    quantity = quantity.toString(),
    measureId = measureId
)
