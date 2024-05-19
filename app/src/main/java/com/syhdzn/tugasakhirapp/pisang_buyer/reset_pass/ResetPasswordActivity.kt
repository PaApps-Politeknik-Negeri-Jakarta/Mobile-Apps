package com.syhdzn.tugasakhirapp.pisang_buyer.reset_pass

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityResetPasswordBinding
import com.syhdzn.tugasakhirapp.login.LoginActivity

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

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
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnSubmit.setOnClickListener {
            val email = binding.edResetEmail.text.toString()

            if (email.isNotEmpty()) {
                checkEmailAndResetPassword(email)
            } else {
                showEmptyDialog("Please enter your email")
            }
        }
    }

    private fun checkEmailAndResetPassword(email: String) {
        databaseReference.child("users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    showSuccessDialog("Password reset email sent to $email")
                                } else {
                                    showErrorDialog("Failed to send password reset email: ${task.exception?.message}")
                                }
                            }
                    } else {
                        showInvalidDialog("Email is not registered")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showErrorDialog("Failed to check email: ${databaseError.message}")
                }
            })
    }

    private fun showInvalidDialog(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showSuccessDialog(message: String) {
        val dialog = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setConfirmClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            onBackPressed()
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
