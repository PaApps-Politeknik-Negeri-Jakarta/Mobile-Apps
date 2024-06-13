package com.syhdzn.tugasakhirapp.pisang_buyer.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.syhdzn.tugasakhirapp.databinding.ItemProductBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.data.Product
import com.syhdzn.tugasakhirapp.pisang_buyer.detail.DetailProductActivity
import java.text.NumberFormat
import java.util.*

class ProductAdapter(private val productList: ArrayList<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = productList.size

    private fun formatPrice(price: Float): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(price)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = productList[position]
        holder.apply {
            binding.apply {
                tvProductName.text = currentItem.nama_pisang
                val price = (currentItem.harga as? Number)?.toFloat() ?: 0f
                tvProductPrice.text = formatPrice(price)

                Picasso.get().load(currentItem.image_url).into(ivProductImage)

                root.setOnClickListener {
                    val context = holder.itemView.context
                    val intent = Intent(context, DetailProductActivity::class.java).apply {
                        putExtra("ID", currentItem.id)
                        putExtra("NAME", currentItem.nama_pisang)
                        putExtra("PRICE", currentItem.harga)
                        putExtra("QUALITY", currentItem.kualitas)
                        putExtra("WEIGHT", currentItem.berat)
                        putExtra("IMG", currentItem.image_url)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }
}
