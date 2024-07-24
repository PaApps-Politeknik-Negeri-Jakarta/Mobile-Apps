package com.syhdzn.tugasakhirapp.pisang_buyer.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.syhdzn.tugasakhirapp.databinding.ItemCartBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private val onItemRemoved: (CartEntity) -> Unit
) : ListAdapter<CartEntity, CartAdapter.CartViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding, onItemRemoved)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class CartViewHolder(
        private val binding: ItemCartBinding,
        private val onItemRemoved: (CartEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartEntity: CartEntity) {
            binding.tvPruductNameCart.text = cartEntity.name
            binding.tvProductPriceCart.text = formatPrice(cartEntity.price.toFloat())
            Picasso.get().load(cartEntity.imageUrl).into(binding.ivProductImageCart)

            binding.btnDelete.setOnClickListener {
                onItemRemoved(cartEntity)
            }
        }

        companion object {
            fun formatPrice(price: Float): String {
                val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                val decimalFormatSymbols = (numberFormat as DecimalFormat).decimalFormatSymbols
                decimalFormatSymbols.currencySymbol = "Rp"
                numberFormat.decimalFormatSymbols = decimalFormatSymbols
                numberFormat.maximumFractionDigits = 0
                numberFormat.minimumFractionDigits = 0
                return numberFormat.format(price)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<CartEntity>() {
        override fun areItemsTheSame(oldItem: CartEntity, newItem: CartEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CartEntity, newItem: CartEntity): Boolean {
            return oldItem == newItem
        }
    }
}
