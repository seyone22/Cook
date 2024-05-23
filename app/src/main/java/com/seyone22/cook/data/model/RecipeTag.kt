package com.seyone22.cook.data.model

import androidx.room.Entity

@Entity(tableName = "recipe_tags", primaryKeys = ["recipeId", "tagId"])
data class RecipeTag(
    val recipeId: Long,
    val tagId: Long
)