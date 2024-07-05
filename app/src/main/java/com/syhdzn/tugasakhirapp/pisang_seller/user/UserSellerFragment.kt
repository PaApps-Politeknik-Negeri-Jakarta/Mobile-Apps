package com.syhdzn.tugasakhirapp.pisang_seller.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.FragmentUserSellerBinding
import com.syhdzn.tugasakhirapp.login.LoginActivity

class UserSellerFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference
    private lateinit var binding: FragmentUserSellerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAction()
        loadUserData()
    }

    private fun setupAction() {
        binding.btnLogout.setOnClickListener {
            showDialogLogout()
        }
    }

    data class User(val fullname: String = "")

    private fun loadUserData() {
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            mDatabase.child("users").child(uid).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        binding.tvNama.text = user.fullname
                    } else {
                        Log.d("UserFragment", "User data is null")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("UserFragment", "Database error: ${error.message}")
                }
            })
        } else {
            Log.d("UserFragment", "Current user is null")
        }
    }

    private fun showDialogLogout() {
        val customDialogView = layoutInflater.inflate(R.layout.costum_dialog_logout, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(customDialogView)
            .create()

        customDialogView.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            logOut()
            requireActivity().finish()
            dialog.dismiss()
        }

        customDialogView.findViewById<Button>(R.id.btn_no).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_3)
        customDialogView.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.anim))
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }


    private fun logOut() {
        mAuth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
