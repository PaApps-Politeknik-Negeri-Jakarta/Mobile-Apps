package com.syhdzn.tugasakhirapp.pisang_seller.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityCameraSellerBinding
import com.syhdzn.tugasakhirapp.pisang_seller.add.ProcessAddActivity
import com.syhdzn.tugasakhirapp.utils.createFile
import com.syhdzn.tugasakhirapp.utils.showToast
import com.syhdzn.tugasakhirapp.utils.uriToFile
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraSellerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraSellerBinding
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null
    private var capturedFile: File? = null
    private var isFlashEnabled = false
    private var loadingDialog: SweetAlertDialog? = null

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            showToast("Camera permission denied.")
        }
    }

    private val launcherIntentGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri? = result.data?.data
            selectedImage?.let {
                setupLoading()
                capturedFile = uriToFile(selectedImage, this)
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, ProcessAddActivity::class.java).apply {
                        putExtra("imageUri", selectedImage.toString())
                    }
                    startActivity(intent)
                    hideLoading()
                }, 1000)
            }
        } else {
            hideLoading()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraSellerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViews()
        setupActions()
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermissionAndStart()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseResources()
    }

    private fun setupViews() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupActions() {
        binding.apply {
            btnCamera.setOnClickListener { takePhoto() }
            btnGallery.setOnClickListener { startGallery() }
            btnSwitch.setOnClickListener {
                cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }
                startCamera()
            }
        }
        binding.btnFlash.setOnClickListener { toggleFlash() }

        binding.ivBackSeller.setOnClickListener {
            val intent = Intent(this, ProcessAddActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }
            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                showToast("Failed to show camera")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = createFile(applicationContext)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        setupLoading()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                showToast("Failed to take image.")
                hideLoading()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                handleImageSaved(photoFile)
            }
        })
    }

    private fun handleImageSaved(photoFile: File) {
        hideLoading()
        val intent = Intent(this, ProcessAddActivity::class.java).apply {
            putExtra("picture", photoFile)
            putExtra("isBackCamera", cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
        }
        startActivity(intent)
    }

    private fun startGallery() {
        val intent = Intent().apply {
            action = Intent.ACTION_GET_CONTENT
            type = "image/*"
        }
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        launcherIntentGallery.launch(chooser)
    }

    private fun toggleFlash() {
        isFlashEnabled = !isFlashEnabled
        val flashMode = if (isFlashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        imageCapture?.flashMode = flashMode

        val flashIconResId = if (isFlashEnabled) R.drawable.ic_flash else R.drawable.ic_flash_off
        binding.btnFlash.setImageResource(flashIconResId)
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
        loadingDialog?.dismissWithAnimation()
    }

    private fun releaseResources() {
        cameraExecutor.shutdown()
    }
}
