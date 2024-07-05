package com.syhdzn.tugasakhirapp.chat.data

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)