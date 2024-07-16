package com.syhdzn.tugasakhirapp.pisang_buyer.cart

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.FragmentCartBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.CustomerViewModelFactory
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity
import com.syhdzn.tugasakhirapp.pisang_buyer.payment.PaymentActivity
import com.syhdzn.tugasakhirapp.pisang_buyer.reset_pass.ResetPasswordActivity
import com.syhdzn.tugasakhirapp.register.RegisterActivity

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupAction()
        setupViewModel()
        observeViewModel()

        userId = getUserIdFromPreferences()


    }

    override fun onResume() {
        super.onResume()
        Log.d("CartFragment", "onResume called")
        viewModel.checkCartItems()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onItemRemoved = { item -> viewModel.removeCartItem(item) }
        )
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }


    private fun setupAction() {
        binding.btnCheckout.setOnClickListener {
            if (cartAdapter.currentList.isNotEmpty()) {
                navigateToPayment()
            } else {
                showEmptyDialog("Cart is empty, cannot proceed to checkout")
            }
        }
    }

    private fun setupViewModel() {
        val factory = CustomerViewModelFactory(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(CartViewModel::class.java)
    }

    private fun observeViewModel() {
        viewModel.cartItems.observe(viewLifecycleOwner, Observer { items ->
            cartAdapter.submitList(items)
            binding.tvTotalHarga.text = calculateTotalPrice(items)
            Log.d("CartFragment", "Observed cart items: $items")
        })
    }

    private fun calculateTotalPrice(items: List<CartEntity>): String {
        val total = items.sumOf { it.price }
        return CartAdapter.CartViewHolder.formatPrice(total.toFloat())
    }

    private fun navigateToPayment() {
        val intent = Intent(activity, PaymentActivity::class.java)
        intent.putParcelableArrayListExtra("CART_ITEMS", ArrayList(cartAdapter.currentList))
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
    }

    private fun getUserIdFromPreferences(): String {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", AppCompatActivity.MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", "") ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showEmptyDialog(message: String) {
        val dialog = SweetAlertDialog(requireContext(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
        dialog.setContentText(message)
        dialog.setCancelable(false)
        dialog.setCustomImage(R.drawable.ic_warning)
        dialog.show()
    }
}