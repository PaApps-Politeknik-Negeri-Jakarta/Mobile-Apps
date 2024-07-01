package com.syhdzn.tugasakhirapp.pisang_buyer.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityDetailProductBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.CustomerViewModelFactory
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity
import com.syhdzn.tugasakhirapp.pisang_buyer.payment.PaymentActivity
import java.text.NumberFormat
import java.util.Locale

class DetailProductActivity : AppCompatActivity() {
    private var _binding: ActivityDetailProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var viewModel: DetailViewModel

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseRef = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        val idbarang = intent.getStringExtra("ID")
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
                price = price,
                idbarang = idbarang ?: "",
                imageUrl = imgUri ?: "",
                amount = 1
            )
            viewModel.addToCart(cartEntity)
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show()
        }

        binding.buttonOrder.setOnClickListener {
            val cartEntity = CartEntity(
                name = name ?: "",
                price = price,
                idbarang = idbarang ?: "",
                imageUrl = imgUri ?: "",
                amount = 1
            )
            navigateToPayment(cartEntity)
        }
    }

    private fun formatPrice(price: Float): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(price)
    }

    private fun navigateToPayment(cartEntity: CartEntity) {
        val intent = Intent(this, PaymentActivity::class.java)
        intent.putParcelableArrayListExtra("CART_ITEMS", arrayListOf(cartEntity))
        intent.putExtra("USER_ID", getUserIdFromPreferences())
        startActivity(intent)
    }

    private fun getUserIdFromPreferences(): String {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", "") ?: ""
    }
}
