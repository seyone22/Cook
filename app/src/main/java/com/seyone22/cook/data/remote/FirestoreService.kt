package com.seyone22.cook.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.seyone22.cook.data.remote.model.CloudRecipe
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()
    private val recipesCollection = db.collection("recipes")

    /**
     * Pushes a recipe to Firestore.
     */
    suspend fun pushRecipe(cloudRecipe: CloudRecipe): Result<String> {
        return try {
            val documentId = cloudRecipe.id
            recipesCollection.document(documentId).set(cloudRecipe).await()
            Log.d("FirestoreService", "Successfully pushed recipe: $documentId")
            Result.success(documentId)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Failed to push recipe", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches ALL recipes owned by a specific user or shared with them.
     */
    suspend fun getAllRecipesForUser(uid: String): Result<List<CloudRecipe>> {
        return try {
            // This query matches our Security Rules:
            // 1. You are the owner OR
            // 2. Your email is in the allowed list (we'll handle email filtering in the next step)
            // For now, let's fetch based on ownerUid
            val snapshot = recipesCollection
                .whereEqualTo("ownerUid", uid)
                .get()
                .await()

            val recipes = snapshot.documents.mapNotNull { it.toObject<CloudRecipe>() }
            Result.success(recipes)
        } catch (e: Exception) {
            Log.e("FirestoreService", "Failed to fetch remote recipes", e)
            Result.failure(e)
        }
    }

    /**
     * Downloads a single recipe by its Firestore Document ID.
     * Use this when a friend scans a QR code.
     */
    suspend fun getRecipeById(recipeId: String): Result<CloudRecipe?> {
        return try {
            val document = recipesCollection.document(recipeId).get().await()
            if (document.exists()) {
                Result.success(document.toObject<CloudRecipe>())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("FirestoreService", "Failed to fetch recipe $recipeId", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a recipe from the cloud.
     */
    suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            recipesCollection.document(recipeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}