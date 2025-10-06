package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.seyone22.cook.data.converters.InstantSerializer
import com.seyone22.cook.helper.UuidSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
@Entity(tableName = "recipes")
data class Recipe(
    @Serializable(with = UuidSerializer::class)
    @Contextual
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String?,
    val prepTime: Int,
    val cookTime: Int,
    val servingSize: Int,
    val reference: String?,
    val timesMade: Int = 0,
    val author: String? = null,
    val videoUrl: String? = null,

    @Serializable(with = InstantSerializer::class)
    val dateCreated: Instant = Instant.now(),

    @Serializable(with = InstantSerializer::class)
    val dateModified: Instant = Instant.now(),

    @Serializable(with = InstantSerializer::class)
    val dateAccessed: Instant = Instant.now()

    )

data class RecipeDetails(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val prepTime: String,
    val cookTime: String,
    val servingSize: String,
    val reference: String,
    val timesMade: String,
    val author: String = "",
    val videoUrl: String? = null,
    val dateCreated: Instant = Instant.now(),
    val dateModified: Instant = Instant.now(),
    val dateAccessed: Instant = Instant.now(),
)

fun RecipeDetails.toRecipe(): Recipe = Recipe(
    id = id,
    name = name,
    description = description,
    prepTime = prepTime.toInt(),
    cookTime = cookTime.toInt(),
    servingSize = servingSize.toInt(),
    reference = reference,
    timesMade = timesMade.toInt(),
    author = author,
    videoUrl = videoUrl,
    dateCreated = dateCreated,
    dateModified = dateModified,
    dateAccessed = dateAccessed,
)

fun Recipe.toRecipeDetails(): RecipeDetails = RecipeDetails(
    id = id,
    name = name,
    description = description ?: "",
    prepTime = prepTime.toString(),
    cookTime = cookTime.toString(),
    servingSize = servingSize.toString(),
    reference = reference ?: "",
    timesMade = timesMade.toString(),
    author = author ?: "",
    videoUrl = videoUrl,
    dateCreated = dateCreated,
    dateModified = dateModified,
    dateAccessed = dateAccessed,
)