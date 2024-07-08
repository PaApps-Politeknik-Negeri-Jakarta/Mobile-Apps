package com.syhdzn.tugasakhirapp.pisang_buyer.search

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.pisang_buyer.detail.DetailProductActivity
import java.text.NumberFormat
import java.util.Locale

class PisangAdapter(private val pisangList: List<Pisang>) :
    RecyclerView.Adapter<PisangAdapter.PisangViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PisangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pisang, parent, false)
        return PisangViewHolder(view)
    }

    private fun formatPrice(price: Float): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return numberFormat.format(price)
    }

    override fun onBindViewHolder(holder: PisangViewHolder, position: Int) {
        val pisang = pisangList[position]
        holder.tvProductName.text = pisang.nama_pisang
        val price = (pisang.harga as? Number)?.toFloat() ?: 0f
        holder.tvProductPrice.text = formatPrice(price)

        val radius = 16
        val requestOptions = RequestOptions()
            .transform(CenterCrop(), RoundedCorners(radius))

        Glide.with(holder.ivProductImage.context)
            .load(pisang.image_url)
            .apply(requestOptions)
            .into(holder.ivProductImage)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailProductActivity::class.java).apply {
                putExtra("ID", pisang.id)
                putExtra("NAME", pisang.nama_pisang)
                putExtra("PRICE", pisang.harga)
                putExtra("QUALITY", pisang.kualitas)
                putExtra("WEIGHT", pisang.berat)
                putExtra("IMG", pisang.image_url)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return pisangList.size
    }

    class PisangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.iv_product_image)
        val tvProductName: TextView = itemView.findViewById(R.id.tv_product_name)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tv_product_price)
    }
}
