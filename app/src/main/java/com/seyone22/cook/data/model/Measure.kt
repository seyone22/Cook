package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measures")
data class Measure(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val abbreviation: String
)
