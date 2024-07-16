package com.syhdzn.tugasakhirapp.chat.data

data class ChatRoom(
    val chatRoomId: String = "",
    val receiverId: String = "",
    val senderId: String = "",
    val userName: String = "",
    val senderId_receiverId: String = "",
    val messages: Map<String, Message> = emptyMap()
)

