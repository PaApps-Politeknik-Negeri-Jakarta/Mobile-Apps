package com.syhdzn.tugasakhirapp.pisang_buyer.cart

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.syhdzn.tugasakhirapp.pisang_buyer.CustomerRepository
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartDatabase
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity

class CartViewModel(application: Application) : AndroidViewModel(application){

    private val repository: CustomerRepository = CustomerRepository(application)

    fun getAllCartItems(): LiveData<List<CartEntity>> = repository.getCartItem()
}


