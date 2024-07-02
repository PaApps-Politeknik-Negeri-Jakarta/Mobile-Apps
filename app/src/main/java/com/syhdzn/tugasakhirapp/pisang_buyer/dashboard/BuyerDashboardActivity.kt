package com.syhdzn.tugasakhirapp.pisang_buyer.dashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.syhdzn.tugasakhirapp.pisang_buyer.camera.CameraFragment
import com.syhdzn.tugasakhirapp.pisang_buyer.cart.CartFragment
import com.syhdzn.tugasakhirapp.pisang_buyer.home.HomeFragment
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityBuyerDashboardBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.user_acc.UserFragment

class BuyerDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuyerDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        firstSelectedItem()
        observeSelectedItem()
        setupBottomNavigation()
        handleIntent(intent)
    }

    private fun firstSelectedItem() {
        binding.menuBottom.setItemSelected(R.id.home, true)
        supportFragmentManager.beginTransaction().replace(R.id.container, HomeFragment()).commit()
    }

    private fun observeSelectedItem() {
        viewModel.selectedItemId.observe(this, Observer { itemId ->
            when (itemId) {
                R.id.home -> fragment = HomeFragment()
                R.id.cart -> fragment = CartFragment()
                R.id.cam -> fragment = CameraFragment()
                R.id.user -> fragment = UserFragment()
            }
            fragment?.let {
                supportFragmentManager.beginTransaction().replace(R.id.container, it).commit()
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.menuBottom.setOnItemSelectedListener { itemId ->
            viewModel.setSelectedItemId(itemId)
        }
    }

    private fun handleIntent(intent: Intent) {
        val switchToFragment = intent.getStringExtra("switchToFragment")
        val selectMenuItem = intent.getIntExtra("selectMenuItem", -1)

        switchToFragment?.let {
            when (it) {
                "DetectionFragment", "ShopFragment", "HistoryFragment" -> switchToDetectionFragment()
            }
        }

        if (selectMenuItem != -1) {
            binding.menuBottom.setItemSelected(selectMenuItem, true)
        }
    }

    private fun switchToDetectionFragment() {
        fragment = CameraFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment!!)
            .addToBackStack(null)
            .commit()
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

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, BuyerDashboardActivity::class.java))
    }
}
