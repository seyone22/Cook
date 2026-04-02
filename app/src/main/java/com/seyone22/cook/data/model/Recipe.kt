package com.seyone22.cook.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
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
    val dateAccessed: Instant = Instant.now(),

    // --- CLOUD SYNC FIELDS ---
    val firestoreId: String? = null,
    val ownerUid: String? = null,
    val remoteImageUrl: String? = null,
    val syncStatus: String = "LOCAL_ONLY",
    val isReadOnly: Boolean = false,

    // --- NEW: QUICK SHARE FIELDS ---
    val shareMode: String = "PRIVATE", // Options: "PRIVATE", "ANYONE", "SPECIFIC"
    val allowedEmails: List<String> = emptyList()
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

    // Pass these to the UI state holder
    val firestoreId: String? = null,
    val ownerUid: String? = null,
    val remoteImageUrl: String? = null,
    val syncStatus: String = "LOCAL_ONLY",
    val isReadOnly: Boolean = false,

    // New Sharing Fields
    val shareMode: String = "PRIVATE",
    val allowedEmails: List<String> = emptyList()
)

fun RecipeDetails.toRecipe(): Recipe = Recipe(
    id = id,
    name = name,
    description = description,
    prepTime = prepTime.toIntOrNull() ?: 0,
    cookTime = cookTime.toIntOrNull() ?: 0,
    servingSize = servingSize.toIntOrNull() ?: 1,
    reference = reference,
    timesMade = timesMade.toIntOrNull() ?: 0,
    author = author,
    videoUrl = videoUrl,
    dateCreated = dateCreated,
    dateModified = dateModified,
    dateAccessed = dateAccessed,
    firestoreId = firestoreId,
    ownerUid = ownerUid,
    remoteImageUrl = remoteImageUrl,
    syncStatus = syncStatus,
    isReadOnly = isReadOnly,
    shareMode = shareMode,
    allowedEmails = allowedEmails
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
    firestoreId = firestoreId,
    ownerUid = ownerUid,
    remoteImageUrl = remoteImageUrl,
    syncStatus = syncStatus,
    isReadOnly = isReadOnly,
    shareMode = shareMode,
    allowedEmails = allowedEmails
)

// This is just a data class to hold the query result. Not an @Entity!
data class FullRecipe(
    @Embedded val recipe: Recipe,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val images: List<RecipeImage>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<RecipeIngredient>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val instructions: List<Instruction>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val sections: List<InstructionSection>
)