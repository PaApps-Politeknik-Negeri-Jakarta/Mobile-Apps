package com.syhdzn.tugasakhirapp.pisang_buyer.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.syhdzn.tugasakhirapp.databinding.ItemCartBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity

class CartAdapter : ListAdapter<CartEntity, CartAdapter.CartViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<CartEntity>() {
            override fun areItemsTheSame(oldItem: CartEntity, newItem: CartEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CartEntity, newItem: CartEntity): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CartViewHolder(private val binding: ItemCartBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cartEntity: CartEntity) {
            binding.apply {
                tvPruductNameCart.text = cartEntity.name
                productAmount.text = cartEntity.amount.toString()
                Picasso.get().load(cartEntity.imageUrl).into(ivProductImageCart)
            }
        }
    }
}

