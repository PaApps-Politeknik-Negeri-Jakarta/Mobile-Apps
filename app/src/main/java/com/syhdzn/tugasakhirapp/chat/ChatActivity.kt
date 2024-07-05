package com.syhdzn.tugasakhirapp.chat

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.chat.adapter.MessageAdapter
import com.syhdzn.tugasakhirapp.chat.data.Message
import com.syhdzn.tugasakhirapp.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var database: DatabaseReference
    private lateinit var chatRoomId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageView

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        chatRoomId = intent.getStringExtra("chatRoomId") ?: return

//        val imgUri = intent.getStringExtra("IMG")
//        val name = intent.getStringExtra("NAME")
//
//        Picasso.get().load(imgUri).into(binding.productImage)
//        binding.tvProductNameDetail.text = name

        recyclerView = binding.rvChat
        messageInput = binding.etChat
        sendButton = binding.send

        adapter = MessageAdapter(messages, getUserIdFromPreferences())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        sendButton.setOnClickListener {
            sendMessage()
        }

        listenForMessages()
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val userId = getUserIdFromPreferences()
            if (userId.isNotEmpty()) {
                val messageId = database.child("chats").child(chatRoomId).child("messages").push().key
                if (messageId != null) {
                    val message = Message(
                        senderId = userId,
                        text = messageText,
                        timestamp = System.currentTimeMillis()
                    )
                    database.child("chats").child(chatRoomId).child("messages").child(messageId).setValue(message)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                messageInput.text.clear()
                                recyclerView.scrollToPosition(messages.size - 1)
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

