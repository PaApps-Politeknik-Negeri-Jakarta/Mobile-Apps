package com.syhdzn.tugasakhirapp.pisang_buyer.result

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityResultBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.CustomerViewModelFactory
import com.syhdzn.tugasakhirapp.pisang_buyer.cart.CartViewModel
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var interpreter: Interpreter
    private lateinit var viewModel: CartViewModel
    private var averageWeight: Float = 0f
    private var isPredictionDone = false
    private var loadingDialog: SweetAlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupWindowInsets()

        databaseReference = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        storageReference = FirebaseStorage.getInstance().reference
        interpreter = Interpreter(loadModelFile())

        setupViewModel()
        displayResults()
        loadWeightData() // Tambahkan ini untuk memuat data berat
        setupActions()
    }

    private fun setupViewModel() {
        val factory = CustomerViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory).get(CartViewModel::class.java)
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

        binding.tvKualitas.text = "$label"
        binding.tvJenis.text = "$labelJenis"

        if (!imageUriString.isNullOrEmpty()) {
            binding.ivItemResult.setImageURI(Uri.parse(imageUriString))
        }
    }

    private fun loadWeightData() {
        databaseReference.child("weight").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val weight = snapshot.getValue(Float::class.java) ?: 0f
                if (weight != 0f) {
                    averageWeight = weight
                    val weightInt = averageWeight.toInt()
                    binding.tvWeight.text = "$weightInt grams"
                } else {
                    showErrorDialog("Banana weight not found.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showErrorDialog("Failed to load data: ${error.message}")
            }
        })
    }

    private fun setupActions() {

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })

        binding.btnPrediksi.setOnClickListener {
            handlePricePrediction()
        }

        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }

        binding.btnBack.setOnClickListener{
            val intent = Intent(this, BuyerDashboardActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handlePricePrediction() {
        if (isPredictionDone) {
            showDialogReplace()
            return
        }

        setupLoading()

        Handler(Looper.getMainLooper()).postDelayed({
            predictPrice(averageWeight)
        }, 3000)
    }

    private fun predictPrice(weight: Float) {
        try {
            val inputBuffer = ByteBuffer.allocateDirect(4).apply {
                order(ByteOrder.nativeOrder())
                putFloat(weight)
            }
            val outputBuffer = ByteBuffer.allocateDirect(4).apply {
                order(ByteOrder.nativeOrder())
            }

            interpreter.run(inputBuffer, outputBuffer)

            outputBuffer.rewind()
            val hargaPrediksi = outputBuffer.float
            binding.tvHarga.text = formatPrice(hargaPrediksi)

            isPredictionDone = true
            hideLoading()
            showSuccessDialog("Price prediction successful: ${formatPrice(hargaPrediksi)}")
        } catch (e: Exception) {
            hideLoading()
            e.printStackTrace()
            Toast.makeText(this, "Failed to predict price", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addToCart() {
        val name = binding.tvJenis.text.toString()
        val quality = binding.tvKualitas.text.toString()
        var priceString = binding.tvHarga.text.toString().replace("[^\\d,]".toRegex(), "").replace(",", ".").trim()
        var price = priceString.toDoubleOrNull() ?: 0.0
        val imageUriString = intent.getStringExtra("imageUri")
        val amount = 1

        if (name == "Unknown") {
            showTypeErrorDialog("Banana type unknown. Cannot add to cart.")
            return
        }

        if (price == 0.0) {
            showPriceErrorDialog("Price is empty. Cannot add to cart.")
            return
        }

        if (quality.equals("unripe", ignoreCase = true)) {
            showQualityErrorDialog("Banana cannot be added to cart because it is unripe.")
            return
        }

        if (quality.equals("overripe", ignoreCase = true)) {
            showQualityErrorDialog("Banana cannot be added to cart because it is overripe.")
            return
        }

        if (imageUriString != null) {
            setupLoading()
            val imageUri = Uri.parse(imageUriString)
            val imageRef = storageReference.child("cart_images/${imageUri.lastPathSegment}")
            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    imageRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                        saveCartItem(name, price, uri.toString(), amount)
                    }
                }
                .addOnFailureListener { exception ->
                    hideLoading()
                    Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            saveCartItem(name, price, "", amount)
        }
    }

    private fun saveCartItem(name: String, price: Double, imageUrl: String, amount: Int) {
        val cartEntity = CartEntity(
            id = 0,
            name = name,
            price = price,
            idbarang = generateFirebaseId(),
            imageUrl = imageUrl,
            amount = amount,
            ignoreCheck = true
        )

        viewModel.addCartItemWithoutCheck(cartEntity)
        hideLoading()
        showSuccessDialogAndNavigate("Banana successfully added to cart")
    }

    private fun generateFirebaseId(): String {
        val newRef = databaseReference.child("cart").push()
        return newRef.key ?: ""
    }

    private fun loadModelFile(): ByteBuffer {
        return assets.openFd("model_harga_pisang.tflite").run {
            FileInputStream(fileDescriptor).channel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
            )
        }
    }

    private fun formatPrice(price: Float): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val decimalFormatSymbols = (numberFormat as DecimalFormat).decimalFormatSymbols
        decimalFormatSymbols.currencySymbol = "Rp"
        numberFormat.decimalFormatSymbols = decimalFormatSymbols
        numberFormat.maximumFractionDigits = 0
        numberFormat.minimumFractionDigits = 0
        return numberFormat.format(price)
    }

    private fun showDialogReplace() {
        val customDialogView = LayoutInflater.from(this).inflate(R.layout.costum_dialog_addproses, null)
        val dialog = AlertDialog.Builder(this)
            .setView(customDialogView)
            .create()

        customDialogView.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            startActivity(Intent(this, BuyerDashboardActivity::class.java).apply {
                putExtra("switchToFragment", "DetectionFragment")
                putExtra("selectMenuItem", R.id.cam)
            })
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

    private fun showTypeErrorDialog(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setConfirmClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showPriceErrorDialog(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setConfirmClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showQualityErrorDialog(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setConfirmClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showSuccessDialogAndNavigate(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setConfirmClickListener {
            dialog.dismiss()
            navigateToCartFragment()
        }
        dialog.show()
        dialog.hideConfirmButton()

        Handler(Looper.getMainLooper()).postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
                navigateToCartFragment()
            }
        }, 2000)
    }

    private fun navigateToCartFragment() {
        val intent = Intent(this, BuyerDashboardActivity::class.java).apply {
            putExtra("switchToFragment", "CartFragment")
            putExtra("selectMenuItem", R.id.cart)
        }
        startActivity(intent)
        finish()
    }

    private fun showSuccessDialog(message: String) {
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
