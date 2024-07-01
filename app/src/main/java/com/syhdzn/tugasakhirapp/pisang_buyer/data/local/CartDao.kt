package com.syhdzn.tugasakhirapp.pisang_buyer.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartEntity: CartEntity)

    @Query("SELECT * FROM cart_table")
    fun getAllCartItems(): LiveData<List<CartEntity>>

    @Delete
    suspend fun delete(cartEntity: CartEntity)

    @Query("SELECT COUNT(*) FROM cart_table WHERE id_pisang = :idbarang")
    suspend fun isItemInCart(idbarang: String): Int

    @Query("DELETE FROM cart_table")
    suspend fun clearCart()

    @Query("DELETE FROM cart_table WHERE cartId = :id")
    suspend fun deleteById(id: Long)
}
