package com.syhdzn.tugasakhirapp.pisang_buyer.history.list_history

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.*
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityHistoryBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity
import com.syhdzn.tugasakhirapp.pisang_buyer.history.Order
import com.syhdzn.tugasakhirapp.pisang_buyer.history.detail_history.OrderDetailActivity

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var userId: String
    private lateinit var ordersRef: DatabaseReference
    private lateinit var historyAdapter: HistoryAdapter
    private var loadingDialog: SweetAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = getUserIdFromPreferences()
        ordersRef = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("orders").child(userId)

        setupRecyclerView()
        setupAction()
        loadOrderHistory()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onItemClick = { orderId -> navigateToOrderDetail(orderId) },
            onCancelClick = { orderId -> showCancelDialog(orderId) }
        )
        binding.recyclerViewHistory.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun setupAction() {

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })

        binding.icBack.setOnClickListener {
            startActivity(Intent(this, BuyerDashboardActivity::class.java).apply {
                putExtra("switchToFragment", "UserFragment")
                putExtra("selectMenuItem", R.id.user)
            })
        }
    }

    private fun loadOrderHistory() {
        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderList = mutableListOf<Order>()
                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(Order::class.java)
                    order?.let {
                        it.id = orderSnapshot.key ?: ""
                        orderList.add(it)
                    }
                }
                orderList.reverse()
                historyAdapter.submitList(orderList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HistoryActivity", "Failed to load orders", error.toException())
            }
        })
    }

    private fun navigateToOrderDetail(orderId: String) {
        Log.d("HistoryActivity", "Navigating to order detail with ID: $orderId")
        val intent = Intent(this, OrderDetailActivity::class.java)
        intent.putExtra("ORDER_ID", orderId)
        startActivity(intent)
    }

    private fun getUserIdFromPreferences(): String {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", "") ?: ""
    }

    private fun showCancelDialog(orderId: String) {
        val customDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_confirm_cancel, null)
        val dialog = AlertDialog.Builder(this)
            .setView(customDialogView)
            .create()

        customDialogView.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            dialog.dismiss()
            showLoading()
            Handler(Looper.getMainLooper()).postDelayed({
                cancelOrder(orderId)
                hideLoading()
                showSuccessDialog("Order successfully cancelled!")
            }, 2000)
        }

        customDialogView.findViewById<Button>(R.id.btn_no).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_3)
        customDialogView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun cancelOrder(orderId: String) {
        ordersRef.child(orderId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {

            } else {
                showErrorDialog("Gagal membatalkan pesanan: ${task.exception?.message}")
            }
        }
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

    private fun showLoading() {
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
    }
}
