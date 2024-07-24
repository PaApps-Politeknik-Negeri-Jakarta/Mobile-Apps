package com.syhdzn.tugasakhirapp.pisang_seller.order.list_buyer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.databinding.FragmentOrderBinding
import com.syhdzn.tugasakhirapp.pisang_seller.order.UserOrder
import com.syhdzn.tugasakhirapp.pisang_seller.order.list_order_buyer.BuyerOrderActivity

class OrderFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var binding: FragmentOrderBinding
    private lateinit var buyerAdapter: BuyerAdapter
    private val userList = mutableListOf<UserOrder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        setupRecyclerView()
        loadUsers()
    }

    private fun setupRecyclerView() {
        buyerAdapter = BuyerAdapter(userList) { user ->
            val intent = Intent(requireContext(), BuyerOrderActivity::class.java).apply {
                putExtra("USER_ID", user.userId)
            }
            startActivity(intent)
        }
        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = buyerAdapter
        }
    }

    private fun loadUsers() {
        mDatabase.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: ""
                    val fullname = userSnapshot.child("fullname").getValue(String::class.java) ?: ""
                    val role = userSnapshot.child("role").getValue(String::class.java) ?: ""

                    if (role == "Pembeli") {
                        val user = UserOrder(userId, fullname)
                        loadOrderCount(user)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("OrderFragment", "Database error: ${error.message}")
            }
        })
    }

    private fun loadOrderCount(user: UserOrder) {
        mDatabase.child("orders").child(user.userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orderCount = snapshot.childrenCount.toInt()
                if (orderCount > 0) {
                    user.orderCount = orderCount
                    userList.add(user)
                    buyerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("OrderFragment", "Database error: ${error.message}")
            }
        })
    }
}