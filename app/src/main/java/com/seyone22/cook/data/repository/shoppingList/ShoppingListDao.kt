package com.seyone22.cook.data.repository.shoppingList

import androidx.room.*
import com.seyone22.cook.data.model.ShoppingList
import com.seyone22.cook.data.model.ShoppingListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(shoppingList: ShoppingList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(shoppingListItem: ShoppingListItem)

    @Update
    suspend fun updateList(shoppingList: ShoppingList)

    @Update
    suspend fun updateItem(shoppingListItem: ShoppingListItem)

    @Delete
    suspend fun deleteList(shoppingList: ShoppingList)

    @Delete
    suspend fun deleteItem(shoppingListItem: ShoppingListItem)

    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun getShoppingListById(id: Long): Flow<ShoppingList>

    @Query("SELECT * FROM shopping_list_items WHERE shoppingListId = :listId")
    fun getItemsForList(listId: Long): Flow<List<ShoppingListItem>>

    @Query("SELECT * FROM shopping_list_items")
    fun getAllItems(): Flow<List<ShoppingListItem>>

    @Query("SELECT * FROM shopping_lists")
    fun getAllShoppingLists(): Flow<List<ShoppingList>>
}
