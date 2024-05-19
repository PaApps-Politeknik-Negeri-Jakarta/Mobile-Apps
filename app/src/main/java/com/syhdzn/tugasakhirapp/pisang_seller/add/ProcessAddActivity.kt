package com.syhdzn.tugasakhirapp.pisang_seller.add

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.syhdzn.tugasakhirapp.databinding.ActivityProcessAddBinding
import com.syhdzn.tugasakhirapp.pisang_seller.dashboard.SellerDashboardActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.text.NumberFormat
import java.util.*

class ProcessAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProcessAddBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var tflite: Interpreter

    private val defaultPrice: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        loadModel()
        displayDataAndPredict()
        setupAction()
    }

    private fun displayDataAndPredict() {
        databaseReference.child("weight").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val weight = snapshot.getValue(Float::class.java)
                if (weight != null) {
                    binding.tvWeight.text = "$weight gram"
                    predictPrice(weight)
                } else {
                    binding.tvWeight.text = "0 gram"
                    binding.tvHarga.text = formatPrice(defaultPrice)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.tvWeight.text = "Failed to load data: ${error.message}"
            }
        })
    }

    private fun setupAction() {
        binding.btnBackSeller.setOnClickListener {
            val intent = Intent(this, SellerDashboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadModel() {
        try {
            val modelFileDescriptor = assets.openFd("model_harga_pisang.tflite")
            val inputStream = FileInputStream(modelFileDescriptor.fileDescriptor)
            val fileChannel = inputStream.channel
            val startOffset = modelFileDescriptor.startOffset
            val declaredLength = modelFileDescriptor.declaredLength
            val modelByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
            tflite = Interpreter(modelByteBuffer)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_SHORT).show()
        }
    }

    private fun predictPrice(weight: Float) {
        if (weight == 0f) {
            binding.tvHarga.text = formatPrice(defaultPrice)
        } else {
            try {
                val inputBuffer = ByteBuffer.allocateDirect(4).apply {
                    order(ByteOrder.nativeOrder())
                    putFloat(weight)
                }
                val outputBuffer = ByteBuffer.allocateDirect(4).apply {
                    order(ByteOrder.nativeOrder())
                }

                tflite.run(inputBuffer, outputBuffer)

                outputBuffer.rewind()
                val hargaPrediksi = outputBuffer.float

                binding.tvHarga.text = formatPrice(hargaPrediksi)

                println("Output model: $hargaPrediksi")
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to make prediction", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formatPrice(price: Float): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(price)
    }
}
