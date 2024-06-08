package com.seyone22.cook.data.repository.shoppingList

import com.seyone22.cook.data.model.ShoppingList
import com.seyone22.cook.data.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    suspend fun insertList(shoppingList: ShoppingList)
    suspend fun insertItem(shoppingListItem: ShoppingListItem)
    suspend fun updateList(shoppingList: ShoppingList)
    suspend fun updateItem(shoppingListItem: ShoppingListItem)
    suspend fun deleteList(shoppingList: ShoppingList)
    suspend fun deleteItem(shoppingListItem: ShoppingListItem)
    fun getShoppingListById(id: Long): Flow<ShoppingList?>
    fun getItemsForList(listId: Long): Flow<List<ShoppingListItem?>>
    fun getAllItems(): Flow<List<ShoppingListItem?>>

    fun getAllShoppingLists(): Flow<List<ShoppingList?>>
}
