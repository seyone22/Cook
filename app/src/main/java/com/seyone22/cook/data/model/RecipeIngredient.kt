package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_ingredients")
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val ingredientId: Long,
    val quantity: Double,
    val unit: String
)