package com.syhdzn.tugasakhirapp.pisang_seller.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.syhdzn.tugasakhirapp.R
import com.syhdzn.tugasakhirapp.databinding.FragmentUserSellerBinding
import com.syhdzn.tugasakhirapp.login.LoginActivity

class UserSellerFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentUserSellerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
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
    }

    private fun setupAction() {
        binding.btnLogout.setOnClickListener {
            showDialogLogout()
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
        auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
