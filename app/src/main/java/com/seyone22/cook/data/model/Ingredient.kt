package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.seyone22.cook.helper.UuidSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

// Matches with the DB ingredient
@Serializable
@Entity(tableName = "ingredients")
data class Ingredient(
    @Serializable(with = UuidSerializer::class) @Contextual @PrimaryKey val id: UUID = UUID.randomUUID(),

    val foodDbId: String? = null,

    val name: String = "",
    val country: List<String> = listOf<String>(),
    val region: List<String> = listOf<String>(),
    val cuisine: List<String> = listOf<String>(),
    val flavor_profile: List<String> = listOf<String>(),
    val dietary_flags: List<String> = listOf<String>(),
    val comment: String = "",
    val image: String = "",
    val category: String = "",

    val price: Double = 0.0,
    val currency: String = "",
    val product_unit: String = "",
    val unit_quantity: Double = 0.0,
)

data class IngredientDetails(
    val id: UUID = UUID.randomUUID(),
    val foodDbId: String? = null,
    val name: String = "",
    val country: List<String> = listOf<String>(),
    val region: List<String> = listOf<String>(),
    val cuisine: List<String> = listOf<String>(),
    val flavor_profile: List<String> = listOf<String>(),
    val dietary_flags: List<String> = listOf<String>(),
    val comment: String = "",
    val image: String = "",
    val category: String = ""
)

fun IngredientDetails.toIngredient(): Ingredient = Ingredient(
    id = id,
    foodDbId = foodDbId,
    name = name,
    country = country,
    region = region,
    cuisine = cuisine,
    flavor_profile = flavor_profile,
    dietary_flags = dietary_flags,
    comment = comment,
    image = image,
    category = category
)

fun Ingredient.toIngredientDetails(): IngredientDetails = IngredientDetails(
    id = id,
    foodDbId = foodDbId,
    name = name,
    country = country,
    region = region,
    cuisine = cuisine,
    flavor_profile = flavor_profile,
    dietary_flags = dietary_flags,
    comment = comment,
    image = image,
    category = category
)
