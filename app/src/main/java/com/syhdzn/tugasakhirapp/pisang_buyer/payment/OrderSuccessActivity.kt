package com.syhdzn.tugasakhirapp.pisang_buyer.payment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityOrderSuccessBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class OrderSuccessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val virtualCode = intent.getStringExtra("VIRTUAL_CODE") ?: ""
        val finalTotal = intent.getDoubleExtra("FINAL_TOTAL", 0.0)

        binding.virtualCodeText.text = virtualCode
        binding.finalTotalText.text = formatPrice(finalTotal)

        setupAction()
    }

    private fun setupAction() {

        val virtualCode = intent.getStringExtra("VIRTUAL_CODE") ?: ""

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })

        binding.copyIcon.setOnClickListener {
            copyToClipboard(virtualCode)
        }

        binding.btnBack.setOnClickListener{
            val intent = Intent(this, BuyerDashboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Virtual Code", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Virtual code copied to clipboard", Toast.LENGTH_SHORT).show()
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

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, BuyerDashboardActivity::class.java)
        startActivity(intent)
    }
}
