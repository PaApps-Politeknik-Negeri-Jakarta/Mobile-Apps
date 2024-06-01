package com.syhdzn.tugasakhirapp.pisang_buyer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.squareup.picasso.Picasso
import com.syhdzn.tugasakhirapp.databinding.ItemProductBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.data.Product

class ProductAdapter(private val productList: java.util.ArrayList<Product>) : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root){

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return  ViewHolder(ItemProductBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }
    override fun getItemCount(): Int = productList.size

    private fun parsePrice(priceText: String): Float {
        val cleanedPriceText = priceText.replace("Rp", "").replace(".", "").replace(",", "")
        return cleanedPriceText.toFloatOrNull() ?: 0f
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = productList[position]
        holder.apply {
            binding.apply {
                tvProductName.text = currentItem.nama_pisang
                val parsedPrice = parsePrice((currentItem.harga ?: "").toString())
                tvProductPrice.text = "Rp ${parsedPrice}"
                Picasso.get().load(currentItem.image_url).into(ivProductImage)
            }
        }
    }
}