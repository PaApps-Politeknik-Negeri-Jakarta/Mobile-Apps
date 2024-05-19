package com.syhdzn.tugasakhirapp.PisangBuyer.proses

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.PisangBuyer.dashboard.BuyerDashboardActivity
import com.syhdzn.tugasakhirapp.databinding.ActivityProcessBinding
import com.syhdzn.tugasakhirapp.PisangBuyer.result.ResultActivity
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class ProcessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProcessBinding
    private lateinit var interpreter: Interpreter
    private lateinit var interpreterJenisPisang: Interpreter
    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProcessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load TensorFlow Lite model
        interpreter = Interpreter(loadModelFile())

        // Load TensorFlow Lite model for banana type detection
        interpreterJenisPisang = Interpreter(loadModelFileJenisPisang())

        handleImage()
        setupAction()
    }

    private fun handleImage() {
        val imageUriString = intent.getStringExtra("imageUri")
        val isBackCamera = intent.getBooleanExtra("isBackCamera", false)
        val pictureFile = intent.getSerializableExtra("picture") as? File

        imageFile = when {
            imageUriString != null -> {
                val imageUri = Uri.parse(imageUriString)
                handleGalleryImage(imageUri)
                null
            }
            isBackCamera && pictureFile != null -> {
                val rotatedBitmap = BitmapFactory.decodeFile(pictureFile.absolutePath)
                binding.ivItemProcess.setImageBitmap(rotatedBitmap)
                pictureFile
            }
            !isBackCamera && pictureFile != null -> {
                val bitmap = BitmapFactory.decodeFile(pictureFile.absolutePath)
                binding.ivItemProcess.setImageBitmap(bitmap)
                pictureFile
            }
            else -> null
        }

        if (imageFile == null) {
        }
    }

    private fun handleGalleryImage(imageUri: Uri) {
        binding.ivItemProcess.setImageURI(imageUri)
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
                detectJenisPisang(rotatedBitmap)
                binding.ivItemProcess.setImageBitmap(rotatedBitmap)
                pictureFile
            }
            !isBackCamera && pictureFile != null -> {
                val bitmap = BitmapFactory.decodeFile(pictureFile.absolutePath)
                processImage(bitmap)
                detectJenisPisang(bitmap)
                binding.ivItemProcess.setImageBitmap(bitmap)
                pictureFile
            }
            else -> null
        }

        if (imageFile == null) {
        }
    }

    private fun handleGalleryImageProses(imageUri: Uri) {
        binding.ivItemProcess.setImageURI(imageUri)
        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
        processImage(bitmap)
        detectJenisPisang(bitmap)
    }

    private fun setupAction() {
        binding.btnProcessImage.setOnClickListener {
            handleImageProses()
        }

        binding.ivBgReplace.setOnClickListener {
            showDialogReplace()
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
            0 -> "pisang_ambon"
            1 -> "pisang_barangan"
            2 -> "pisang_kepok"
            3 -> "pisang_raja"
            4 -> "pisang_tanduk"
            5 -> "pisang_uli"
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

        val imageUriString = intent.getStringExtra("imageUri")
        val isBackCamera = intent.getBooleanExtra("isBackCamera", false)
        val imageFile = intent.getSerializableExtra("picture") as? File

        setupLoading()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, ResultActivity::class.java)
            // Deteksi jenis pisang
            val jenisPisang = detectJenisPisang(bitmap)
            intent.putExtra("jenisPisang", jenisPisang)

            intent.putExtra("label", label)
            if (imageUriString != null) {
                intent.putExtra("imageUri", imageUriString)
            } else if (isBackCamera && imageFile != null) {
                val imageUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", imageFile)
                intent.putExtra("imageUri", imageUri.toString())
            }

            startActivity(intent)

            hideLoading()
        }, 1000)
    }



    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * img_width * img_height * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, img_width, img_height, true)
        val pixels = IntArray(img_width * img_height)
        resizedBitmap.getPixels(pixels, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)

        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF)
            val g = (pixel shr 8 and 0xFF)
            val b = (pixel and 0xFF)

            byteBuffer.putFloat(r / 255f)
            byteBuffer.putFloat(g / 255f)
            byteBuffer.putFloat(b / 255f)
        }

        return byteBuffer
    }

    private fun loadModelFile(): ByteBuffer {
        val modelFileDescriptor = assets.openFd("banana_detectionkualitas_model.tflite")
        val inputStream = FileInputStream(modelFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = modelFileDescriptor.startOffset
        val declaredLength = modelFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    private fun loadModelFileJenisPisang(): ByteBuffer {
        val modelFileDescriptor = assets.openFd("banana_detectionjenis_model.tflite")
        val inputStream = FileInputStream(modelFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = modelFileDescriptor.startOffset
        val declaredLength = modelFileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }


    private fun showDialogReplace() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val customDialogView = inflater.inflate(R.layout.costum_dialog_replace, null)

        builder.setView(customDialogView)
        val dialog = builder.create()

        val btnYes = customDialogView.findViewById<Button>(R.id.btn_yes)
        val btnNo = customDialogView.findViewById<Button>(R.id.btn_no)

        btnYes.setOnClickListener {
            val intent = Intent(this, BuyerDashboardActivity::class.java)
            intent.putExtra("switchToFragment", "DetectionFragment")
            intent.putExtra("selectMenuItem", R.id.cam)
            startActivity(intent)
            dialog.dismiss()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_3)
        customDialogView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.anim))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupLoading() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#06283D")
        pDialog.titleText = "Loading"
        pDialog.setCancelable(false)
        pDialog.show()
    }

    private fun hideLoading() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.progressHelper.barColor = Color.parseColor("#06283D")
        pDialog.titleText = "Loading"
        pDialog.setCancelable(false)
        pDialog.hide()
    }


    companion object {
        private const val img_width = 150
        private const val img_height = 150
        private const val NUM_CLASSES = 3
        private const val NUM_CLASSES_JENIS_PISANG = 6
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, BuyerDashboardActivity::class.java)
        startActivity(intent)
    }
}