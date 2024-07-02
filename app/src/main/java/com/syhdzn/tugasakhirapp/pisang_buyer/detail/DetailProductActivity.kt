package com.syhdzn.tugasakhirapp.pisang_buyer.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.syhdzn.tugasakhirapp.databinding.ActivityDetailProductBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.CustomerViewModelFactory
import com.syhdzn.tugasakhirapp.pisang_buyer.cart.CartFragment
import com.syhdzn.tugasakhirapp.pisang_buyer.chat.ChatActivity
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity
import java.text.NumberFormat
import java.util.Currency

class DetailProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailProductBinding
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var viewModel: DetailViewModel

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        firebaseRef = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        val name = intent.getStringExtra("NAME")
        val price = intent.getDoubleExtra("PRICE", 0.0)
        val quality = intent.getStringExtra("QUALITY")
        val weight = intent.getIntExtra("WEIGHT", 0)
        val imgUri = intent.getStringExtra("IMG")
        val formattedPrice = formatPrice(price.toFloat())

        binding.tvProductNameDetail.text = name
        binding.tvProductPriceDetail.text = formattedPrice
        binding.tvProductQualityDetail.text = quality
        binding.tvProductWeightDetail.text = weight.toString()
        Picasso.get().load(imgUri).into(binding.ivProductImageDetail)

        val factory = CustomerViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory).get(DetailViewModel::class.java)

        binding.buttonCart.setOnClickListener {
            val cartEntity = CartEntity(
                name = name ?: "",
                imageUrl = imgUri ?: "",
                amount = 1
            )
            viewModel.addToCart(cartEntity)
            Toast.makeText(this, "Berhasil menambahkan ke keranjang", Toast.LENGTH_SHORT).show()
        }
        binding.cart.setOnClickListener {
            val intent = Intent(this, CartFragment::class.java)
            startActivity(intent)
        }
        binding.back.setOnClickListener {
            startActivity(Intent(this, BuyerDashboardActivity::class.java))
        }
        binding.chat.setOnClickListener {
            startActivity(Intent(this, ChatActivity::class.java))
        }
    }

    private fun setupWindowInsets() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun formatPrice(price: Float): String {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 0
        format.currency = Currency.getInstance("IDR")
        return format.format(price)
    }
}