package com.syhdzn.tugasakhirapp.pisang_seller.add

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.syhdzn.tugasakhirapp.databinding.ItemAddProductBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.data.Product
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class AddProductAdapter(
    private val productList: ArrayList<Product>,
    private val onDeleteClick: (String, Int) -> Unit
) : RecyclerView.Adapter<AddProductAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemAddProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAddProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = productList.size

    private fun formatPrice(price: Float): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val decimalFormatSymbols = (numberFormat as DecimalFormat).decimalFormatSymbols
        decimalFormatSymbols.currencySymbol = "Rp"
        numberFormat.decimalFormatSymbols = decimalFormatSymbols
        numberFormat.maximumFractionDigits = 0
        numberFormat.minimumFractionDigits = 0
        return numberFormat.format(price)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = productList[position]
        holder.binding.apply {
            productName.text = currentItem.nama_pisang ?: "Unknown"
            productWeight.text = "${currentItem.berat}g"

            val price = (currentItem.harga as? Number)?.toFloat() ?: 0f
            productPrice.text = formatPrice(price)

            val radius = 16
            val requestOptions = RequestOptions()
                .override(150, 150)
                .transform(CenterCrop(), RoundedCorners(radius))

            Glide.with(productImage.context)
                .load(currentItem.image_url ?: "")
                .apply(requestOptions)
                .into(productImage)

            imageView17.setOnClickListener {
                onDeleteClick(currentItem.id ?: "", position)
            }
        }
    }
}
