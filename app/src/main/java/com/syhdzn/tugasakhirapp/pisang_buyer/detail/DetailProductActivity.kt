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
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.ActivityDetailProductBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.CustomerViewModelFactory
import com.syhdzn.tugasakhirapp.pisang_buyer.cart.CartFragment
import com.syhdzn.tugasakhirapp.pisang_buyer.chat.ChatActivity
import com.syhdzn.tugasakhirapp.chat.data.ChatRoom
import com.syhdzn.tugasakhirapp.pisang_buyer.UserUtils
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
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private var fullName: String? = null

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        firebaseRef = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance().reference

        currentUserId = getUserIdFromPreferences()

        val idbarang = intent.getStringExtra("ID")
        val name = intent.getStringExtra("NAME")
        val price = intent.getDoubleExtra("PRICE", 0.0)
        val quality = intent.getStringExtra("QUALITY")
        val weight = intent.getIntExtra("WEIGHT", 0)
        val imgUri = intent.getStringExtra("IMG")
        fullName = intent.getStringExtra("FULL_NAME") // Terima fullname dari Intent

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
            startActivity(Intent(this, BuyerDashboardActivity::class.java).apply {
                putExtra("switchToFragment", "CartFragment")
                putExtra("selectMenuItem", R.id.cart)
            })
        }
        binding.back.setOnClickListener {
            onBackPressed()
        }
        binding.chat.setOnClickListener {
            Log.d("DetailActivity", "Chat button clicked")
            if (idbarang != null) {
                name?.let { it1 ->
                    Log.d("DetailActivity", "Navigating to chat with idbarang: $idbarang")
                    navigateToChat(idbarang)
                }
            } else {
                Log.d("DetailActivity", "idbarang is null")
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
        val image = intent.getStringExtra("IMG") ?: ""
        val name = intent.getStringExtra("NAME") ?: ""
        val weight = intent.getIntExtra("WEIGHT", 0)
        val price = intent.getDoubleExtra("PRICE", 0.0)

        // Gunakan fullname yang sudah diterima dari Intent
        fullName?.let { userName ->
            checkChatRoomExistence(receiverId) { chatRoomId ->
                if (chatRoomId.isNotEmpty()) {
                    openChatActivity(chatRoomId, image, name, weight, price, userName)
                } else {
                    createNewChatRoom(userId, receiverId, image, name, weight, price, userName)
                }
            }
        }
    }

    private fun checkChatRoomExistence(receiverId: String, callback: (String) -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val chatRoomQuery1 = "${currentUserId}_$receiverId"
        val chatRoomQuery2 = "${receiverId}_$currentUserId"

        Log.d("DetailActivity", "Checking chat room existence for queries: $chatRoomQuery1, $chatRoomQuery2")

        firebaseRef.child("chats")
            .orderByChild("senderId_receiverId").equalTo(chatRoomQuery1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val chatRoomId = childSnapshot.key ?: ""
                            if (chatRoomId.isNotEmpty()) {
                                Log.d("DetailActivity", "Chat room exists: $chatRoomId")
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
                                                Log.d("DetailActivity", "Chat room exists: $chatRoomId")
                                                callback(chatRoomId)
                                                return
                                            }
                                        }
                                    }
                                    Log.d("DetailActivity", "No chat room found")
                                    callback("") // No chat room found
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.d("DetailActivity", "Database error: ${error.message}")
                                    callback("") // Error handling
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("DetailActivity", "Database error: ${error.message}")
                    callback("") // Error handling
                }
            })
    }

    private fun createNewChatRoom(userId: String, receiverId: String, image: String, name: String, weight: Int, price: Double, userName: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatRoomId = firebaseRef.child("chats").push().key

        if (chatRoomId != null) {
            val chatRoom = ChatRoom(
                chatRoomId = chatRoomId,
                senderId = currentUserId,
                receiverId = receiverId,
                senderId_receiverId = "${currentUserId}_$receiverId",  // Field for combination
                userName = userName // Add userName here
            )
            Log.d("DetailActivity", "Creating chat room: $chatRoom")
            firebaseRef.child("chats").child(chatRoomId).setValue(chatRoom).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DetailActivity", "Chat room created successfully")
                    openChatActivity(chatRoomId, image, name, weight, price, userName)
                } else {
                    Log.e("DetailActivity", "Failed to create chat room", task.exception)
                }
            }
        } else {
            Log.e("DetailActivity", "Failed to generate chat room ID")
        }
    }

    private fun openChatActivity(chatRoomId: String, image: String, name: String, weight: Int, price: Double, userName: String) {
        val intent = Intent(this, ChatActivity::class.java).apply {
            putExtra("CHAT_ROOM_ID", chatRoomId)
            putExtra("IMG", image)
            putExtra("NAME", name)
            putExtra("FULL_NAME", userName)
            putExtra("WEIGHT", weight)
            putExtra("PRICE", price)
        }
        startActivity(intent)
    }
}
