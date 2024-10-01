package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.seyone22.cook.helper.UuidSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(
    tableName = "instructions",
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["id"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Instruction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @Contextual
    @Serializable(with = UuidSerializer::class)
    val recipeId: UUID,
    val stepNumber: Int,
    val description: String
)