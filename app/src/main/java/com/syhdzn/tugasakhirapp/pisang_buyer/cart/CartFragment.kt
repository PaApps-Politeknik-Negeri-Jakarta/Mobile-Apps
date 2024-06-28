package com.syhdzn.tugasakhirapp.pisang_buyer.cart

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.syhdzn.tugasakhirapp.databinding.FragmentCartBinding

class CartFragment : Fragment() {

    private lateinit var binding: FragmentCartBinding
    private lateinit var viewModel: CartViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(CartViewModel::class.java)

        val adapter = CartAdapter()
        binding.rvCart.adapter = adapter

        viewModel.getAllCartItems().observe(viewLifecycleOwner, Observer { cartItems ->
            cartItems?.let { adapter.submitList(it) }
        })
    }
}