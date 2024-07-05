package com.syhdzn.tugasakhirapp.pisang_seller.order.list_buyer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.pisang_seller.order.UserOrder

class BuyerAdapter(
    private val users: List<UserOrder>,
    private val onItemClicked: (UserOrder) -> Unit
) : RecyclerView.Adapter<BuyerAdapter.BuyerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BuyerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_penjual_user_order, parent, false)
        return BuyerViewHolder(view)
    }

    override fun onBindViewHolder(holder: BuyerViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
        holder.itemView.setOnClickListener { onItemClicked(user) }
    }

    override fun getItemCount(): Int = users.size

    class BuyerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userName: TextView = itemView.findViewById(R.id.userName)
        private val orderCount: TextView = itemView.findViewById(R.id.orderCount)

        fun bind(user: UserOrder) {
            userName.text = user.fullname
            orderCount.text = "Orders: ${user.orderCount}"
        }
    }
}
