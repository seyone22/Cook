package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String?,
    val prepTime: Int,
    val cookTime: Int,
    val servingSize: Int,
    val reference: String?,
    val timesMade: Int = 0
)

data class RecipeDetails(
    val id: Long = 0,
    val name: String,
    val description: String,
    val prepTime: String,
    val cookTime: String,
    val servingSize: String,
    val reference: String,
    val timesMade: String
)

fun RecipeDetails.toRecipe(): Recipe = Recipe(
    id = id,
    name = name,
    description = description,
    prepTime = prepTime.toInt(),
    cookTime = cookTime.toInt(),
    servingSize = servingSize.toInt(),
    reference = reference,
    timesMade = timesMade.toInt()
)

fun Recipe.toRecipeDetails(): RecipeDetails = RecipeDetails(
    id = id,
    name = name,
    description = description ?: "",
    prepTime = prepTime.toString(),
    cookTime = cookTime.toString(),
    servingSize = servingSize.toString(),
    reference = reference ?: "",
    timesMade = timesMade.toString()
)