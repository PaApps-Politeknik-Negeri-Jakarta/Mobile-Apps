package com.syhdzn.tugasakhirapp.pisang_buyer.history.detail_history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.syhdzn.tugasakhirapp.databinding.ItemHistoryOrderDetailBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.history.OrderItem

class OrderDetailAdapter :
    ListAdapter<OrderItem, OrderDetailAdapter.OrderDetailViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
        val binding = ItemHistoryOrderDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderDetailViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class OrderDetailViewHolder(private val binding: ItemHistoryOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(orderItem: OrderItem) {
            binding.productName.text = orderItem.name
            binding.productPrice.text = formatPrice(orderItem.price)
            binding.productAmount.text = orderItem.amount.toString()
            Picasso.get().load(orderItem.imageUrl).into(binding.productImage)
        }

        private fun formatPrice(price: Double): String {
            return java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID")).format(price)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<OrderItem>() {
        override fun areItemsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderItem, newItem: OrderItem): Boolean {
            return oldItem == newItem
        }
    }
}
