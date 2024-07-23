package com.syhdzn.tugasakhirapp.pisang_seller.dashboard

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivitySellerDashboardBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.camera.CameraFragment
import com.syhdzn.tugasakhirapp.pisang_seller.add.AddFragment
import com.syhdzn.tugasakhirapp.pisang_seller.order.list_buyer.OrderFragment
import com.syhdzn.tugasakhirapp.pisang_seller.chatSeller.ProductListFragment
import com.syhdzn.tugasakhirapp.pisang_seller.user.UserSellerFragment


class SellerDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySellerDashboardBinding
    private val viewModel: DashboardSellerViewModel by viewModels()
    private var fragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Handle back press logic here if needed
            }
        })

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ProductListFragment())
                .commit()
        }

        setupView()
        firstSelectedItem()
        observeSelectedItem()
        setupBottomNavigation()
        handleIntent(intent)
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

    private fun firstSelectedItem() {
        binding.menuBottom.setItemSelected(R.id.add, true)
        supportFragmentManager.beginTransaction().replace(R.id.container, AddFragment()).commit()
    }

    private fun observeSelectedItem() {
        viewModel.selectedItemId.observe(this, Observer { itemId ->
            when (itemId) {
                R.id.add -> fragment = AddFragment()
                R.id.user -> fragment = UserSellerFragment()
                R.id.order -> fragment = OrderFragment()
                R.id.chatseller -> fragment = ProductListFragment()
            }

            fragment?.let {
                supportFragmentManager.beginTransaction().replace(R.id.container, it).commit()
            }
        })
    }

    private fun handleIntent(intent: Intent) {
        val switchToFragment = intent.getStringExtra("switchToFragment")
        val selectMenuItem = intent.getIntExtra("selectMenuItem", -1)

        if (switchToFragment != null) {
            when (switchToFragment) {
                "OrderFragment", "ChatFragment" -> switchToDetectionFragment()
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

    private fun setupBottomNavigation() {
        binding.menuBottom.setOnItemSelectedListener(object : ChipNavigationBar.OnItemSelectedListener {
            override fun onItemSelected(i: Int) {
                viewModel.setSelectedItemId(i)
            }
        })
    }
}
