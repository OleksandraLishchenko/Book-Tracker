package com.example.book_tracker

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.book_tracker.databinding.ForgotPasswordPageBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var binding: ForgotPasswordPageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ForgotPasswordPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        // Set onClickListener for the reset password button
        binding.resetPasswordButton.setOnClickListener {
            validateInput()
        }
    }

    // Validate the email input
    private fun validateInput() {
        val email = binding.resetEmailEditText.text.toString().trim()

        // Check if the email field is not empty
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email address", Toast.LENGTH_SHORT).show()
        } else {
            sendPasswordResetEmail(email)
        }
    }

    // Send the password reset email through Firebase
    private fun sendPasswordResetEmail(email: String) {
        progressDialog.setMessage("Sending password reset email...")
        progressDialog.show()

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                finish() // Close this activity and return to login screen
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to send reset email: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
