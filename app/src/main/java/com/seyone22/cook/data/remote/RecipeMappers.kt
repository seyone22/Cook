package com.seyone22.cook.data.remote

import com.seyone22.cook.data.model.FullRecipe
import com.seyone22.cook.data.remote.model.CloudIngredient
import com.seyone22.cook.data.remote.model.CloudInstruction
import com.seyone22.cook.data.remote.model.CloudRecipe

fun FullRecipe.toCloudRecipe(): CloudRecipe {
    return CloudRecipe(
        id = recipe.id.toString(),
        name = recipe.name,
        description = recipe.description,
        prepTime = recipe.prepTime,
        cookTime = recipe.cookTime,
        servingSize = recipe.servingSize,
        reference = recipe.reference,
        timesMade = recipe.timesMade,
        author = recipe.author,
        videoUrl = recipe.videoUrl,

        // Timestamps
        dateCreated = recipe.dateCreated.toEpochMilli(),
        dateModified = recipe.dateModified.toEpochMilli(),
        dateAccessed = recipe.dateAccessed.toEpochMilli(),

        // Sync & Security
        ownerUid = recipe.ownerUid ?: "",
        // Note: imageUrls are empty here. The SyncWorker will upload the local images
        // to Firebase Storage first, get the URLs, and attach them before pushing to Firestore.
        imageUrls = emptyList(),

        // 1. Map Ingredients
        ingredients = ingredients.map { localIng ->
            CloudIngredient(
                foodDbId = localIng.foodDbId,
                name = localIng.name,
                quantity = localIng.quantity,
                unit = localIng.unit,
                notes = localIng.notes
            )
        },

        // 2. Map Instructions (and attach Section Names)
        instructions = instructions.map { localInst ->
            // Find the matching section name if this instruction belongs to one
            val section = sections.find { it.id.toInt() == localInst.sectionId }

            CloudInstruction(
                stepNumber = localInst.stepNumber,
                description = localInst.description,
                sectionName = section?.name,
                sectionNumber = section?.sectionNumber
            )
        }
    )
}