package com.syhdzn.tugasakhirapp.pisang_buyer.history

data class Order(
    var id: String = "",
    val items: List<OrderItem> = listOf(),
    val totalPrice: Double = 0.0,
    val virtualCode: String = "",
    val paymentStatus: String = "",
    val shippingMethod: String = "",
    val address: String = ""
)

data class OrderItem(
    val id: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val idbarang: String = "",
    val imageUrl: String = "",
    val amount: Int = 0
)


