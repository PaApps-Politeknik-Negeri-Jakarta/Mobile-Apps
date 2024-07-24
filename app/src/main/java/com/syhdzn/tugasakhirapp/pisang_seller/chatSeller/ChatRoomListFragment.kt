package com.syhdzn.tugasakhirapp.pisang_seller.chatSeller

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.chat.data.ChatRoom
import com.syhdzn.tugasakhirapp.databinding.FragmentChatRoomListBinding
import com.syhdzn.tugasakhirapp.pisang_seller.chatSeller.adapter.ChatRoomAdapter
import com.syhdzn.tugasakhirapp.pisang_seller.dashboard.SellerDashboardActivity

class ChatRoomListFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var adapter: ChatRoomAdapter
    private val chatRooms = mutableListOf<ChatRoom>()
    private lateinit var productId: String
    private var _binding: FragmentChatRoomListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatRoomListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productId = arguments?.getString("productId") ?: return

        binding.recyclerViewChatRooms.layoutManager = LinearLayoutManager(context)

        adapter = ChatRoomAdapter(requireContext(), chatRooms, productId) { chatRoom ->
        }
        binding.recyclerViewChatRooms.adapter = adapter

        database = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        fetchChatRooms(productId)

        binding.btnBack.setOnClickListener {
            val intent = Intent(requireContext(), SellerDashboardActivity::class.java).apply {
                putExtra("switchToFragment", "ChatFragment")
                putExtra("selectMenuItem", R.id.chatseller)
                flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
            }
            startActivity(intent)
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}