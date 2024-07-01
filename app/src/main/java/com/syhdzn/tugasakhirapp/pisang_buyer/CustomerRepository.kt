package com.syhdzn.tugasakhirapp.pisang_buyer

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import com.google.firebase.database.FirebaseDatabase
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartDao
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartDatabase
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity

class CustomerRepository(application: Application) {
    private var cartDao: CartDao

    init {
        val db = CartDatabase.getDatabase(application)
        cartDao = db.cartDAO()
    }

    suspend fun addToCart(cartEntity: CartEntity) {
        cartDao.insert(cartEntity)
    }

    fun getAllCartItems(): LiveData<List<CartEntity>> {
        return cartDao.getAllCartItems()
    }

    suspend fun isItemInCart(idbarang: String): Boolean {
        return cartDao.isItemInCart(idbarang) > 0
    }


    suspend fun removeCartItem(cartEntity: CartEntity) {
        cartDao.delete(cartEntity)
    }

    suspend fun removeCartItemById(id: Long) {
        cartDao.deleteById(id)
        Log.d("CustomerRepository", "Removed item from cart by id: $id")
    }

    suspend fun checkoutItems(
        userId: String,
        cartItems: List<CartEntity>,
        finalTotalPrice: Double,
        virtualCode: String,
        paymentStatus: String,
        paymentMethod: String,
        shippingMethod: String,
        address: String
    ) {
        cartDao.clearCart()
        Log.d("CustomerRepository", "Cleared cart")

        val databaseReference = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("orders").child(userId)
        val orderId = databaseReference.push().key ?: return

        val orderData = cartItems.map {
            mapOf(
                "id" to it.id,
                "name" to it.name,
                "price" to it.price,
                "idbarang" to it.idbarang,
                "imageUrl" to it.imageUrl,
                "amount" to it.amount
            )
        }

        val orderDetails = mapOf(
            "items" to orderData,
            "totalPrice" to finalTotalPrice,
            "virtualCode" to virtualCode,
            "paymentStatus" to paymentStatus,
            "paymentMethod" to paymentMethod,
            "shippingMethod" to shippingMethod,
            "address" to address
        )

        databaseReference.child(orderId).setValue(orderDetails)
        Log.d("CustomerRepository", "Saved order to Firebase with id: $orderId")

        val firebaseRef = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("product")
        cartItems.forEach { item ->
            firebaseRef.child(item.idbarang).removeValue()
            Log.d("CustomerRepository", "Removed item from Firebase: ${item.idbarang}")
        }
    }
}
