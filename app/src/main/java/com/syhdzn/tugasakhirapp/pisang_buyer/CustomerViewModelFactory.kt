package com.syhdzn.tugasakhirapp.pisang_buyer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.syhdzn.tugasakhirapp.pisang_buyer.cart.CartViewModel
import com.syhdzn.tugasakhirapp.pisang_buyer.detail.DetailViewModel
import com.syhdzn.tugasakhirapp.pisang_buyer.payment.CheckoutViewModel

class CustomerViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repository = CustomerRepository(application)

        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {  // Pastikan pengecekan ini benar
            @Suppress("UNCHECKED_CAST")
            return CheckoutViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
