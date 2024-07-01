package com.syhdzn.tugasakhirapp.pisang_buyer.payment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityPaymentBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.CustomerViewModelFactory
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity
import java.text.NumberFormat
import java.util.Locale

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var checkoutViewModel: CheckoutViewModel
    private lateinit var cartItems: List<CartEntity>
    private lateinit var userId: String
    private lateinit var virtualCode: String
    private var loadingDialog: SweetAlertDialog? = null

    private var adminFee = 1000.0
    private var shippingFee = 0.0
    private var tax = 0.0
    private var finalTotal = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()

        cartItems = intent.getParcelableArrayListExtra("CART_ITEMS") ?: listOf()
        userId = intent.getStringExtra("USER_ID") ?: getUserIdFromPreferences()

        tax = calculateItemsTotal() * 0.1

        binding.totalPrice.text = formatPrice(calculateItemsTotal())
        binding.adminFee.text = formatPrice(adminFee)
        binding.tax.text = formatPrice(tax)

        setupShippingMethodSpinner()
        setupPaymentMethodSpinner()

        binding.buttonConfirmPayment.setOnClickListener {
            setupLoading()
            Handler(Looper.getMainLooper()).postDelayed({
                processPayment(adminFee, shippingFee, tax, finalTotal)
                hideLoading()
            }, 3000)
        }

        binding.btnBack.setOnClickListener{
            startActivity(Intent(this, BuyerDashboardActivity::class.java).apply {
                putExtra("switchToFragment", "CartFragment")
                putExtra("selectMenuItem", R.id.cart)
            })
        }

        updateFinalTotal()
    }

    private fun setupViewModel() {
        val factory = CustomerViewModelFactory(application)
        checkoutViewModel = ViewModelProvider(this, factory).get(CheckoutViewModel::class.java)
    }

    private fun setupShippingMethodSpinner() {
        val shippingMethods = arrayOf("Instan", "Ambil Sendiri")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, shippingMethods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.shippingMethodSpinner.adapter = adapter

        binding.shippingMethodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                shippingFee = if (position == 0) {
                    binding.addressLabel.visibility = View.VISIBLE
                    binding.editTextAddress.visibility = View.VISIBLE
                    15000.0
                } else {
                    binding.addressLabel.visibility = View.GONE
                    binding.editTextAddress.visibility = View.GONE
                    0.0
                }
                binding.shippingFee.text = formatPrice(shippingFee)
                updateFinalTotal()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupPaymentMethodSpinner() {
        val paymentMethods = arrayOf("Mandiri", "BCA", "BRI")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentMethods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.paymentMethodSpinner.adapter = adapter
    }

    private fun updateFinalTotal() {
        finalTotal = calculateTotalAmount(adminFee, shippingFee, tax)
        binding.finalTotalPrice.text = formatPrice(finalTotal)
    }

    private fun processPayment(adminFee: Double, shippingFee: Double, tax: Double, finalTotal: Double) {
        val selectedShippingMethod = binding.shippingMethodSpinner.selectedItem.toString()
        val address = if (selectedShippingMethod == "Instan") binding.editTextAddress.text.toString() else ""

        if (selectedShippingMethod == "Instan" && address.isEmpty()) {
            showErrorDialog("Shipping address must be filled in")
            return
        }

        virtualCode = generateVirtualCode()
        val paymentStatus = "Pending"
        val selectedPaymentMethod = binding.paymentMethodSpinner.selectedItem.toString()

        checkoutViewModel.checkoutItems(userId, cartItems, finalTotal, virtualCode, paymentStatus, selectedPaymentMethod, selectedShippingMethod, address)
        navigateToOrderSuccess(finalTotal)
    }

    private fun navigateToOrderSuccess(finalTotal: Double) {
        val intent = Intent(this, OrderSuccessActivity::class.java)
        intent.putExtra("VIRTUAL_CODE", virtualCode)
        intent.putExtra("FINAL_TOTAL", finalTotal)
        startActivity(intent)
    }

    private fun calculateTotalAmount(adminFee: Double, shippingFee: Double, tax: Double): Double {
        val itemsTotal = calculateItemsTotal()
        return itemsTotal + adminFee + shippingFee + tax
    }

    private fun calculateItemsTotal(): Double {
        return cartItems.sumOf { it.price }
    }

    private fun formatPrice(price: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(price)
    }

    private fun getUserIdFromPreferences(): String {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", "") ?: ""
    }

    private fun generateVirtualCode(): String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val randomPart = (1..6)
            .map { charset.random() }
            .joinToString("")
        return "PSG-$randomPart-JARA"
    }

    private fun setupLoading() {
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
