package com.seyone22.cook.service

import android.util.Log
import com.seyone22.cook.data.model.Ingredient
import com.seyone22.cook.data.model.IngredientProduct
import com.seyone22.cook.data.model.RecipeIngredient
import com.seyone22.cook.data.repository.ingredient.IngredientRepository
import com.seyone22.cook.data.repository.ingredientVariant.IngredientVariantRepository
import com.seyone22.cook.provider.KtorClientProvider.client
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

suspend fun matchCanonicalIngredient(
    parsed: ParsedIngredient,
    client: HttpClient,
    ingredientRepository: IngredientRepository,
    ingredientProductRepository: IngredientVariantRepository
): RecipeIngredient? = withContext(Dispatchers.IO) {
    runCatching {
        Log.d("matchCanonicalIngredient", "Input parsed ingredient: $parsed")

        val response =
            client.get("https://ingredient-database-api.vercel.app/api/ingredients/vector") {
                parameter("query", parsed.ingredient)
                parameter("includeProducts", true)
                parameter("limit", 1)
                accept(ContentType.Application.Json)
            }

        val text = response.bodyAsText()
        Log.d("matchCanonicalIngredient", "API response: $text")

        val root = Json.parseToJsonElement(text).jsonObject
        val results = root["results"]?.jsonArray ?: run {
            Log.d("matchCanonicalIngredient", "No results array")
            return@runCatching null
        }

        if (results.isEmpty()) {
            Log.d("matchCanonicalIngredient", "Results array is empty")
            return@runCatching null
        }

        val bestMatch = results.first().jsonObject

        // canonical fields
        val foodDbId = bestMatch["_id"]?.jsonPrimitive?.contentOrNull
        val canonicalName = bestMatch["name"]?.jsonPrimitive?.contentOrNull

        if (foodDbId.isNullOrBlank() || canonicalName.isNullOrBlank()) {
            Log.d("matchCanonicalIngredient", "Missing id/name in bestMatch: $bestMatch")
            return@runCatching null
        }

        // helper to safely parse string-list fields
        fun jsonArrayToStringList(key: String): List<String> =
            bestMatch[key]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()

        val country = jsonArrayToStringList("country")
        val region = jsonArrayToStringList("region")
        val cuisine = jsonArrayToStringList("cuisine")
        val flavorProfile = jsonArrayToStringList("flavor_profile")
        val dietaryFlags = jsonArrayToStringList("dietary_flags")
        val comment = bestMatch["comment"]?.jsonPrimitive?.contentOrNull ?: ""
        val provenance = bestMatch["provenance"]?.jsonPrimitive?.contentOrNull
        //val image = bestMatch["image"]?.jsonPrimitive?.contentOrNull ?: ""

        // products parsing: choose the product with the lowest non-zero price, fallback to first
        val productsJson = bestMatch["products"]?.jsonArray
        var chosenProduct: JsonObject? = null
        if (productsJson != null && productsJson.isNotEmpty()) {
            val productObjs = productsJson.mapNotNull { it.jsonObject }
            chosenProduct = productObjs.minByOrNull {
                it["price"]?.jsonPrimitive?.doubleOrNull ?: Double.POSITIVE_INFINITY
            } ?: productObjs.firstOrNull()
        }

        // extract product fields safely
        val productPrice = chosenProduct?.get("price")?.jsonPrimitive?.doubleOrNull ?: 0.0
        val productCurrency = chosenProduct?.get("currency")?.jsonPrimitive?.contentOrNull ?: ""
        val productUnit = chosenProduct?.get("unit")?.jsonPrimitive?.contentOrNull ?: ""
        val productQuantity = chosenProduct?.get("quantity")?.jsonPrimitive?.doubleOrNull ?: 0.0
        val productImage = chosenProduct?.get("url")?.jsonPrimitive?.contentOrNull ?: ""
        val productSource = chosenProduct?.get("source")?.jsonPrimitive?.contentOrNull ?: ""
        val productName = chosenProduct?.get("name")?.jsonPrimitive?.contentOrNull ?: ""
        val uniqueId = chosenProduct?.get("_id")?.jsonPrimitive?.contentOrNull ?: ""


        // If the canonical `Ingredient` exists in DB, use it; otherwise create one populated with mapped fields
        val dbIng = ingredientRepository.getIngredientByFoodDbId(foodDbId).firstOrNull()

        val ingredientId = if (dbIng != null) {
            dbIng.id
        } else {
            val newIngredient = Ingredient(
                id = UUID.randomUUID(),
                name = canonicalName,
                // keep the same property names you've used in your model
                country = country,
                region = region,
                cuisine = cuisine,
                flavor_profile = flavorProfile,
                dietary_flags = dietaryFlags,
                comment = comment,
                image = "",
                foodDbId = foodDbId
            )

            if (newIngredient.foodDbId === null) {
                throw Exception("FUUUUUUUUCK")
            }

            val newIngredientProduct = IngredientProduct(
                ingredientId = newIngredient.foodDbId,
                productName = productName,
                source = productSource,
                item_unit = productUnit,
                price = productPrice,
                currency = productCurrency,
                quantity = productQuantity,
                last_fetched = "",
                image = productImage,
                uniqueId = uniqueId
            )
            ingredientRepository.insertIngredient(newIngredient)
            ingredientProductRepository.insertIngredientVariant(newIngredientProduct)
            Log.d(
                "matchCanonicalIngredient",
                "Inserted canonical ingredient: ${newIngredient.name} (${foodDbId})"
            )
            newIngredient.id
        }

        // Create RecipeIngredient linked to the canonical ingredient ID
        val matchedIngredient = RecipeIngredient(
            foodDbId = foodDbId,
            name = canonicalName,
            quantity = parsed.quantity ?: productQuantity.takeIf { it > 0.0 } ?: 0.0,
            unit = parsed.unit ?: productUnit.ifBlank { "pcs" },
            recipeId = UUID.randomUUID(),
            ingredientId = ingredientId,
            notes = parsed.notes
        )

        Log.d("matchCanonicalIngredient", "Matched ingredient: $matchedIngredient")
        matchedIngredient
    }.onFailure { e ->
        Log.e("matchCanonicalIngredient", "Error matching ingredient: ${e.localizedMessage}", e)
    }.getOrNull()
}

suspend fun resolveAndSaveIngredient(
    parsed: ParsedIngredient,
    client: HttpClient,
    ingredientRepository: IngredientRepository,
    ingredientProductRepository: IngredientVariantRepository
): RecipeIngredient {

    // --- 1. Network Match (Attempt to find the "Tag") ---
    val matchResult = try {
        if (parsed.ingredient.isBlank()) null else {
            val response = client.get("https://ingredient-database-api.vercel.app/api/ingredients/vector") {
                parameter("query", parsed.ingredient)
                parameter("includeProducts", true)
                parameter("limit", 1)
                accept(ContentType.Application.Json)
            }
            val root = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            root["results"]?.jsonArray?.firstOrNull()?.jsonObject
        }
    } catch (e: Exception) {
        Log.w("IngredientResolution", "Network match failed: ${e.localizedMessage}")
        null
    }

    // --- 2. Determine Identifiers ---
    val localIngredientId: UUID
    val remoteId: String?

    if (matchResult != null) {
        // --- MATCH FOUND ---
        remoteId = matchResult["_id"]?.jsonPrimitive?.contentOrNull

        // This is the name from the DB (e.g. "Wheat Flour, White")
        val canonicalName = matchResult["name"]?.jsonPrimitive?.contentOrNull ?: parsed.ingredient

        // Check if we already have this canonical item in our library
        val existing = remoteId?.let { ingredientRepository.getIngredientByFoodDbId(it).firstOrNull() }

        if (existing != null) {
            localIngredientId = existing.id
        } else {
            // New Canonical Item: Save it using the DB's official name
            localIngredientId = UUID.randomUUID()
            val newIngredient = Ingredient(
                id = localIngredientId,
                name = canonicalName, // DB Name
                foodDbId = remoteId,
                country = emptyList(), region = emptyList(), cuisine = emptyList(),
                flavor_profile = emptyList(), dietary_flags = emptyList(), comment = "", image = ""
            )
            ingredientRepository.insertIngredient(newIngredient)
        }
    } else {
        // --- NO MATCH (Provisional) ---
        // Create a placeholder. We use the recipe name as the DB name since we have nothing else.
        remoteId = null
        localIngredientId = UUID.randomUUID()

        val provisionalIngredient = Ingredient(
            id = localIngredientId,
            name = parsed.ingredient, // Fallback to recipe name
            foodDbId = null,
            country = emptyList(), region = emptyList(), cuisine = emptyList(),
            flavor_profile = emptyList(), dietary_flags = emptyList(), comment = "", image = ""
        )
        ingredientRepository.insertIngredient(provisionalIngredient)
    }

    // --- 3. Create Recipe Ingredient (PRESERVE PROVISIONAL DATA) ---
    return RecipeIngredient(
        id = 0,
        recipeId = UUID.randomUUID(), // To be set by ViewModel
        ingredientId = localIngredientId, // Link to the DB item (Reference)
        foodDbId = remoteId,              // Link to the DB item (Reference)

        // HERE IS THE FIX: Always use the parsed name/qty/unit for the RecipeIngredient entry
        name = parsed.ingredient,
        quantity = parsed.quantity ?: 0.0,
        unit = parsed.unit ?: "pcs",
        notes = parsed.notes
    )
}

suspend fun getIngredient(id: String, client: HttpClient): Ingredient? {
    return withContext(Dispatchers.IO) {
        try {
            val res = client.get("https://ingredient-database-api.vercel.app/api/ingredients/$id") {
                accept(ContentType.Application.Json)
            }
            val text = res.bodyAsText()
            Json.decodeFromString<Ingredient>(text)
        } catch (e: Exception) {
            Log.e("FoodRepoService", "Error fetching ingredient: ${e.localizedMessage}")
            null
        }
    }
}

@Serializable
data class IngredientPrice(
    val name: String,
    val unit: String,
    val quantity: Double,
    val price: Double,
    val currency: String,
    val url: String?,
    val isAvailable: Boolean = true
)

@Serializable
data class IngredientPriceResponse(
    val ingredient: String, val prices: List<IngredientPrice>, val message: String? = null
)

suspend fun getIngredientPrices(
    id: String, client: HttpClient, country: String = "LK"
): List<IngredientPrice> = withContext(Dispatchers.IO) {
    try {
        val res =
            client.get("https://ingredient-database-api.vercel.app/api/ingredients/$id/price") {
                parameter("country", country)
                accept(ContentType.Application.Json)
            }
        val text = res.bodyAsText()

        Log.d("TAG", "getIngredientPrices: $text")

        val json = Json.parseToJsonElement(text).jsonObject
        val pricesArray = json["prices"]?.jsonArray ?: return@withContext emptyList()
        pricesArray.mapNotNull { item ->
            runCatching { Json.decodeFromJsonElement<IngredientPrice>(item) }.getOrNull()
        }
    } catch (e: Exception) {
        Log.e("FoodRepoService", "Error fetching prices: ${e.localizedMessage}")
        emptyList()
    }
}


suspend fun getProducts(
    client: HttpClient, productIds: List<String>
): List<JsonObject> = withContext(Dispatchers.IO) {
    if (productIds.isEmpty()) return@withContext emptyList<JsonObject>()

    try {
        val res = client.post("https://ingredient-database-api.vercel.app/api/products") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(mapOf("ids" to productIds))
        }

        Log.d("FoodRepoService", "${mapOf("ids" to productIds)}")

        val text = res.bodyAsText()

        val root = Json.parseToJsonElement(text).jsonObject
        val productsArray = root["products"]?.jsonArray ?: return@withContext emptyList()

        productsArray.mapNotNull { it.jsonObject }
    } catch (e: Exception) {
        Log.e("FoodRepoService", "Error fetching products: ${e.localizedMessage}", e)
        emptyList()
    }
}

suspend fun updateIngredient(
    foodDbId: String,
    ingredientRepository: IngredientRepository,
    ingredientProductRepository: IngredientVariantRepository
): Ingredient? = withContext(Dispatchers.IO) {
    runCatching {
        Log.d("updateIngredient", "Updating ingredient: $foodDbId")

        // --- 1️⃣ Fetch canonical ingredient data ---
        val ingredientRes = client.get("https://ingredient-database-api.vercel.app/api/ingredients/$foodDbId?=") {
            accept(ContentType.Application.Json)
            parameter("includeProducts", true)
        }

        val text = ingredientRes.bodyAsText()
        val jsonRoot = Json.parseToJsonElement(text).jsonObject
        val json = jsonRoot["ingredient"]?.jsonObject
        Log.d("updateIngredient", "Fetched ingredient JSON: $json")

        if (json == null) return@runCatching null

        // --- 2️⃣ Parse canonical ingredient fields ---
        val canonicalName = json["name"]?.jsonPrimitive?.contentOrNull
        if (canonicalName == null) {
            Log.w("updateIngredient", "Skipping ingredient $foodDbId — missing name field")
            return@runCatching null
        }

        fun jsonArrayToStringList(key: String): List<String> =
            json[key]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()

        val country = jsonArrayToStringList("country")
        val region = jsonArrayToStringList("region")
        val cuisine = jsonArrayToStringList("cuisine")
        val flavorProfile = jsonArrayToStringList("flavor_profile")
        val dietaryFlags = jsonArrayToStringList("dietary_flags")
        val comment = json["comment"]?.jsonPrimitive?.contentOrNull ?: ""
        val provenance = json["provenance"]?.jsonPrimitive?.contentOrNull

        val image = when (val imageElement = json["image"]) {
            is JsonPrimitive -> imageElement.contentOrNull ?: ""
            is JsonObject -> imageElement["url"]?.jsonPrimitive?.contentOrNull ?: "" // use "url" if exists
            else -> ""
        }

        // --- 3️⃣ Upsert Ingredient in local DB ---
        val existingIngredient = ingredientRepository.getIngredientByFoodDbId(foodDbId).firstOrNull()
        val updatedIngredient = if (existingIngredient != null) {
            existingIngredient.copy(
                name = canonicalName,
                country = country,
                region = region,
                cuisine = cuisine,
                flavor_profile = flavorProfile,
                dietary_flags = dietaryFlags,
                comment = comment,
                image = image,
            ).also {
                ingredientRepository.updateIngredient(it)
                Log.d("updateIngredient", "Updated ingredient: ${it.name}")
            }
        } else {
            Ingredient(
                id = UUID.randomUUID(),
                name = canonicalName,
                country = country,
                region = region,
                cuisine = cuisine,
                flavor_profile = flavorProfile,
                dietary_flags = dietaryFlags,
                comment = comment,
                image = image,
                foodDbId = foodDbId
            ).also {
                ingredientRepository.insertIngredient(it)
                Log.d("updateIngredient", "Inserted new ingredient: ${it.name}")
            }
        }

        val productJson = jsonRoot["products"]

        // --- 4️⃣ Fetch related product data ---
        val productIds = productJson?.jsonArray
            ?.mapNotNull { it.jsonObject["_id"]?.jsonPrimitive?.contentOrNull }
            ?.filter { it.isNotBlank() }
            .orEmpty()

        if (productIds.isNotEmpty()) {
            val products = getProducts(client, productIds)

            // --- 5️⃣ Upsert IngredientProduct entries ---
            for (prod in products) {
                val uniqueId = prod["_id"]?.jsonPrimitive?.contentOrNull ?: continue
                val name = prod["name"]?.jsonPrimitive?.contentOrNull ?: continue
                val price = prod["price"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                val currency = prod["currency"]?.jsonPrimitive?.contentOrNull ?: ""
                val quantity = prod["quantity"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                val unit = prod["unit"]?.jsonPrimitive?.contentOrNull ?: ""
                val source = prod["source"]?.jsonPrimitive?.contentOrNull ?: ""
                val url = prod["url"]?.jsonPrimitive?.contentOrNull
                val imageUrl = prod["image"]?.jsonPrimitive?.contentOrNull ?: ""

                if (updatedIngredient.foodDbId === null) {
                    throw Exception("FUUUUUUUUCK")
                }

                val existingVariant = ingredientProductRepository.getIngredientProductByUniqueId(uniqueId).firstOrNull()
                val productEntity = IngredientProduct(
                    ingredientId = updatedIngredient.foodDbId,
                    productName = name,
                    source = source,
                    item_unit = unit,
                    price = price,
                    currency = currency,
                    quantity = quantity,
                    last_fetched = "",
                    image = imageUrl.ifBlank { url ?: "" },
                    uniqueId = uniqueId
                )

                if (existingVariant != null) {
                    ingredientProductRepository.updateIngredientVariant(productEntity)
                    Log.d("updateIngredient", "Updated product: $name ($uniqueId)")
                } else {
                    ingredientProductRepository.insertIngredientVariant(productEntity)
                    Log.d("updateIngredient", "Inserted new product: $name ($uniqueId)")
                }
            }
        } else {
            Log.d("updateIngredient", "No linked products found for $canonicalName")
        }

        updatedIngredient
    }.onFailure { e ->
        Log.e("updateIngredient", "Failed to update ingredient: ${e.localizedMessage}", e)
    }.getOrNull()
}
