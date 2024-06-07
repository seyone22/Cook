package com.seyone22.cook.data.repository.shoppingList

import com.seyone22.cook.data.model.ShoppingList
import com.seyone22.cook.data.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

class OfflineShoppingListRepository(private val shoppingListDao: ShoppingListDao) :
    ShoppingListRepository {
    override suspend fun insertList(shoppingList: ShoppingList) = shoppingListDao.insertList(shoppingList)
    override suspend fun insertItem(shoppingListItem: ShoppingListItem) = shoppingListDao.insertItem(shoppingListItem)
    override suspend fun updateList(shoppingList: ShoppingList) = shoppingListDao.updateList(shoppingList)
    override suspend fun updateItem(shoppingListItem: ShoppingListItem) = shoppingListDao.updateItem(shoppingListItem)
    override suspend fun deleteList(shoppingList: ShoppingList) = shoppingListDao.deleteList(shoppingList)
    override suspend fun deleteItem(shoppingListItem: ShoppingListItem) = shoppingListDao.deleteItem(shoppingListItem)
    override fun getShoppingListById(id: Long): Flow<ShoppingList?> = shoppingListDao.getShoppingListById(id)
    override fun getItemsForList(listId: Long): Flow<List<ShoppingListItem?>> = shoppingListDao.getItemsForList(listId)
    override fun getAllShoppingLists(): Flow<List<ShoppingList?>> = shoppingListDao.getAllShoppingLists()
}
