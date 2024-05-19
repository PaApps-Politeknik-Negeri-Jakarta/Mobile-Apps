package com.syhdzn.tugasakhirapp.pisang_buyer.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.FragmentCameraBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.proses.ProcessActivity
import com.syhdzn.tugasakhirapp.utils.createFile
import com.syhdzn.tugasakhirapp.utils.showToast
import com.syhdzn.tugasakhirapp.utils.uriToFile
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val CAMERA_PERMISSION_REQUEST = 123

class CameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding
    private lateinit var cameraExecutor: ExecutorService
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var imageCapture: ImageCapture? = null
    private var capturedFile: File? = null
    private var isFlashEnabled = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
    }

    private fun checkCameraPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }
            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(requireActivity(), cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                requireActivity().showToast("Failed to show camera")
            }
        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                requireActivity().showToast("Camera permission denied.")
            }
        }
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = createFile(requireContext().applicationContext)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        setupLoading()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                requireActivity().showToast("Failed to take image.")
                hideLoading()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                handleImageSaved(photoFile)
            }
        })
    }

    private fun handleImageSaved(photoFile: File) {
        hideLoading()
        val intent = Intent(requireContext(), ProcessActivity::class.java).apply {
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

    private val launcherIntentGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri? = result.data?.data
            selectedImage?.let {
                setupLoading()
                capturedFile = uriToFile(selectedImage, requireContext())
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(requireContext(), ProcessActivity::class.java).apply {
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

    private fun toggleFlash() {
        isFlashEnabled = !isFlashEnabled
        val flashMode = if (isFlashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
        imageCapture?.flashMode = flashMode

        val flashIconResId = if (isFlashEnabled) R.drawable.ic_flash else R.drawable.ic_flash_off
        binding.btnFlash.setImageResource(flashIconResId)
    }

    private fun setupLoading() {
        SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE).apply {
            progressHelper.barColor = Color.parseColor("#06283D")
            titleText = "Loading"
            setCancelable(false)
            show()
        }
    }

    private fun hideLoading() {
        SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE).apply {
            progressHelper.barColor = Color.parseColor("#06283D")
            titleText = "Loading"
            setCancelable(false)
            hide()
        }
    }

    private fun releaseResources() {
        cameraExecutor.shutdown()
    }
}
