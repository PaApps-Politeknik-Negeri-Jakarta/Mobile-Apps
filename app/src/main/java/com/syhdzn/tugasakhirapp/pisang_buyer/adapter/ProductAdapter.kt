package com.syhdzn.tugasakhirapp.pisang_buyer.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.syhdzn.tugasakhirapp.databinding.ItemProductBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.data.Product
import com.syhdzn.tugasakhirapp.pisang_buyer.detail.DetailProductActivity
import java.text.NumberFormat
import java.util.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

class ProductAdapter(private val productList: ArrayList<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = productList.size

    private fun formatPrice(price: Float): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(price)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = productList[position]
        holder.binding.apply {
            tvProductName.text = currentItem.nama_pisang
            val price = (currentItem.harga as? Number)?.toFloat() ?: 0f
            tvProductPrice.text = formatPrice(price)

            val radius = 8
            val requestOptions = RequestOptions()
                .override(150, 150)
                .transform(CenterCrop(), RoundedCorners(radius))

            Glide.with(ivProductImage.context)
                .load(currentItem.image_url)
                .apply(requestOptions)
                .into(ivProductImage)

            root.setOnClickListener {
                val context = holder.itemView.context

                val sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val fullname = sharedPreferences.getString("FULL_NAME", "") ?: ""

                val intent = Intent(context, DetailProductActivity::class.java).apply {
                    putExtra("ID", currentItem.id)
                    putExtra("NAME", currentItem.nama_pisang)
                    putExtra("PRICE", currentItem.harga)
                    putExtra("QUALITY", currentItem.kualitas)
                    putExtra("WEIGHT", currentItem.berat)
                    putExtra("IMG", currentItem.image_url)
                    putExtra("FULL_NAME", fullname)
                }
                context.startActivity(intent)
            }
        }
    }
}
