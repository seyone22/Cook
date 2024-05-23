package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversions")
data class UnitConversion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromUnitId: Long,
    val toUnitId: Long,
    val conversionFactor: Double
)