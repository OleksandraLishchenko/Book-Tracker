package com.example.book_tracker

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.book_tracker.databinding.AddlistPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class AddListActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: AddlistPageBinding
    private lateinit var database: DatabaseReference
    private lateinit var progressDialog: ProgressDialog
    private var list = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddlistPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.submit.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        list = binding.listEditText.text.toString().trim()

        if (list.isEmpty()) {
            Toast.makeText(this, "Enter List...", Toast.LENGTH_SHORT).show()
        } else {
            addListFirebase()
        }
    }

    private fun addListFirebase() {
        progressDialog.show()
        val timestamp = System.currentTimeMillis()
        val currentUser = auth.currentUser
        val email = currentUser?.email ?: "Unknown email"
        val name = currentUser?.displayName ?: "Unknown name"

        val hashMap = HashMap<String, Any>()
        hashMap["name"] = name
        hashMap["email"] = email
        hashMap["id"] = "$timestamp"
        hashMap["list"] = list
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${auth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Lists")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Added successfully...", Toast.LENGTH_SHORT).show()
                finish()

            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
