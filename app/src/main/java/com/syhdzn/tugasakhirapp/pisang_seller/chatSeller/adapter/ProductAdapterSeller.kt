package com.syhdzn.tugasakhirapp.pisang_seller.chatSeller.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.syhdzn.tugasakhirapp.databinding.ItemProductSellerBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.data.Product

class ProductAdapterSeller(private val products: List<Product>, private val clickListener: (Product) -> Unit) : RecyclerView.Adapter<ProductAdapterSeller.ViewHolder>() {

    class ViewHolder(val binding: ItemProductSellerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemProductSellerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = products[position]
        holder.binding.apply {
            productNameSeller.text = currentItem.nama_pisang
            productWeightSeller.text = currentItem.berat.toString()

            val radius = 10
            val requestOptions = RequestOptions()
                .override(150, 150)
                .transform(CenterCrop(), RoundedCorners(radius))
            Glide.with(productImageSeller.context)
                .load(currentItem.image_url)
                .apply(requestOptions)
                .into(productImageSeller)

            root.setOnClickListener {
                clickListener(currentItem)
            }
        }
    }
}
