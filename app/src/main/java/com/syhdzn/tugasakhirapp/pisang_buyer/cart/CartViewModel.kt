package com.syhdzn.tugasakhirapp.pisang_buyer.cart

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.*
import com.syhdzn.tugasakhirapp.pisang_buyer.CustomerRepository
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity
import kotlinx.coroutines.launch

class CartViewModel(private val repository: CustomerRepository) : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartEntity>>()
    val cartItems: LiveData<List<CartEntity>> get() = _cartItems

    private val firebaseRef = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("product")
    private val itemListeners = mutableMapOf<String, ValueEventListener>()

    init {
        observeCartItems()
    }

    private fun observeCartItems() {
        repository.getAllCartItems().observeForever { items ->
            _cartItems.value = items
            attachFirebaseListeners(items)
        }
    }

    private fun attachFirebaseListeners(items: List<CartEntity>) {
        items.forEach { item ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Log.d("CartViewModel", "Item not found in Firebase: ${item.idbarang}, removing from cart")
                        removeCartItemById(item.id)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CartViewModel", "Failed to check item in Firebase: ${item.idbarang}", error.toException())
                }
            }
            firebaseRef.child(item.idbarang).addValueEventListener(listener)
            itemListeners[item.idbarang] = listener
        }
    }

    fun removeCartItem(cartEntity: CartEntity) {
        viewModelScope.launch {
            repository.removeCartItem(cartEntity)
            removeFirebaseListener(cartEntity.idbarang)
            Log.d("CartViewModel", "Removed item from cart: ${cartEntity.idbarang}")
        }
    }

    private fun removeCartItemById(id: Long) {
        viewModelScope.launch {
            val item = _cartItems.value?.find { it.id == id }
            if (item != null) {
                repository.removeCartItemById(id)
                removeFirebaseListener(item.idbarang)
                Log.d("CartViewModel", "Removed item from cart by id: $id")
            }
        }
    }

    private fun removeFirebaseListener(idbarang: String) {
        val listener = itemListeners[idbarang]
        if (listener != null) {
            firebaseRef.child(idbarang).removeEventListener(listener)
            itemListeners.remove(idbarang)
        }
    }

    fun checkCartItems() {
        Log.d("CartViewModel", "Checking cart items")
        viewModelScope.launch {
            val cartItems = repository.getAllCartItems().value ?: emptyList()
            Log.d("CartViewModel", "Current cart items: $cartItems")
            cartItems.forEach { item ->
                Log.d("CartViewModel", "Checking item: ${item.idbarang}")
                firebaseRef.child(item.idbarang).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists()) {
                            Log.d("CartViewModel", "Item not found in Firebase: ${item.idbarang}, removing from cart")
                            removeCartItemById(item.id)
                        } else {
                            Log.d("CartViewModel", "Item exists in Firebase: ${item.idbarang}")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("CartViewModel", "Failed to check item in Firebase: ${item.idbarang}", error.toException())
                    }
                })
            }
        }
    }
}
