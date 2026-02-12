package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.seyone22.cook.helper.UuidSerializer
import com.seyone22.cook.service.ParsedIngredient
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
    @Serializable(with = UuidSerializer::class)
    val recipeId: UUID,
    @Contextual
    @Serializable(with = UuidSerializer::class)
    val ingredientId: UUID,

    val foodDbId: String?,
    val name: String,
    val quantity: Double,
    val unit: String,
    val notes: String?
)

data class RecipeIngredientDetails(
    val id: Long = 0,
    val recipeId: UUID,
    val ingredientId: UUID,

    val foodDbId: String?,
    val name: String,
    val quantity: String,
    val unit: String,
    val notes: String?
)

fun RecipeIngredientDetails.toRecipeIngredient(): RecipeIngredient = RecipeIngredient(
    id = id,
    recipeId = recipeId,
    ingredientId = ingredientId,

    foodDbId = foodDbId,
    name = name,
    quantity = quantity.toDoubleOrNull() ?: 0.0,
    unit = unit,
    notes = notes
)

fun RecipeIngredient.toRecipeIngredientDetails(): RecipeIngredientDetails = RecipeIngredientDetails(
    id = id,
    recipeId = recipeId,
    ingredientId = ingredientId,

    foodDbId = foodDbId,
    name = name,
    quantity = quantity.toString(),
    unit = unit,
    notes = notes
)

fun RecipeIngredient.toParsedIngredient(): ParsedIngredient = ParsedIngredient(
    ingredient = this.name,
    quantity = this.quantity,
    unit = this.unit,
    notes = this.notes
)