package com.syhdzn.tugasakhirapp.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityLoginBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity
import com.syhdzn.tugasakhirapp.register.RegisterActivity
import com.syhdzn.tugasakhirapp.pisang_buyer.reset_pass.ResetPasswordActivity
import com.syhdzn.tugasakhirapp.pisang_seller.dashboard.SellerDashboardActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        setupView()
        setupAction()
        setupKeyboardClosing()

    }

    private fun setupView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        })

        val edLoginPassword = binding.edLoginPassword
        val icShowPass = binding.icShowPass

        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                showEmptyDialog("Email and password cannot be empty")
            }
        }

        binding.icShowPass.setOnClickListener {
            togglePasswordVisibility(edLoginPassword, icShowPass)
        }

        binding.regisnow.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            finish()
        }

        binding.resetnow.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ResetPasswordActivity::class.java))
            finish()
        }
    }

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    val userId = user?.uid
                    if (userId != null) {
                        saveUserIdToPreferences(userId)
                        checkUserRole(email)
                    } else {
                        showErrorDialog("Failed to get user ID")
                    }
                } else {
                    showInvalidDialog("Invalid email or password")
                }
            }
    }

    private fun checkUserRole(email: String) {
        setupLoading()
        val query = databaseReference.child("users").orderByChild("email").equalTo(email)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (userSnapshot in dataSnapshot.children) {
                        val role = userSnapshot.child("role").getValue(String::class.java)
                        if (role != null) {
                            when (role) {
                                "Pembeli" -> {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        showSuccessDialogBuyer("Login Successful")
                                        hideLoading()
                                    }, 1000)
                                }
                                "Penjual" -> {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        showSuccessDialogSeller("Login Successful as Seller")
                                        hideLoading()
                                    }, 1000)
                                }
                                else -> {
                                    showErrorDialog("Unknown role")
                                }
                            }
                        } else {
                            showErrorDialog("User role not found")
                        }
                    }
                } else {
                    showErrorDialog("User not found")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showErrorDialog("Database error: ${databaseError.message}")
            }
        })
    }

    private fun saveUserIdToPreferences(userId: String) {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("USER_ID", userId)
        editor.apply()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupKeyboardClosing() {
        val rootLayout = findViewById<View>(android.R.id.content)
        rootLayout.setOnTouchListener { _, _ ->
            currentFocus?.let { focusedView ->
                val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(focusedView.windowToken, 0)
                focusedView.clearFocus()
            }
            false
        }
    }

    private fun togglePasswordVisibility(edLoginPassword: EditText, icShowPass: ImageView) {
        val inputType = if (edLoginPassword.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            InputType.TYPE_CLASS_TEXT
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        edLoginPassword.inputType = inputType
        val font = ResourcesCompat.getFont(this, R.font.font_3_reguler)
        edLoginPassword.typeface = font
        icShowPass.setImageResource(if (inputType == InputType.TYPE_CLASS_TEXT) R.drawable.ic_visible else R.drawable.ic_invisible)
        edLoginPassword.setSelection(edLoginPassword.text.length)
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

    private fun showInvalidDialog(message: String) {
        val Dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
        Dialog.setContentText(message)
        Dialog.setCancelable(false)
        Dialog.show()
    }

    private fun showErrorDialog(message: String) {
        val Dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
        Dialog.setContentText(message)
        Dialog.setCancelable(false)
        Dialog.show()
    }

    private fun showSuccessDialogBuyer(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setConfirmClickListener {
            startActivity(Intent(this@LoginActivity, BuyerDashboardActivity::class.java))
            finish()
        }
        dialog.show()
    }

    private fun showSuccessDialogSeller(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setConfirmClickListener {
            startActivity(Intent(this@LoginActivity, SellerDashboardActivity::class.java))
            finish()
        }
        dialog.show()
    }

    private fun showEmptyDialog(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setCustomImage(R.drawable.ic_warning)
        dialog.show()
    }


}
