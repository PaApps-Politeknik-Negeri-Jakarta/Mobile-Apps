package com.syhdzn.tugasakhirapp.pisang_seller.order.list_order_buyer

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.pisang_seller.order.detail_order_buyer.BuyerDetailActivity
import com.syhdzn.tugasakhirapp.pisang_seller.order.Order
import java.text.NumberFormat
import java.util.Locale

class BuyerOrderAdapter(private val orders: List<Order>, private val userId: String) :
    RecyclerView.Adapter<BuyerOrderAdapter.BuyerOrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyerOrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_penjual_detail_buyer, parent, false)
        return BuyerOrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuyerOrderViewHolder, position: Int) {
        val order = orders[position]
        holder.bind(order)
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, BuyerDetailActivity::class.java).apply {
                putExtra("USER_ID", userId)
                putExtra("ORDER_ID", order.orderId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = orders.size

    class BuyerOrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvVirtualCode: TextView = itemView.findViewById(R.id.tvVirtualCode)
        private val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)
        private val tvPaymentStatus: TextView = itemView.findViewById(R.id.tvPaymentStatus)

        fun bind(order: Order) {
            tvVirtualCode.text = order.virtualCode
            tvTotalPrice.text = formatRupiah(order.totalPrice)
            tvPaymentStatus.text = order.paymentStatus
        }

        private fun formatRupiah(value: Double): String {
            val localeID = Locale("in", "ID")
            val format = NumberFormat.getCurrencyInstance(localeID)
            return format.format(value)
        }
    }
}
