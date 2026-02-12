package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val category: TagType = TagType.CUISINE
)

enum class TagType { CUISINE, PLANNING, CATEGORY, MEAL, DIETARY, SKILL_LEVEL, TIME, METHODS, SEASONAL, OCCASION, ALLERGIES, HEALTH }