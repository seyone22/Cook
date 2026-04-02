package com.seyone22.cook.data.remote

import androidx.room.withTransaction
import com.google.firebase.auth.FirebaseAuth
import com.seyone22.cook.data.CookDatabase
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.InstructionSection
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.remote.model.CloudRecipe
import java.time.Instant
import java.util.UUID

class SyncRepository(
    private val db: CookDatabase,
    private val firestoreService: FirestoreService
) {
    // Pull DAOs directly from the DB instance
    private val recipeDao = db.recipeDao()
    private val ingredientDao = db.recipeIngredientDao()
    private val instructionDao = db.instructionDao()
    private val sectionDao = db.instructionSectionDao()

    suspend fun performStartupSync() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val uid = currentUser.uid

        val remoteResult = firestoreService.getAllRecipesForUser(uid)
        val remoteRecipes = remoteResult.getOrNull() ?: return
        val localRecipes = recipeDao.getAllRecipesSnapshot()

        val remoteMap = remoteRecipes.associateBy { it.id }
        val localSyncedMap = localRecipes
            .filter { it.firestoreId != null }
            .associateBy { it.id.toString() }

        // 1. DELETE: Local synced exists, but Remote is gone
        (localSyncedMap.keys - remoteMap.keys).forEach { id ->
            recipeDao.deleteFullRecipeData(UUID.fromString(id))
        }

        // 2. NEW & MODIFIED
        remoteRecipes.forEach { cloudRecipe ->
            val localMatch = localSyncedMap[cloudRecipe.id]

            if (localMatch == null) {
                downloadAndSaveRecipe(cloudRecipe)
            } else {
                val remoteMod = cloudRecipe.dateModified
                val localMod = localMatch.dateModified.toEpochMilli()

                if (remoteMod > localMod) {
                    downloadAndSaveRecipe(cloudRecipe)
                }
            }
        }
    }

    private suspend fun downloadAndSaveRecipe(cloudRecipe: CloudRecipe) {
        val recipeId = UUID.fromString(cloudRecipe.id)

        db.withTransaction {
            // A. Wipe existing data
            recipeDao.deleteFullRecipeData(recipeId)

            // B. Insert Main Recipe
            recipeDao.insert(
                Recipe(
                    id = recipeId,
                    name = cloudRecipe.name,
                    description = cloudRecipe.description,
                    prepTime = cloudRecipe.prepTime,
                    cookTime = cloudRecipe.cookTime,
                    servingSize = cloudRecipe.servingSize,
                    reference = cloudRecipe.reference,
                    timesMade = cloudRecipe.timesMade,
                    author = cloudRecipe.author,
                    videoUrl = cloudRecipe.videoUrl,
                    dateCreated = Instant.ofEpochMilli(cloudRecipe.dateCreated),
                    dateModified = Instant.ofEpochMilli(cloudRecipe.dateModified),
                    dateAccessed = Instant.now(),
                    firestoreId = cloudRecipe.id,
                    ownerUid = cloudRecipe.ownerUid,
                    syncStatus = "SYNCED",
                    shareMode = cloudRecipe.shareMode,
                    allowedEmails = cloudRecipe.allowedEmails
                )
            )

            // C. Insert Ingredients
            cloudRecipe.ingredients.forEach {
                ingredientDao.insert(
                    RecipeIngredient(
                        recipeId = recipeId,
                        ingredientId = UUID.randomUUID(),
                        foodDbId = it.foodDbId,
                        name = it.name,
                        quantity = it.quantity,
                        unit = it.unit,
                        notes = it.notes
                    )
                )
            }

            // D. Insert Sections and link Instructions
            val groupedBySection = cloudRecipe.instructions.groupBy { it.sectionName }

            groupedBySection.forEach { (sectionName, cloudInsts) ->
                var localSectionId: Int? = null

                if (sectionName != null) {
                    val newSectionId = sectionDao.insert(
                        InstructionSection(
                            recipeId = recipeId,
                            name = sectionName,
                            sectionNumber = cloudInsts.firstOrNull()?.sectionNumber ?: 0
                        )
                    )
                    localSectionId = newSectionId.toInt()
                }

                cloudInsts.forEach { ci ->
                    instructionDao.insert(
                        Instruction(
                            recipeId = recipeId,
                            sectionId = localSectionId,
                            stepNumber = ci.stepNumber,
                            description = ci.description
                        )
                    )
                }
            }
        }
    }
}