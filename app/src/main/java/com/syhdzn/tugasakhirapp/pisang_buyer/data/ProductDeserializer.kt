package com.syhdzn.tugasakhirapp.pisang_buyer.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.ValueEventListener

class ProductDeserializer(
    private val onProductsLoaded: (List<Product>) -> Unit,
    private val onError: (DatabaseError) -> Unit
) : ValueEventListener {

    override fun onDataChange(snapshot: DataSnapshot) {
        val productList = mutableListOf<Product>()
        if (snapshot.exists()) {
            for (productSnap in snapshot.children) {
                try {
                    val product = productSnap.getValue(Product::class.java)
                    product?.let { productList.add(it) }
                } catch (e: DatabaseException) {
                    Log.e("DatabaseError", "Error converting product", e)
                }
            }
        }
        onProductsLoaded(productList)
    }

    override fun onCancelled(error: DatabaseError) {
        onError(error)
    }
}
