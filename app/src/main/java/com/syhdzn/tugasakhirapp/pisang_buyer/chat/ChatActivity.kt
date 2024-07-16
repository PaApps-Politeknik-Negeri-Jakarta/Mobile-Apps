package com.syhdzn.tugasakhirapp.pisang_buyer.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.pisang_buyer.adapter.MessageAdapter
import com.syhdzn.tugasakhirapp.chat.data.Message
import com.syhdzn.tugasakhirapp.databinding.ActivityChatBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity
import com.syhdzn.tugasakhirapp.pisang_buyer.detail.DetailProductActivity

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var database: DatabaseReference
    private lateinit var chatRoomId: String
    private lateinit var image: String
    private lateinit var name: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView
    private lateinit var backButton: ImageView

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        chatRoomId = intent.getStringExtra("CHAT_ROOM_ID") ?: return
        image = intent.getStringExtra("IMAGE") ?: ""
        name = intent.getStringExtra("NAME") ?: ""

        recyclerView = binding.rvChat
        messageInput = binding.etChat
        sendButton = binding.send
        backButton = binding.back
        binding.productName.text = name
        Glide.with(this).load(image).into(binding.productImage)

        val userId = getUserIdFromPreferences()
        adapter = MessageAdapter(messages, userId)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        sendButton.setOnClickListener {
            Log.e("ChatActivity", "send message")
            sendMessage()
        }
        backButton.setOnClickListener {
            onBackPressed()
        }

        listenForMessages()
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val userId = getUserIdFromPreferences()
            if (userId.isNotEmpty()) {
                val messageId = database.child("chats").child(chatRoomId).child("messages").push().key
                if (messageId!= null) {
                    val message = Message(
                        senderId = userId,
                        text = messageText,
                        timestamp = System.currentTimeMillis()
                    )
                    database.child("chats").child(chatRoomId).child("messages").child(messageId).setValue(message)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                messageInput.text.clear()
                            } else {
                                Log.e("ChatActivity", "Failed to send message", task.exception)
                            }
                        }
                }
            }
        }
    }

    private fun listenForMessages() {
        database.child("chats").child(chatRoomId).child("messages").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    messages.add(message)
                    adapter.notifyItemInserted(messages.size - 1)
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getUserIdFromPreferences(): String {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", "") ?: ""
    }
}
