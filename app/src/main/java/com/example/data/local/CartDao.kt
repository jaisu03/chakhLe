package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY dishName ASC")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE dishId = :dishId")
    suspend fun updateQuantity(dishId: String, quantity: Int)

    @Query("DELETE FROM cart_items WHERE dishId = :dishId")
    suspend fun deleteItem(dishId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}
