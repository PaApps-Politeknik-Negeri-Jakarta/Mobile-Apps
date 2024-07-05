package com.syhdzn.tugasakhirapp.pisang_seller.product

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.chat.ChatActivity
import com.syhdzn.tugasakhirapp.chat.data.ChatRoom

class ChatRoomAdapter(
    private val context: Context,
    private val chatRooms: List<ChatRoom>,
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
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("chatRoomId", chatRoom.chatRoomId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = chatRooms.size
}

