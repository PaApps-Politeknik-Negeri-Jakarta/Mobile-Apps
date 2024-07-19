package com.syhdzn.tugasakhirapp.pisang_seller.chatSeller

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.pisang_buyer.data.Product
import com.syhdzn.tugasakhirapp.pisang_seller.chatSeller.adapter.ProductAdapterSeller

class ProductListFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductAdapterSeller
    private val products = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerViewProducts)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = ProductAdapterSeller(products) { product ->
            Log.d("ProductListFragment", "Product clicked: ${product.id}")
            val bundle = Bundle().apply {
                putString("productId", product.id)
            }
            val chatRoomListFragment = ChatRoomListFragment().apply {
                arguments = bundle
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.container, chatRoomListFragment)
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        fetchProducts()
    }

    private fun fetchProducts() {
        database.child("product").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                products.clear()
                for (childSnapshot in snapshot.children) {
                    val product = childSnapshot.getValue(Product::class.java)
                    if (product != null) {
                        products.add(product)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}




