package com.syhdzn.tugasakhirapp.pisang_buyer.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syhdzn.tugasakhirapp.pisang_buyer.CustomerRepository
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity
import kotlinx.coroutines.launch

class CheckoutViewModel(private val repository: CustomerRepository) : ViewModel() {

    fun checkoutItems(
        userId: String,
        cartItems: List<CartEntity>,
        finalTotalPrice: Double,
        virtualCode: String,
        paymentStatus: String,
        paymentMethod: String,
        shippingMethod: String,
        address: String
    ) {
        viewModelScope.launch {
            repository.checkoutItems(userId, cartItems, finalTotalPrice, virtualCode, paymentStatus, paymentMethod, shippingMethod, address)
        }
    }
}
