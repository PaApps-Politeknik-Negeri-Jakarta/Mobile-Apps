package com.syhdzn.tugasakhirapp.pisang_seller.order.detail_order_buyer

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityBuyerDetailBinding
import com.syhdzn.tugasakhirapp.pisang_seller.order.OrderItem
import com.syhdzn.tugasakhirapp.pisang_seller.order.list_order_buyer.BuyerOrderActivity
import java.text.NumberFormat
import java.util.Locale

class BuyerDetailActivity : AppCompatActivity() {

    private lateinit var mDatabase: DatabaseReference
    private lateinit var binding: ActivityBuyerDetailBinding
    private lateinit var orderItemAdapter: OrderItemAdapter
    private val itemList = mutableListOf<OrderItem>()
    private lateinit var userId: String
    private lateinit var orderId: String
    private var loadingDialog: SweetAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyerDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Custom back press action
                startActivity(Intent(this@BuyerDetailActivity, BuyerOrderActivity::class.java))
                finish()
            }
        })

        mDatabase = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        userId = intent.getStringExtra("USER_ID") ?: return
        orderId = intent.getStringExtra("ORDER_ID") ?: return

        setupRecyclerView()
        setupAction()
        loadOrderDetails()
    }

    private fun setupRecyclerView() {
        orderItemAdapter = OrderItemAdapter(itemList)
        binding.recyclerViewOrderDetail.apply {
            layoutManager = LinearLayoutManager(this@BuyerDetailActivity)
            adapter = orderItemAdapter
        }
    }

    private fun setupAction() {
        binding.icBack.setOnClickListener {
            finish()
        }

        binding.btnUpdate.setOnClickListener {
            showDialogUpdateStatus()
        }
    }

    private fun showDialogUpdateStatus() {
        val customDialogView = layoutInflater.inflate(R.layout.costum_dialog_update_status, null)
        val dialog = AlertDialog.Builder(this)
            .setView(customDialogView)
            .create()

        val spinnerDialog: Spinner = customDialogView.findViewById(R.id.spinnerDialogPaymentStatus)
        ArrayAdapter.createFromResource(
            this,
            R.array.payment_status_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerDialog.adapter = adapter
        }

        customDialogView.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            val selectedStatus = spinnerDialog.selectedItem.toString()
            showLoadingDialog()
            updatePaymentStatus(selectedStatus)
            dialog.dismiss()
        }

        customDialogView.findViewById<Button>(R.id.btn_no).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_3)
        customDialogView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun updatePaymentStatus(status: String) {
        mDatabase.child("orders").child(userId).child(orderId).child("paymentStatus").setValue(status)
            .addOnSuccessListener {
                Handler(Looper.getMainLooper()).postDelayed({
                    hideLoading()
                    showSuccessDialog("Payment status updated successfully!")
                    binding.paymentStatus.text = status
                }, 2000)
            }
            .addOnFailureListener { e ->
                Handler(Looper.getMainLooper()).postDelayed({
                    hideLoading()
                    showErrorDialog("Failed to update payment status: ${e.message}")
                }, 2000)
            }
    }

    private fun loadOrderDetails() {
        val ordersRef = mDatabase.child("orders").child(userId).child(orderId)

        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()

                val virtualCode = snapshot.child("virtualCode").getValue(String::class.java) ?: ""
                val totalPrice = snapshot.child("totalPrice").getValue(Double::class.java) ?: 0.0
                val paymentStatus = snapshot.child("paymentStatus").getValue(String::class.java) ?: ""
                val shippingMethod = snapshot.child("shippingMethod").getValue(String::class.java) ?: ""
                val address = snapshot.child("address").getValue(String::class.java) ?: ""

                binding.virtualCode.text = virtualCode
                binding.totalPrice.text = formatRupiah(totalPrice)
                binding.paymentStatus.text = paymentStatus
                binding.shippingMethodText.text = shippingMethod
                if (shippingMethod == "Ambil Sendiri") {
                    binding.addressText.visibility = View.GONE
                    binding.ttlAddres.visibility = View.GONE
                    binding.strpAddress.visibility = View.GONE
                } else {
                    binding.addressText.visibility = View.VISIBLE
                    binding.ttlAddres.visibility = View.VISIBLE
                    binding.strpAddress.visibility = View.VISIBLE
                    binding.addressText.text = address
                }

                val itemsSnapshot = snapshot.child("items")
                for (itemSnapshot in itemsSnapshot.children) {
                    val amount = itemSnapshot.child("amount").getValue(Int::class.java) ?: 0
                    val name = itemSnapshot.child("name").getValue(String::class.java) ?: ""
                    val price = itemSnapshot.child("price").getValue(Double::class.java) ?: 0.0
                    val imageUrl = itemSnapshot.child("imageUrl").getValue(String::class.java) ?: ""

                    val orderItem = OrderItem(amount, name, price, imageUrl)
                    itemList.add(orderItem)
                }

                orderItemAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("BuyerDetailActivity", "Database error: ${error.message}")
            }
        })
    }

    private fun formatRupiah(value: Double): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        return format.format(value)
    }

    private fun showErrorDialog(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setConfirmClickListener {
            dialog.dismiss()
            hideLoading()
        }
        dialog.show()
    }

    private fun showLoadingDialog() {
        loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
            progressHelper.barColor = Color.parseColor("#06283D")
            titleText = "Loading"
            setCancelable(false)
            show()
        }
    }

    private fun hideLoading() {
        loadingDialog?.hide()
        loadingDialog = null
    }

    private fun showSuccessDialog(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setConfirmClickListener {
            dialog.dismiss()
            hideLoading()
        }
        dialog.show()
        dialog.hideConfirmButton()
    }
}
