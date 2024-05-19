package com.syhdzn.tugasakhirapp.pisang_buyer.result

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.syhdzn.tugasakhirapp.databinding.ActivityResultBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWindowInsets()

        displayResults()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun displayResults() {
        val label = intent.getStringExtra("label")
        val labelJenis = intent.getStringExtra("jenisPisang")
        val imageUriString = intent.getStringExtra("imageUri")

        binding.tvKualitas.text = "Kualitas pisang terdeteksi: $label"
        binding.tvJenis.text = "Jenis pisang terdeteksi: $labelJenis"

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
