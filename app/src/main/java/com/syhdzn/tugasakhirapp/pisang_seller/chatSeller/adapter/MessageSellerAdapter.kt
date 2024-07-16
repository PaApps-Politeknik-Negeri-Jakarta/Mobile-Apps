package com.syhdzn.tugasakhirapp.pisang_seller.chatSeller.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.chat.data.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageSellerAdapter(
    private val listOfMessage: List<Message>,
    private val userId: String
) : RecyclerView.Adapter<MessageHolder>() {

    private val LEFT = 0
    private val RIGHT = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == RIGHT) {
            val view = inflater.inflate(R.layout.item_chat_sender, parent, false)
            MessageHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_receiver, parent, false)
            MessageHolder(view)
        }
    }

    override fun getItemCount() = listOfMessage.size

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val message = listOfMessage[position]

        holder.messageText.visibility = View.VISIBLE
        holder.timeOfSent.visibility = View.VISIBLE

        holder.messageText.text = message.text
        holder.timeOfSent.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
    }

    override fun getItemViewType(position: Int): Int {
        return if (listOfMessage[position].senderId == userId) RIGHT else LEFT
    }
}

class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView.rootView) {
    val messageText: TextView = itemView.findViewById(R.id.show_message)
    val timeOfSent: TextView = itemView.findViewById(R.id.timeView)
}
