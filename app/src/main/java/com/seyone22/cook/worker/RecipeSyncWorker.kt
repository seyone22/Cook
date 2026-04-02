package com.seyone22.cook.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.seyone22.cook.data.CookDatabase
import com.seyone22.cook.data.remote.CloudStorageService
import com.seyone22.cook.data.remote.FirestoreService
import com.seyone22.cook.data.remote.toCloudRecipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.util.UUID

class RecipeSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val firestoreService = FirestoreService()
    private val cloudStorageService = CloudStorageService()

    // Database setup
    private val databaseScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database = CookDatabase.getDatabase(appContext, databaseScope)
    private val recipeDao = database.recipeDao()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // --- 1. THE AUTH GATE ---
        // We get a fresh instance and check the CURRENT state.
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Log.e("SyncWorker", "SYNC ABORTED: No user is currently logged in.")
            // We return Failure (not Retry) because without a user,
            // retrying will just keep failing until a new login happens.
            return@withContext Result.failure()
        }

        // Capture the UID immediately so it's consistent for this entire run
        val uid = currentUser.uid
        Log.d("SyncWorker", "Starting sync for User: $uid")

        // --- 2. INPUT VALIDATION ---
        val recipeIdString = inputData.getString(KEY_RECIPE_ID) ?: return@withContext Result.failure()
        val recipeId = UUID.fromString(recipeIdString)

        try {
            // --- 3. FETCH DATA ---
            val fullRecipe = recipeDao.getFullRecipeSnapshot(recipeId)
                ?: run {
                    Log.e("SyncWorker", "Recipe $recipeId not found in local DB.")
                    return@withContext Result.failure()
                }

            // --- 4. IMAGE UPLOAD ---
            val uploadedImageUrls = mutableListOf<String>()
            for (localImage in fullRecipe.images) {
                // If it's already a cloud URL, don't re-upload
                if (localImage.imagePath.startsWith("http")) {
                    uploadedImageUrls.add(localImage.imagePath)
                    continue
                }

                // Attempt upload
                val uploadResult = cloudStorageService.uploadRecipeImage(
                    recipeId = recipeIdString,
                    localImagePath = localImage.imagePath
                )

                if (uploadResult.isSuccess) {
                    uploadedImageUrls.add(uploadResult.getOrThrow())
                } else {
                    Log.e("SyncWorker", "Image upload failed. Retrying job later.")
                    return@withContext Result.retry()
                }
            }

            // --- 5. FIRESTORE PUSH ---
            // We map the recipe and FORCE the current live UID as the owner
            val cloudRecipe = fullRecipe.toCloudRecipe().copy(
                ownerUid = uid,
                imageUrls = uploadedImageUrls
            )

            val pushResult = firestoreService.pushRecipe(cloudRecipe)

            return@withContext if (pushResult.isSuccess) {
                // --- 6. LOCAL DB UPDATE ---
                val updatedRecipe = fullRecipe.recipe.copy(
                    firestoreId = recipeIdString,
                    ownerUid = uid,
                    syncStatus = "SYNCED"
                )
                recipeDao.update(updatedRecipe)

                Log.d("SyncWorker", "Cloud Sync Successful: $recipeIdString")
                Result.success()
            } else {
                Log.w("SyncWorker", "Firestore push failed. Check rules or connection.")
                Result.retry()
            }

        } catch (e: Exception) {
            Log.e("SyncWorker", "Critical failure in SyncWorker", e)
            Result.failure()
        }
    }

    companion object {
        const val KEY_RECIPE_ID = "recipe_id"
    }
}