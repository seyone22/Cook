package com.seyone22.cook.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
    tableName = "meal_entries",
    indices = [Index("recipeId")]
)
data class MealEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryDate: LocalDate,
    val lastUpdated: LocalDateTime,
    val imageUri: String? = null,
    val notes: String = "",
    val recipeId: Long? = null
)


@Entity(
    tableName = "meal_entry_tag_cross_ref",
    primaryKeys = ["mealEntryId", "tagId"]
)
data class MealEntryTagCrossRef(
    val mealEntryId: Long,
    val tagId: Long
)

@Entity(
    tableName = "meal_entry_ingredient_cross_ref",
    primaryKeys = ["mealEntryId", "ingredientId"],
    indices = [Index(value = ["ingredientId"]), Index(value = ["mealEntryId"])]
)
data class MealEntryIngredientCrossRef(
    val mealEntryId: Long,
    val ingredientId: Long,
    val quantity: Double,
    val unit: String
)





data class IngredientWithQuantity(
    @Embedded val ingredient: Ingredient,
    val quantity: Double,
    val unit: String
)

data class MealEntryWithDetails(
    val entry: MealEntry,
    val tags: List<Tag>,
    val ingredients: List<IngredientWithQuantity>
)
