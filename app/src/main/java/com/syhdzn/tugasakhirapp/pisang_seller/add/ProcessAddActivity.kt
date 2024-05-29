package com.syhdzn.tugasakhirapp.pisang_seller.add

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityProcessAddBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity
import com.syhdzn.tugasakhirapp.pisang_seller.camera.CameraSellerActivity
import com.syhdzn.tugasakhirapp.pisang_seller.dashboard.SellerDashboardActivity
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.text.NumberFormat
import java.util.Locale

class ProcessAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProcessAddBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var tflite: Interpreter
    private lateinit var interpreter: Interpreter
    private lateinit var interpreterJenisPisang: Interpreter
    private var imageFile: File? = null
    private val defaultPrice: Float = 0f
    private var loadingDialog: SweetAlertDialog? = null
    private var isPredictionDone = false
    private var totalWeight: Float = 0f
    private var measurementCount: Int = 0
    private var averageWeight: Float = 0f

    companion object {
        private const val img_width = 150
        private const val img_height = 150
        private const val NUM_CLASSES = 3
        private const val NUM_CLASSES_JENIS_PISANG = 6
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        interpreter = Interpreter(loadModelFile())
        interpreterJenisPisang = Interpreter(loadModelFileJenisPisang())

        loadModel()
        setupAction()
        handleImageProses()
        displayDataAndPredict()
    }

    private fun setupAction() {
        binding.btnBackSeller.setOnClickListener {
            val intent = Intent(this, SellerDashboardActivity::class.java)
            startActivity(intent)
        }

        binding.ivBgCamera.setOnClickListener {
            val intent = Intent(this, CameraSellerActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnPrediksi.setOnClickListener {
            handlePricePrediction()
        }

        binding.btnAdd.setOnClickListener {
            handleAddProduct()
        }
    }

    private fun uploadDataToFirebase(namaPisang: String, kualitas: String, berat: Float, harga: Float, imageUrl: String) {

        val databaseReference = this.databaseReference.child("product")
        val dataId = databaseReference.push().key ?: ""

        val data = hashMapOf<String, Any>(
            "nama_pisang" to namaPisang,
            "kualitas" to kualitas,
            "berat" to berat,
            "harga" to harga,
            "image_url" to imageUrl
        )

        databaseReference.child(dataId).setValue(data)
            .addOnSuccessListener {
                hideLoading()
                showSuccessDialogBuyer("Product berhasil ditambahkan")
            }
            .addOnFailureListener { e ->
                hideLoading()
                showErrorDialog("Gagal mengirim data ke Firebase: ${e.message}")
            }
    }



    private fun processImageAndUpload() {
        val drawable = binding.ivItemProcess.drawable
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
            val namaPisang = binding.tvNamaPisang.text.toString()
            val kualitas = binding.tvKualitaPisang.text.toString()
            val weightText = binding.tvWeight.text.toString()
            val berat = weightText.replace(" gram", "").toFloatOrNull() ?: 0f
            val hargaText = binding.tvHarga.text.toString()
            val harga = parsePrice(hargaText)

            val hargaDouble = harga / 100

            uploadImageToFirebaseStorage(bitmap, namaPisang, kualitas, berat, hargaDouble)
        }
    }

    private fun parsePrice(priceText: String): Float {
        val cleanedPriceText = priceText.replace("Rp", "").replace(".", "").replace(",", "")
        return cleanedPriceText.toFloatOrNull() ?: 0f
    }



    private fun uploadImageToFirebaseStorage(bitmap: Bitmap, namaPisang: String, kualitas: String, berat: Float, harga: Float) {
        setupLoading()
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        val storageReference = FirebaseStorage.getInstance().reference.child("images")

        val imageFileName = "image_${System.currentTimeMillis()}.jpg"
        val imageReference = storageReference.child(imageFileName)

        val uploadTask = imageReference.putBytes(imageData)
        uploadTask.addOnSuccessListener { taskSnapshot ->

            imageReference.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()

                uploadDataToFirebase(namaPisang, kualitas, berat, harga, imageUrl)
            }.addOnFailureListener { e ->

                Toast.makeText(this, "Gagal mendapatkan URL gambar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->

            Toast.makeText(this, "Gagal unggah gambar ke Firebase Storage: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun handleImageProses() {
        val imageUriString = intent.getStringExtra("imageUri")
        val isBackCamera = intent.getBooleanExtra("isBackCamera", false)
        val pictureFile = intent.getSerializableExtra("picture") as? File

        imageFile = when {
            imageUriString != null -> {
                val imageUri = Uri.parse(imageUriString)
                handleGalleryImageProses(imageUri)
                null
            }
            isBackCamera && pictureFile != null -> {
                val rotatedBitmap = BitmapFactory.decodeFile(pictureFile.absolutePath)
                processImage(rotatedBitmap)
                val jenisPisang = detectJenisPisang(rotatedBitmap)
                binding.tvNamaPisang.text = "$jenisPisang"
                binding.ivItemProcess.setImageBitmap(rotatedBitmap)
                pictureFile
            }
            !isBackCamera && pictureFile != null -> {
                val bitmap = BitmapFactory.decodeFile(pictureFile.absolutePath)
                processImage(bitmap)
                val jenisPisang = detectJenisPisang(bitmap)
                binding.tvNamaPisang.text = "$jenisPisang"
                binding.ivItemProcess.setImageBitmap(bitmap)
                pictureFile
            }
            else -> null
        }
    }

    private fun handleGalleryImageProses(imageUri: Uri) {
        binding.ivItemProcess.setImageURI(imageUri)
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        processImage(bitmap)
        val jenisPisang = detectJenisPisang(bitmap)
        binding.tvNamaPisang.text = "$jenisPisang"
    }

    private fun displayDataAndPredict() {
        databaseReference.child("weight").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val weight = snapshot.getValue(Float::class.java)
                if (weight != null) {
                    totalWeight += weight
                    measurementCount++
                    averageWeight = totalWeight / measurementCount
                    val weightInt = weight.toInt()
                    binding.tvWeight.text = "$weightInt gram"
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

    private fun handlePricePrediction() {
        if (isPredictionDone) {
            showDialogReplace()
            return
        }

        val weightText = binding.tvWeight.text.toString()
        val weight = weightText.replace(" gram", "").toFloatOrNull() ?: 0f
        val quality = binding.tvKualitaPisang.text.toString()

        if (quality.isEmpty() || quality == "Unknown") {
            showErrorDialog("Harap deteksi kualitas pisang terlebih dahulu.")
        } else if (weight == 0f) {
            showErrorDialog("Harap masukkan berat pisang terlebih dahulu.")
        } else {
            ProcessDataAndPredict()
        }
    }

    private fun handleAddProduct() {
        val hargaText = binding.tvHarga.text.toString()
        val harga = hargaText.replace("Rp", "").replace(".", "").replace(",", ".").toFloatOrNull() ?: 0f
        val quality = binding.tvKualitaPisang.text.toString()

        if (quality.isEmpty() || quality == "Unknown") {
            showErrorDialog("Harap deteksi kualitas pisang terlebih dahulu.")
        } else if (harga == 0f) {
            showErrorDialog("Harap masukkan harga pisang terlebih dahulu.")
        } else {
            processImageAndUpload()
        }
    }

    private fun ProcessDataAndPredict() {
        calculateAverageWeightAndPredict()
    }

    private fun calculateAverageWeightAndPredict() {
        setupLoading()
        databaseReference.child("weight").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val weight = averageWeight
                if (weight != 0f) {
                    binding.tvWeightProses.text = "$weight"
                    val quality = binding.tvKualitaPisang.text.toString()
                    predictPrice(weight, quality)
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

    private fun predictPrice(weight: Float, quality: String) {
        if (weight == 0f) {
            binding.tvHarga.text = formatPrice(defaultPrice)
        } else {
            try {
                val inputBuffer = ByteBuffer.allocateDirect(4).apply {
                    order(ByteOrder.nativeOrder())
                    putFloat(weight)
                }
                val outputBuffer =  ByteBuffer.allocateDirect(4).apply {
                    order(ByteOrder.nativeOrder())
                }

                tflite.run(inputBuffer, outputBuffer)

                outputBuffer.rewind()
                var hargaPrediksi = outputBuffer.float

                when (quality) {
                    "" -> {
                        showErrorDialog("Kulitas tidak terdeteksi")
                        hargaPrediksi = 0f
                    }
                    "Ripe" -> {
                        showSuccessDialogBuyer("Pisang dengan kualitas Ripe")
                    }
                    "Overripe" -> {
                        showSuccessDialogBuyer("Pisang dengan kualitas Overripe , Harga dikurangi Rp.2.000,00")
                        hargaPrediksi -= 2000
                    }
                    "Unripe" -> {
                        showErrorDialog("Pisang dengan kulitas Unripe tidak dapat dijual")
                        hargaPrediksi = 0f
                    }
                }

                binding.tvHarga.text = formatPrice(hargaPrediksi)

                println("Output model: $hargaPrediksi")

                isPredictionDone = true

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Failed to make prediction", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun detectJenisPisang(bitmap: Bitmap): String {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, img_width, img_height, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)
        val outputScores = Array(1) { FloatArray(NUM_CLASSES_JENIS_PISANG) }
        interpreterJenisPisang.run(byteBuffer, outputScores)
        val result = outputScores[0]
        val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1

        return when (maxIndex) {
            0 -> "Pisang Ambon"
            1 -> "Pisang Barangan"
            2 -> "Pisang Kepok"
            3 -> "Pisang Raja"
            4 -> "Pisang Tanduk"
            5 -> "Pisang Uli"
            else -> "Unknown"
        }
    }

    private fun processImage(bitmap: Bitmap) {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, img_width, img_height, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedBitmap)
        val outputScores = Array(1) { FloatArray(NUM_CLASSES) }
        interpreter.run(byteBuffer, outputScores)
        val result = outputScores[0]
        val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1
        val label = when (maxIndex) {
            0 -> "Overripe"
            1 -> "Ripe"
            2 -> "Unripe"
            else -> "Unknown"
        }

        binding.tvKualitaPisang.text = "$label"

        val weightText = binding.tvWeight.text.toString()
        val weight = weightText.replace(" gram", "").toFloatOrNull() ?: 0f
        predictPrice(weight, label)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * img_width * img_height * 3).apply {
            order(ByteOrder.nativeOrder())
        }
        val pixels = IntArray(img_width * img_height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        pixels.forEach { pixel ->
            val r = (pixel shr 16 and 0xFF) / 255f
            val g = (pixel shr 8 and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        return byteBuffer
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

    private fun loadModelFile(): ByteBuffer {
        return assets.openFd("banana_detectionkualitas_model.tflite").run {
            FileInputStream(fileDescriptor).channel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
            )
        }
    }

    private fun loadModelFileJenisPisang(): ByteBuffer {
        return assets.openFd("banana_detectionjenis_model.tflite").run {
            FileInputStream(fileDescriptor).channel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
            )
        }
    }

    private fun formatPrice(price: Float): String {
        return NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(price)
    }

    private fun showDialogReplace() {
        val customDialogView = layoutInflater.inflate(R.layout.costum_dialog_addproses, null)
        val dialog = AlertDialog.Builder(this)
            .setView(customDialogView)
            .create()

        customDialogView.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            val intent = Intent(this, CameraSellerActivity::class.java)
            startActivity(intent)
            finish()
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

    private fun showSuccessDialogBuyer(message: String) {
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

