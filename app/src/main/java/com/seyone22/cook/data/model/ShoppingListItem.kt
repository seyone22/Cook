package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.seyone22.cook.helper.UuidSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(
    tableName = "shopping_list_items",
    foreignKeys = [ForeignKey(
        entity = ShoppingList::class,
        parentColumns = ["id"],
        childColumns = ["shoppingListId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ShoppingListItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shoppingListId: Long,
    @Serializable(with = UuidSerializer::class)
    @Contextual
    val ingredientId: UUID,
    val quantity: Double,
    val measureId: Long,
    val checked: Boolean = false
)

data class ShoppingListItemDetails(
    val id: Long = 0,
    val shoppingListId: Long = -1,
    val ingredientId: UUID = UUID.randomUUID(),
    val quantity: String = "",
    val measureId: Long = -1,
    val checked: Boolean = false
)

fun ShoppingListItemDetails.toShoppingList(): ShoppingListItem = ShoppingListItem(
    id = id,
    shoppingListId = shoppingListId,
    ingredientId = ingredientId,
    quantity = quantity.toDouble(),
    measureId = measureId,
    checked = checked
)

fun ShoppingListItem.toShoppingListItemDetails(): ShoppingListItemDetails = ShoppingListItemDetails(
    id = id,
    shoppingListId = shoppingListId,
    ingredientId = ingredientId,
    quantity = quantity.toString(),
    measureId = measureId,
    checked = checked
)