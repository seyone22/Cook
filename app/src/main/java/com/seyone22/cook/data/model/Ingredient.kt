package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameEn: String,
    val nameSi: String,
    val nameTa: String,
    val description: String?
)