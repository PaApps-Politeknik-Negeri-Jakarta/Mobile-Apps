package com.syhdzn.tugasakhirapp.pisang_seller.order

data class UserOrder(
    val userId: String = "",
    val fullname: String = "",
    var orderCount: Int = 0
)

data class Order(
    val orderId: String = "",
    val virtualCode: String = "",
    val totalPrice: Double = 0.0,
    val paymentStatus: String = "",
    val shippingMethod: String = "",
    val address: String = ""
)

data class OrderItem(
    val amount: Int = 0,
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = ""
)
