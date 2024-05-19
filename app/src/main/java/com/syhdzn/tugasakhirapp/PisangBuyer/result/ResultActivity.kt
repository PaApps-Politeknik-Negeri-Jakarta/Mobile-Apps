package com.syhdzn.tugasakhirapp.PisangBuyer.result

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.syhdzn.tugasakhirapp.PisangBuyer.dashboard.BuyerDashboardActivity
import com.syhdzn.tugasakhirapp.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Ambil label dan imageUriString dari Intent
        val label = intent.getStringExtra("label")

        val labeljenis = intent.getStringExtra("jenisPisang")

        val imageUriString = intent.getStringExtra("imageUri")

        // Tampilkan label di UI
        binding.tvKualitas.text = "Kualitas pisang terdeteksi: $label"
        binding.tvJenis.text = "Jenis pisang terdeteksi: $labeljenis"

        // Tampilkan gambar di ImageView
        if (!imageUriString.isNullOrEmpty()) {
            binding.ivItemResult.setImageURI(Uri.parse(imageUriString))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, BuyerDashboardActivity::class.java)
        startActivity(intent)
    }
}
