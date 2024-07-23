package com.syhdzn.tugasakhirapp.pisang_buyer.data

data class Product(
    val id: String? = null,
    val nama_pisang: String? = null,
    val kualitas: String? = null,
    val berat: Int? = 0,
    val harga: Double? = 0.0,
    val image_url: String? = null,
    var newChatCount: Int = 0,
    var hasNewChat: Boolean = false
)
