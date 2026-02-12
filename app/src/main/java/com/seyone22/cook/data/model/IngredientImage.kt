package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.seyone22.cook.helper.UuidSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(
    tableName = "ingredient_images",
    foreignKeys = [ForeignKey(
        entity = Ingredient::class,
        parentColumns = ["id"],
        childColumns = ["ingredientId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class IngredientImage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @Contextual
    @Serializable(with = UuidSerializer::class)
    val ingredientId: UUID,
    val imagePath: String
)
