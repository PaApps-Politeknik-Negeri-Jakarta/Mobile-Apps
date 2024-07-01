package com.syhdzn.tugasakhirapp.pisang_buyer.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syhdzn.tugasakhirapp.pisang_buyer.CustomerRepository
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity
import kotlinx.coroutines.launch

class DetailViewModel(private val repository: CustomerRepository) : ViewModel() {

    fun addToCart(cartEntity: CartEntity) {
        viewModelScope.launch {
            val isInCart = repository.isItemInCart(cartEntity.idbarang)
            if (!isInCart) {
                repository.addToCart(cartEntity)
            }
        }
    }
}
