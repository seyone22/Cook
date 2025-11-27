package com.seyone22.cook.data.converters

import androidx.room.TypeConverter
import com.seyone22.cook.data.model.MeasureType
import com.seyone22.cook.data.model.TagType
import kotlinx.serialization.json.Json
import recipeimporter.model.RecipeInstruction
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

class RoomConverters {
    @TypeConverter
    fun fromMeasureType(value: MeasureType): String {
        return value.name
    }

    // --- Optional List<String> if you store tags, cuisines, etc ---
    @TypeConverter
    fun fromStringList(value: List<String>?): String? =
        value?.let { json.encodeToString(it) }

    @TypeConverter
    fun toStringList(value: String?): List<String>? =
        value?.let { json.decodeFromString(it) }

    @TypeConverter
    fun toMeasureType(value: String): MeasureType {
        return enumValueOf<MeasureType>(value)
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(dateString: String?): LocalDate? = dateString?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?): String? = dateTime?.toString()

    @TypeConverter
    fun toLocalDateTime(dateTimeString: String?): LocalDateTime? =
        dateTimeString?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun fromInstant(value: Instant?): String? = value?.toString()

    @TypeConverter
    fun toInstant(value: String?): Instant? = value?.let { Instant.parse(it) }


    @TypeConverter
    fun toTagType(value: String?): TagType? = value?.let { TagType.valueOf(it) }

    @TypeConverter
    fun fromTagType(value: TagType?): String? = value?.name

    private val json =
        Json { encodeDefaults = true; ignoreUnknownKeys = true; classDiscriminator = "type" }

    @TypeConverter
    fun fromInstructions(value: RecipeInstruction): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun toInstructions(value: String): RecipeInstruction {
        return json.decodeFromString(value)
    }
}