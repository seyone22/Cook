package com.seyone22.cook.helper

import android.content.Context
import android.net.Uri
import com.seyone22.cook.data.model.Instruction
import com.seyone22.cook.data.model.Recipe
import com.seyone22.cook.data.model.RecipeImage
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.repository.instruction.InstructionRepository
import com.seyone22.cook.data.repository.recipe.RecipeRepository
import com.seyone22.cook.data.repository.recipeImage.RecipeImageRepository
import com.seyone22.cook.data.repository.recipeIngredient.RecipeIngredientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class DataHelper {
    suspend fun exportRecipe(
        context: Context,
        recipe: Recipe,
        instructions: List<Instruction?>,
        ingredients: List<RecipeIngredient?>,
        images: List<RecipeImage?>
    ): File {
        return withContext(Dispatchers.IO) {
            val jsonRecipe = Json.encodeToString(recipe)
            val jsonInstructions = Json.encodeToString(instructions)
            val jsonIngredients = Json.encodeToString(ingredients)

            val tempFile = File(context.cacheDir, "${recipe.name}.recipe")
            ZipOutputStream(FileOutputStream(tempFile)).use { zip ->
                zip.putNextEntry(ZipEntry("recipe.json"))
                zip.write(jsonRecipe.toByteArray())
                zip.closeEntry()

                zip.putNextEntry(ZipEntry("instructions.json"))
                zip.write(jsonInstructions.toByteArray())
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
        recipeImageRepository: RecipeImageRepository
    ) {
        return withContext(Dispatchers.IO) {
            val tempDir = File(context.cacheDir, "imported_recipe")
            if (!tempDir.exists()) {
                tempDir.mkdir()
            }

            val extractedFiles = extractFilesFromUri(context, uri, tempDir)

            val recipeFile = File(tempDir, "recipe.json")
            val instructionsFile = File(tempDir, "instructions.json")
            val ingredientsFile = File(tempDir, "ingredients.json")

            val recipeJson = recipeFile.readText()
            val instructionsJson = instructionsFile.readText()
            val ingredientsJson = ingredientsFile.readText()

            val recipe = Json.decodeFromString<Recipe>(recipeJson)
            val instructions = Json.decodeFromString<List<Instruction>>(instructionsJson)
            val ingredients = Json.decodeFromString<List<RecipeIngredient>>(ingredientsJson)

            recipeRepository.insertRecipe(recipe)
            instructions.forEach { instructionRepository.insertInstruction(it) }
            ingredients.forEach { recipeIngredientRepository.insertRecipeIngredient(it) }

            extractedFiles.filter { it.name.startsWith("images/") }.forEach { imageFile ->
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
                    val file = File(destinationDir, entry!!.name)
                    if (file.exists()) {
                        file.outputStream().use { fileOut ->
                            zip.copyTo(fileOut)
                        }
                        extractedFiles.add(file)
                    }
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
}