package com.syhdzn.tugasakhirapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.databinding.ActivitySplashBinding
import com.syhdzn.tugasakhirapp.login.LoginActivity
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity
import com.syhdzn.tugasakhirapp.pisang_seller.dashboard.SellerDashboardActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var databaseReference: DatabaseReference
    private val splashTimeOut: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseReference = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        setupView()
        checkSession()
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

    private fun checkSession() {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val userId = sharedPreferences.getString("USER_ID", null)
        if (userId != null) {
            checkUserRoleById(userId)
        } else {
            navigateToLogin()
        }
    }

    private fun checkUserRoleById(userId: String) {
        val userRef = databaseReference.child("users").child(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val role = dataSnapshot.child("role").getValue(String::class.java)
                if (role != null) {
                    when (role) {
                        "Pembeli" -> {
                            navigateToDashboard(BuyerDashboardActivity::class.java)
                        }
                        "Penjual" -> {
                            navigateToDashboard(SellerDashboardActivity::class.java)
                        }
                        else -> {
                            navigateToLogin()
                        }
                    }
                } else {
                    navigateToLogin()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                navigateToLogin()
            }
        })
    }

    private fun navigateToDashboard(activityClass: Class<*>) {
        Handler().postDelayed({
            val intent = Intent(this, activityClass)
            startActivity(intent)
            finish()
        }, splashTimeOut)
    }

    private fun navigateToLogin() {
        Handler().postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, splashTimeOut)
    }
}
