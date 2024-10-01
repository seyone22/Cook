package com.seyone22.cook.helper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object RecipeFileHandler {
    suspend fun exportRecipe(
        context: Context,
        recipe: Recipe,
        instructions: List<Instruction?>,
        recipeIngredients: List<RecipeIngredient?>,
        ingredients: List<Ingredient?>,
        images: List<RecipeImage?>
    ): File {
        return withContext(Dispatchers.IO) {
            val jsonRecipe = Json.encodeToString(recipe)
            val jsonInstructions = Json.encodeToString(instructions)
            val jsonRecipeIngredients = Json.encodeToString(recipeIngredients)
            val jsonIngredients = Json.encodeToString(ingredients)

            val tempFile = File(context.cacheDir, "${recipe.name}.recipe")
            ZipOutputStream(FileOutputStream(tempFile)).use { zip ->
                zip.putNextEntry(ZipEntry("recipe.json"))
                zip.write(jsonRecipe.toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry("instructions.json"))
                zip.write(jsonInstructions.toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry("recipeIngredients.json"))
                zip.write(jsonRecipeIngredients.toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry("ingredients.json"))
                zip.write(jsonIngredients.toByteArray())
                zip.closeEntry()

                images.forEach { image ->
                    val imageFile = File(image?.imagePath ?: "")
                    zip.putNextEntry(ZipEntry("images/${imageFile.name}"))
                    zip.write(imageFile.readBytes())
                    zip.closeEntry()
                }
            }
            return@withContext tempFile
        }
    }


    suspend fun importRecipe(
        context: Context,
        uri: Uri,
        recipeRepository: RecipeRepository,
        instructionRepository: InstructionRepository,
        recipeIngredientRepository: RecipeIngredientRepository,
        recipeImageRepository: RecipeImageRepository,
        ingredientRepository: IngredientRepository
    ) {
        return withContext(Dispatchers.IO) {
            val tempDir = File(context.cacheDir, "imported_recipe")
            if (!tempDir.exists()) {
                tempDir.mkdir()
            }

            val extractedFiles = extractFilesFromUri(context, uri, tempDir)
            val filesInTempDir = tempDir.listFiles()
            filesInTempDir?.forEach { file ->
                // Log the file name
                Log.d("FileList", file.name)
            }

            // Read data from files
            val recipeFile = File(tempDir, "recipe.json")
            val instructionsFile = File(tempDir, "instructions.json")
            val recipeIngredientsFile = File(tempDir, "recipeIngredients.json")
            val ingredientsFile = File(tempDir, "ingredients.json")

            val recipeJson = recipeFile.readText()
            val instructionsJson = instructionsFile.readText()
            val recipeIngredientsJson = recipeIngredientsFile.readText()
            val ingredientsJson = ingredientsFile.readText()

            // Parse data to objects
            val recipe = Json.decodeFromString<Recipe>(recipeJson)
            val instructions = Json.decodeFromString<List<Instruction>>(instructionsJson)
            val recipeIngredients =
                Json.decodeFromString<List<RecipeIngredient>>(recipeIngredientsJson)
            val ingredients = Json.decodeFromString<List<Ingredient>>(ingredientsJson)

            // Insert data to db
            if(recipeRepository.getRecipeById(recipe.id).firstOrNull() != null) {
                throw Exception("Recipe with id ${recipe.id} already exists")
            }

            recipeRepository.insertRecipe(recipe)
            instructions.forEach { instructionRepository.insertInstruction(it) }
            ingredients.forEach { ingredient ->
                val existingIngredient =
                    ingredientRepository.getIngredientByName(ingredient.nameEn).firstOrNull()

                if (existingIngredient == null) {
                    // Ingredient with similar name does not exist, proceed with insertion
                    val ingredientToAdd = ingredient.copy(id = 0L) // Clear the ID field
                    val newId = ingredientRepository.insertIngredient(ingredientToAdd)
                    recipeIngredients.find { ri -> ri.ingredientId == ingredient.id }?.let { ri ->
                        recipeIngredientRepository.insertRecipeIngredient(ri.copy(ingredientId = newId))
                    }
                } else {
                    // Ingredient with similar name already exists, handle accordingly
                    Log.d(
                        "IngredientValidation",
                        "Ingredient '${ingredient.nameEn}' already exists"
                    )
                    recipeIngredients.find { ri -> ri.ingredientId == ingredient.id }?.let { ri ->
                        recipeIngredientRepository.insertRecipeIngredient(ri.copy(ingredientId = existingIngredient.id))
                    }
                }
            }

            Log.d("TAG", "importRecipe: $extractedFiles")

            extractedFiles.filter { it.name.contains("images__recipe_") }.forEach { imageFile ->
                if (imageFile.exists()) {
                    val imagePath = saveImageToInternalStorage(context, imageFile)
                    recipeImageRepository.insertRecipeImage(
                        RecipeImage(
                            recipeId = recipe.id, imagePath = imagePath
                        )
                    )
                }
            }
        }
    }

    private fun extractFilesFromUri(context: Context, uri: Uri, destinationDir: File): List<File> {
        val extractedFiles = mutableListOf<File>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zip ->
                var entry: ZipEntry?
                while (zip.nextEntry.also { entry = it } != null) {
                    val sanitizedFileName = entry!!.name.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
                    val file = File(destinationDir, sanitizedFileName)
                    Log.d(
                        "ExtractedFile", "Extracted file path: ${file.absolutePath}"
                    ) // Log the constructed file path
                    file.outputStream().use { fileOut ->
                        zip.copyTo(fileOut)
                    }
                    extractedFiles.add(file)
                    zip.closeEntry()
                }
            }
        }

        return extractedFiles
    }


    private fun saveImageToInternalStorage(context: Context, imageFile: File): String {
        val destinationFile = File(context.filesDir, imageFile.name)
        imageFile.copyTo(destinationFile, overwrite = true)
        return destinationFile.absolutePath
    }

    fun compressImageFile(bitmap: Bitmap, quality: Int): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        return outputStream.toByteArray()
    }
}