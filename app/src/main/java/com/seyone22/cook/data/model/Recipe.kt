package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.seyone22.cook.helper.UUIDSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "recipes")
data class Recipe(
    @Serializable(with = UUIDSerializer::class)
    @Contextual
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String?,
    val prepTime: Int,
    val cookTime: Int,
    val servingSize: Int,
    val reference: String?,
    val timesMade: Int = 0
)

data class RecipeDetails(
    val id: UUID = UUID.randomUUID(),
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