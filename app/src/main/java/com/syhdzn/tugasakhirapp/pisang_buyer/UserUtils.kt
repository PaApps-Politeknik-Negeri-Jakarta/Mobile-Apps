package com.syhdzn.tugasakhirapp.pisang_buyer

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class UserUtils {
    data class User(val fullname: String = "")

    companion object {
        fun loadUserData(mAuth: FirebaseAuth, mDatabase: DatabaseReference, callback: (String?) -> Unit) {
            val currentUser = mAuth.currentUser
            if (currentUser != null) {
                val uid = currentUser.uid
                mDatabase.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val user = snapshot.getValue(User::class.java)
                            if (user != null) {
                                val fullname = user.fullname
                                callback(fullname)
                            } else {
                                callback(null)
                            }
                        } else {
                            callback(null)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d("UserUtils", "Database error: ${error.message}")
                        callback(null)
                    }
                })
            } else {
                Log.d("UserUtils", "Current user is null")
                callback(null)
            }
        }
    }
}

