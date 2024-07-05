package com.syhdzn.tugasakhirapp.pisang_buyer.history.list_history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.syhdzn.tugasakhirapp.databinding.ItemHistoryBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.history.Order

class HistoryAdapter(
    private val onItemClick: (String) -> Unit,
    private val onCancelClick: (String) -> Unit
) : ListAdapter<Order, HistoryAdapter.OrderViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding, onItemClick, onCancelClick)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    class OrderViewHolder(
        private val binding: ItemHistoryBinding,
        private val onItemClick: (String) -> Unit,
        private val onCancelClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: Order) {
            binding.virtualCodeText.text = order.virtualCode
            binding.finalTotalText.text = formatPrice(order.totalPrice)
            binding.paymentStatusText.text = order.paymentStatus
            binding.buttonCancelOrder.setOnClickListener {
                onCancelClick(order.id)
            }
            binding.root.setOnClickListener {
                onItemClick(order.id)
            }
        }

        private fun formatPrice(price: Double): String {
            return java.text.NumberFormat.getCurrencyInstance(java.util.Locale("id", "ID")).format(price)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
