package com.syhdzn.tugasakhirapp.pisang_seller.add

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.FragmentAddBinding
import com.syhdzn.tugasakhirapp.pisang_buyer.data.Product

class AddFragment : Fragment() {
    private lateinit var binding: FragmentAddBinding
    private lateinit var productAdapter: AddProductAdapter
    private lateinit var productList: ArrayList<Product>
    private lateinit var firebaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseRef = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        productList = arrayListOf()

        setupRecyclerView()
        fetchData()
        setupAction()
    }

    private fun setupRecyclerView() {
        productAdapter = AddProductAdapter(productList, this::showDeleteDialog)
        binding.rvAddProduct.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productAdapter
        }
    }

    private fun fetchData() {
        firebaseRef.child("product").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                snapshot.children.mapNotNullTo(productList) { it.getValue(Product::class.java) }
                productAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupAction() {
        binding.btnadd.setOnClickListener {
            val intent = Intent(requireContext(), ProcessAddActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showDeleteDialog(productId: String, position: Int) {
        val customDialogView = LayoutInflater.from(context).inflate(R.layout.custom_dialog_confirm_add, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(customDialogView)
            .create()

        customDialogView.findViewById<Button>(R.id.btn_yes).setOnClickListener {
            dialog.dismiss()
            deleteProductFromFirebase(productId, position)
        }

        customDialogView.findViewById<Button>(R.id.btn_no).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(R.drawable.bg_rounded_3)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    private fun deleteProductFromFirebase(productId: String, position: Int) {
        val databaseRef = FirebaseDatabase.getInstance("https://tugasakhirapp-c5669-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("product").child(productId)
        databaseRef.removeValue().addOnSuccessListener {
            Toast.makeText(context, "Product deleted successfully", Toast.LENGTH_SHORT).show()
            if (position >= 0 && position < productList.size) {
                productList.removeAt(position)
                productAdapter.notifyItemRemoved(position)
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Failed to delete product: ${exception.message}", Toast.LENGTH_SHORT).show()
            Log.e("AddFragment", "Failed to delete product", exception)
        }
    }
}
