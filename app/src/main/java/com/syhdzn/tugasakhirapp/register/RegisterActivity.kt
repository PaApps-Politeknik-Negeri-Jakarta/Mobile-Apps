package com.syhdzn.tugasakhirapp.register

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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityRegisterBinding
import com.syhdzn.tugasakhirapp.login.LoginActivity

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

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
        val edLoginPassword = binding.edRegisPassword
        val icShowPass = binding.icShowPass

        val edConfirmPassword = binding.edRegisConpassword
        val icShowConPass = binding.icShowConpass

        binding.icShowPass.setOnClickListener{
            togglePasswordVisibility(edLoginPassword, icShowPass)
        }

        binding.icShowConpass.setOnClickListener{
            togglePasswordVisibility(edConfirmPassword, icShowConPass)
        }

        binding.loginnow.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnRegis.setOnClickListener {
            val fullname = binding.edRegisFullname.text.toString()
            val email = binding.edRegisEmail.text.toString()
            val password = binding.edRegisPassword.text.toString()
            val confirmPassword = binding.edRegisConpassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && fullname.isNotEmpty()) {
                if (password == confirmPassword) {
                    registerUser(email, password, fullname)
                } else {
                    showFailedDialog("Confirm password and password do not match")
                }
            } else {
                showEmptyDialog("Please enter empty form")
            }
        }
    }

    private fun registerUser(email: String, password: String, fullname: String) {
        databaseReference.child("users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        showFailedDialog("Email has been registered")
                    } else {
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = firebaseAuth.currentUser?.uid
                                    if (userId != null) {
                                        val userData = HashMap<String, Any>()
                                        userData["full name"] = fullname
                                        userData["email"] = email
                                        databaseReference.child("users").child(userId).setValue(userData)
                                            .addOnCompleteListener { dbTask ->
                                                if (dbTask.isSuccessful) {
                                                    showSuccessDialog("Registration successful")
                                                } else {
                                                    showFailedDialog("Failed to save user data")
                                                }
                                            }
                                    } else {
                                        showFailedDialog("Failed to get user ID")
                                    }
                                } else {
                                    showFailedDialog("Registration failed: ${task.exception?.message}")
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showFailedDialog("Failed to check email availability")
                }
            })
    }


    private fun showFailedDialog(message: String) {
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
            startActivity(Intent(this, LoginActivity::class.java))
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
