package com.syhdzn.tugasakhirapp.pisang_buyer.search

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.syhdzn.tugasakhirapp.databinding.ItemProductBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.detail.DetailProductActivity
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale


class PisangAdapter(private val pisangList: List<Pisang>) : RecyclerView.Adapter<PisangAdapter.ViewHolder>() {

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

    override fun getItemCount(): Int = pisangList.size

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
        val currentItem = pisangList[position]
        holder.binding.apply {
            tvProductName.text = currentItem.nama_pisang
            tvProductWeight.text = "${currentItem.berat}g"

            val price = (currentItem.harga as? Number)?.toFloat() ?: 0f
            tvProductPrice.text = formatPrice(price)

            val radius = 16
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
