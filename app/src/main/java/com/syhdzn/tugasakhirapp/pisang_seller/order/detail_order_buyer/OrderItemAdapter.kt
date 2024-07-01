package com.syhdzn.tugasakhirapp.pisang_seller.order.detail_order_buyer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.pisang_seller.order.OrderItem
import java.text.NumberFormat
import java.util.Locale

class OrderItemAdapter(private val items: List<OrderItem>) :
    RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_penjual_order_detail, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    class OrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivItemImage: ImageView = itemView.findViewById(R.id.productImage)
        private val tvItemName: TextView = itemView.findViewById(R.id.productName)
        private val tvItemPrice: TextView = itemView.findViewById(R.id.productPrice)
        private val tvItemQuantity: TextView = itemView.findViewById(R.id.productAmount)

        fun bind(item: OrderItem) {
            Glide.with(itemView.context).load(item.imageUrl).into(ivItemImage)
            tvItemName.text = item.name
            tvItemPrice.text = formatRupiah(item.price)
            tvItemQuantity.text = "Quantity: ${item.amount}"
        }

        private fun formatRupiah(value: Double): String {
            val localeID = Locale("in", "ID")
            val format = NumberFormat.getCurrencyInstance(localeID)
            return format.format(value)
        }
    }
}
