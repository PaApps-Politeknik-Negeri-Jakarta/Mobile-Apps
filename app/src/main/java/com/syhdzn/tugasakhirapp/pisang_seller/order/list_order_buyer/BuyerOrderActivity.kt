package com.syhdzn.tugasakhirapp.pisang_seller.order.list_order_buyer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityBuyerOrderBinding
import com.syhdzn.tugasakhirapp.pisang_seller.dashboard.SellerDashboardActivity
import com.syhdzn.tugasakhirapp.pisang_seller.order.Order

class BuyerOrderActivity : AppCompatActivity() {

    private lateinit var mDatabase: DatabaseReference
    private lateinit var binding: ActivityBuyerOrderBinding
    private lateinit var orderDetailAdapter: BuyerOrderAdapter
    private val orderList = mutableListOf<Order>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyerOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mDatabase = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })

        setupRecyclerView()
        loadOrders()
        setupAction()
    }

    private fun setupRecyclerView() {
        val userId = intent.getStringExtra("USER_ID") ?: return
        orderDetailAdapter = BuyerOrderAdapter(orderList, userId)
        binding.recyclerViewOrder.apply {
            layoutManager = LinearLayoutManager(this@BuyerOrderActivity)
            adapter = orderDetailAdapter
        }
    }

    private fun setupAction() {
        binding.icBack.setOnClickListener{
            startActivity(Intent(this, SellerDashboardActivity::class.java).apply {
                putExtra("switchToFragment", "OrderFragment")
                putExtra("selectMenuItem", R.id.order)
            })
        }
    }

    private fun loadOrders() {
        val userId = intent.getStringExtra("USER_ID") ?: return
        val ordersRef = mDatabase.child("orders").child(userId)
        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                for (orderSnapshot in snapshot.children) {
                    val orderId = orderSnapshot.key ?: ""
                    val virtualCode = orderSnapshot.child("virtualCode").getValue(String::class.java) ?: ""
                    val totalPrice = orderSnapshot.child("totalPrice").getValue(Double::class.java) ?: 0.0
                    val paymentStatus = orderSnapshot.child("paymentStatus").getValue(String::class.java) ?: ""
                    val order = Order(orderId, virtualCode, totalPrice, paymentStatus)
                    orderList.add(order)
                }
                orderList.reverse()
                orderDetailAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("BuyerOrderActivity", "Database error: ${error.message}")
            }
        })
    }
}
