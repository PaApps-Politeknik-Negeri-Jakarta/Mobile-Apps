package com.syhdzn.tugasakhirapp.pisang_buyer.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.syhdzn.tugasakhirapp.pisang_buyer.CustomerRepository
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartDatabase
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity
import kotlinx.coroutines.launch

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CustomerRepository = CustomerRepository(application)

    fun addToCart(cartEntity: CartEntity) = viewModelScope.launch {
        repository.addToCart(cartEntity)
    }
}
