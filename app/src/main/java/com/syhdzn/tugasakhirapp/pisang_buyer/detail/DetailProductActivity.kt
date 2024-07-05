package com.syhdzn.tugasakhirapp.pisang_buyer.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.syhdzn.tugasakhirapp.databinding.ActivityDetailProductBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.CustomerViewModelFactory

import com.syhdzn.tugasakhirapp.pisang_buyer.cart.CartFragment
import com.syhdzn.tugasakhirapp.chat.ChatActivity
import com.syhdzn.tugasakhirapp.chat.data.ChatRoom
import com.syhdzn.tugasakhirapp.pisang_buyer.dashboard.BuyerDashboardActivity
import com.syhdzn.tugasakhirapp.pisang_buyer.data.local.CartEntity
import com.syhdzn.tugasakhirapp.pisang_buyer.payment.PaymentActivity
import java.text.NumberFormat
import java.util.Currency

class DetailProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailProductBinding
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var viewModel: DetailViewModel
    private lateinit var currentUserId: String

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        firebaseRef = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        currentUserId = getUserIdFromPreferences()

        val idbarang = intent.getStringExtra("ID")
        val name = intent.getStringExtra("NAME")
        val price = intent.getDoubleExtra("PRICE", 0.0)
        val quality = intent.getStringExtra("QUALITY")
        val weight = intent.getIntExtra("WEIGHT", 0)
        val imgUri = intent.getStringExtra("IMG")
        val formattedPrice = formatPrice(price.toFloat())

        binding.tvProductNameDetail.text = name
        binding.tvProductPriceDetail.text = formattedPrice
        binding.tvProductQualityDetail.text = quality
        binding.tvProductWeightDetail.text = weight.toString()
        Picasso.get().load(imgUri).into(binding.ivProductImageDetail)

        val factory = CustomerViewModelFactory(application)
        viewModel = ViewModelProvider(this, factory).get(DetailViewModel::class.java)

        binding.buttonCart.setOnClickListener {
            val cartEntity = CartEntity(
                name = name ?: "",
                price = price,
                idbarang = idbarang ?: "",
                imageUrl = imgUri ?: "",
                amount = 1
            )
            viewModel.addToCart(cartEntity)
            Toast.makeText(this, "Berhasil menambahkan ke keranjang", Toast.LENGTH_SHORT).show()
        }
        binding.cart.setOnClickListener {
            val intent = Intent(this, CartFragment::class.java)
            startActivity(intent)
        }
        binding.back.setOnClickListener {
            startActivity(Intent(this, BuyerDashboardActivity::class.java))
        }
        binding.chat.setOnClickListener {
            if (idbarang != null) {
                navigateToChat(idbarang)
            }
        }

        binding.buttonOrder.setOnClickListener {
            val cartEntity = CartEntity(
                name = name ?: "",
                price = price,
                idbarang = idbarang ?: "",
                imageUrl = imgUri ?: "",
                amount = 1
            )
            navigateToPayment(cartEntity)
        }
    }

    private fun setupWindowInsets() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun formatPrice(price: Float): String {
        val format = NumberFormat.getCurrencyInstance()
        format.maximumFractionDigits = 0
        format.currency = Currency.getInstance("IDR")
        return format.format(price)
    }

    private fun navigateToPayment(cartEntity: CartEntity) {
        val intent = Intent(this, PaymentActivity::class.java)
        intent.putParcelableArrayListExtra("CART_ITEMS", arrayListOf(cartEntity))
        intent.putExtra("USER_ID", currentUserId)
        startActivity(intent)
    }

    private fun getUserIdFromPreferences(): String {
        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
        return sharedPreferences.getString("USER_ID", "") ?: ""
    }

    private fun navigateToChat(receiverId: String) {
        val userId = getUserIdFromPreferences()
        Log.d("DetailActivity", "Navigating to chat with ID: $userId")

        checkChatRoomExistence(userId, receiverId) { chatRoomId ->
            if (chatRoomId.isNotEmpty()) {
                openChatActivity(chatRoomId)
            } else {
                createNewChatRoom(userId, receiverId)
            }
        }
    }


    private fun checkChatRoomExistence(userId: String, receiverId: String, callback: (String) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val chatRoomQuery1 = "${currentUserId}_$receiverId"
        val chatRoomQuery2 = "${receiverId}_$currentUserId"

        firebaseRef.child("chats")
            .orderByChild("senderId_receiverId").equalTo(chatRoomQuery1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val chatRoomId = childSnapshot.key ?: ""
                            if (chatRoomId.isNotEmpty()) {
                                callback(chatRoomId)
                                return
                            }
                        }
                    } else {
                        firebaseRef.child("chats")
                            .orderByChild("senderId_receiverId").equalTo(chatRoomQuery2)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        for (childSnapshot in snapshot.children) {
                                            val chatRoomId = childSnapshot.key ?: ""
                                            if (chatRoomId.isNotEmpty()) {
                                                callback(chatRoomId)
                                                return
                                            }
                                        }
                                    }
                                    callback("") // No chat room found
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    callback("") // Error handling
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback("") // Error handling
                }
            })
    }


    private fun createNewChatRoom(userId: String, receiverId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val chatRoomId = firebaseRef.child("chats").push().key

        if (chatRoomId != null) {
            val chatRoom = ChatRoom(
                chatRoomId = chatRoomId,
                senderId = currentUserId,
                receiverId = receiverId,
                senderId_receiverId = "${currentUserId}_$receiverId"  // Field for combination
            )
            firebaseRef.child("chats").child(chatRoomId).setValue(chatRoom).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    openChatActivity(chatRoomId)
                } else {
                    Log.e("DetailActivity", "Failed to create chat room", task.exception)
                }
            }
        } else {
            Log.e("DetailActivity", "Failed to generate chat room ID")
        }
    }


    private fun openChatActivity(chatRoomId: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("CHAT_ROOM_ID", chatRoomId)
        startActivity(intent)
    }

//    private fun getUserNameFromPreferences(callback: (String) -> Unit) {
//        val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
//        val userId = sharedPreferences.getString("USER_ID", "") ?: return
//
//        // Ambil data pengguna berdasarkan USER_ID
//        firebaseRef.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()) {
//                    val fullname = snapshot.child("fullname").getValue(String::class.java)
//                    if (fullname != null) {
//                        callback(fullname)
//                    } else {
//                        callback("")  // Atau nilai default lain jika fullname tidak ada
//                    }
//                } else {
//                    callback("")  // Atau nilai default lain jika user tidak ditemukan
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle error
//                callback("")  // Atau nilai default lain jika terjadi error
//            }
//        })
//    }

}


