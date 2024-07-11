package com.syhdzn.tugasakhirapp.pisang_seller.chatSeller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.chat.data.ChatRoom
import com.syhdzn.tugasakhirapp.pisang_seller.chatSeller.adapter.ChatRoomAdapter

class ChatRoomListFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatRoomAdapter
    private val chatRooms = mutableListOf<ChatRoom>()
    private lateinit var productId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_room_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productId = arguments?.getString("productId") ?: return

        recyclerView = view.findViewById(R.id.recyclerViewChatRooms)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = ChatRoomAdapter(requireContext(),chatRooms,productId) { chatRoom ->
        }
        recyclerView.adapter = adapter

        database = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        fetchChatRooms(productId)
    }

    private fun fetchChatRooms(productId: String) {
        database.child("chats").orderByChild("receiverId").equalTo(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    chatRooms.clear()
                    for (childSnapshot in snapshot.children) {
                        val chatRoom = childSnapshot.getValue(ChatRoom::class.java)
                        if (chatRoom != null) {
                            chatRooms.add(chatRoom)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}



