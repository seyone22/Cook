package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_images",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class RecipeImage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val imagePath: String
)
