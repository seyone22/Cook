package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "instructions")
data class Instruction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @Contextual
    val recipeId: UUID,
    val stepNumber: Int,
    val description: String
)