package com.seyone22.cook.data.remote.model

/**
 * The "Flat DTO" representing an entire Recipe and all its relations in Firestore.
 */
data class CloudRecipe(
    // Core Info
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val prepTime: Int = 0,
    val cookTime: Int = 0,
    val servingSize: Int = 1,
    val reference: String? = null,
    val timesMade: Int = 0,
    val author: String? = null,
    val videoUrl: String? = null,

    // Timestamps
    val dateCreated: Long = 0L,
    val dateModified: Long = 0L,
    val dateAccessed: Long = 0L,

    // Security & Sync
    val ownerUid: String = "",
    val shareMode: String = "PRIVATE", // "PRIVATE", "ANYONE", "SPECIFIC"
    val allowedEmails: List<String> = emptyList(), // Emails allowed if mode is SPECIFIC

    // Images
    val imageUrls: List<String> = emptyList(),

    // Nested Relations
    val ingredients: List<CloudIngredient> = emptyList(),
    val instructions: List<CloudInstruction> = emptyList(),
    val tags: List<String> = emptyList()
)

data class CloudIngredient(
    val foodDbId: String? = null,
    val name: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val notes: String? = null
)

data class CloudInstruction(
    val stepNumber: Int = 0,
    val description: String = "",
    val sectionName: String? = null,
    val sectionNumber: Int? = null
)