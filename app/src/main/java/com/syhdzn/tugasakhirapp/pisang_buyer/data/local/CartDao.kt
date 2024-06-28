package com.syhdzn.tugasakhirapp.pisang_buyer.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cart: CartEntity)

    @Query("SELECT * FROM cart_table")
    fun getAllCartItems(): LiveData<List<CartEntity>>
}
