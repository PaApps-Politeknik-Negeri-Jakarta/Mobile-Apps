package com.syhdzn.tugasakhirapp.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.register.RegisterActivity
import com.syhdzn.tugasakhirapp.reset_pass.ResetPasswordActivity
import com.syhdzn.tugasakhirapp.dashboard.DashboardActivity
import com.syhdzn.tugasakhirapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        setupKeyboardClosing()
        setupView()
        setupAction()
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

        binding.icShowPass.setOnClickListener{
            togglePasswordVisibility(edLoginPassword, icShowPass)
        }

        binding.regisnow.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            finish()
        }

        binding.resetnow.setOnClickListener {
            startActivity(Intent(this@LoginActivity, ResetPasswordActivity ::class.java))
            finish()
        }

    }

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login berhasil
                    showSuccessDialog("Login Successful")
                } else {
                    // Login gagal
                    showInvalidDialog("Invalid email or password")
                }
            }
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

    private fun showSuccessDialog(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setConfirmClickListener {
            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
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
}
