package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instructions")
data class Instruction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val stepNumber: Int,
    val description: String
)