package com.syhdzn.tugasakhirapp.pisang_buyer.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityDetailProductBinding
import java.text.NumberFormat
import java.util.Locale

class DetailProductActivity : AppCompatActivity() {
    private var _binding : ActivityDetailProductBinding? = null
    private  val binding get() = _binding!!
    private lateinit var firebaseRef : DatabaseReference

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        firebaseRef = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        val name = intent.getStringExtra("NAME")
        val price = intent.getDoubleExtra("PRICE",0.0)
        val quality = intent.getStringExtra("QUALITY")
        val weight = intent.getIntExtra("WEIGHT",0)
        val imgUri = intent.getStringExtra("IMG")
        val formattedPrice = formatPrice(price.toFloat())

        findViewById<TextView>(R.id.tv_product_name_detail).text = name
        findViewById<TextView>(R.id.tv_product_price_detail).text = formattedPrice
        findViewById<TextView>(R.id.tv_product_quality_detail).text = quality
        findViewById<TextView>(R.id.tv_product_weight_detail).text = weight.toString()
        Picasso.get().load(imgUri).into(findViewById<ImageView>(R.id.iv_product_image_detail))
    }
    private fun formatPrice(price: Float): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(price)
    }
}