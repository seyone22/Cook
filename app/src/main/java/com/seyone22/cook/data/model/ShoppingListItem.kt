package com.seyone22.cook.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
    val ingredientId: Long,
    val quantity: Double,
    val measureId: Long,
    val checked: Boolean = false
)

data class ShoppingListItemDetails(
    val id: Long = 0,
    val shoppingListId: Long = -1,
    val ingredientId: Long = -1,
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