package com.syhdzn.tugasakhirapp.pisang_seller.chatSeller.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.chat.data.ChatRoom
import com.syhdzn.tugasakhirapp.pisang_seller.chatSeller.ChatSellerActivity

class ChatRoomAdapter(
    private val context: Context,
    private val chatRooms: List<ChatRoom>,
    private val productId: String,
    private val clickListener: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {

    class ChatRoomViewHolder(view: View, val clickListener: (ChatRoom) -> Unit) : RecyclerView.ViewHolder(view) {
        private val chatRoomId: TextView = view.findViewById(R.id.chatRoomId)

        fun bind(chatRoom: ChatRoom, clickListener: (ChatRoom) -> Unit) {
            chatRoomId.text = chatRoom.chatRoomId
            itemView.setOnClickListener {
                clickListener(chatRoom)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        return ChatRoomViewHolder(view, clickListener)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(chatRooms[position]) { chatRoom ->
            val intent = Intent(context, ChatSellerActivity::class.java).apply {
                putExtra("chatRoomId", chatRoom.chatRoomId)
                putExtra("productID", productId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = chatRooms.size
}

