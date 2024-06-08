package com.seyone22.cook.helper

import androidx.room.TypeConverter
import com.seyone22.cook.data.model.MeasureType

class Converters {
    @TypeConverter
    fun fromMeasureType(value: MeasureType): String {
        return value.name
    }

    @TypeConverter
    fun toMeasureType(value: String): MeasureType {
        return enumValueOf<MeasureType>(value)
    }
}