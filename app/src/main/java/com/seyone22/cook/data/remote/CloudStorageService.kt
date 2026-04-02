package com.seyone22.cook.data.remote

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID

class CloudStorageService {
    private val storage = Firebase.storage
    private val auth = Firebase.auth

    suspend fun uploadRecipeImage(recipeId: String, localImagePath: String): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not logged in"))

        return try {
            val fileUri = Uri.fromFile(File(localImagePath))
            val imageId = UUID.randomUUID().toString()

            // Path: users/{uid}/recipes/{recipeId}/{imageId}.jpg
            val storageRef = storage.reference
                .child("users/$uid/recipes/$recipeId/$imageId.jpg")

            // Upload and wait for completion
            storageRef.putFile(fileUri).await()

            // Get the public download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}