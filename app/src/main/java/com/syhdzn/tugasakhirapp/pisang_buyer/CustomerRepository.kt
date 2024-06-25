package com.syhdzn.tugasakhirapp.pisang_buyer

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartDao
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartDatabase
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity
import kotlinx.coroutines.flow.Flow

class CustomerRepository(application: Application) {
    private var cartDao:CartDao

    init {
        val db = CartDatabase.getInstance(application)
        cartDao = db.cartDAO()
    }

    fun getCartItem(): LiveData<List<CartEntity>> = cartDao.getAllCartItems()

    suspend fun addToCart(cartEntity: CartEntity) {
        cartDao.addToCart(cartEntity)
    }

}


