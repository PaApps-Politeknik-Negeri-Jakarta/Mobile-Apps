package com.syhdzn.tugasakhirapp.pisang_buyer.history.detail_history

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.syhdzn.tugasakhirapp.databinding.ActivityOrderDetailBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.history.Order
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class OrderDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var orderId: String
    private lateinit var userId: String
    private lateinit var orderRef: DatabaseReference
    private lateinit var orderDetailAdapter: OrderDetailAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = getUserIdFromPreferences()
        orderId = intent.getStringExtra("ORDER_ID") ?: ""

        Log.d("OrderDetailActivity", "User ID: $userId, Order ID: $orderId")

        orderRef = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("orders").child(userId).child(orderId)

        setupRecyclerView()
        loadOrderDetails()
        setupAction()
    }

    private fun setupRecyclerView() {
        orderDetailAdapter = OrderDetailAdapter()
        binding.recyclerViewOrderDetail.apply {
            layoutManager = LinearLayoutManager(this@OrderDetailActivity)
            adapter = orderDetailAdapter
        }
    }

    private fun setupAction() {

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })


        binding.icBack.setOnClickListener {
            finish()
        }
    }

    private fun loadOrderDetails() {
        orderRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Order::class.java)
                Log.d("OrderDetailActivity", "Order data: $order")
                order?.let {
                    orderDetailAdapter.submitList(it.items)
                    binding.totalPrice.text = formatPrice(it.totalPrice)
                    binding.virtualCode.text = it.virtualCode
                    binding.paymentStatus.text = it.paymentStatus
                    binding.shippingMethodText.text = it.shippingMethod
                    if (it.shippingMethod == "Ambil Sendiri") {
                        binding.addressText.visibility = View.GONE
                        binding.ttlAddres.visibility = View.GONE
                        binding.strpAddress.visibility = View.GONE
                    } else {
                        binding.addressText.visibility = View.VISIBLE
                        binding.ttlAddres.visibility = View.VISIBLE
                        binding.strpAddress.visibility = View.VISIBLE
                        binding.addressText.text = it.address
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderDetailActivity", "Failed to load order details", error.toException())
            }
        })
    }

    private fun formatPrice(price: Double): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val decimalFormatSymbols = (numberFormat as DecimalFormat).decimalFormatSymbols
        decimalFormatSymbols.currencySymbol = "Rp"
        numberFormat.decimalFormatSymbols = decimalFormatSymbols
        numberFormat.maximumFractionDigits = 0
        numberFormat.minimumFractionDigits = 0
        return numberFormat.format(price)
    }

    private fun getUserIdFromPreferences(): String {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", "") ?: ""
    }
}
