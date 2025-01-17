package com.example.book_tracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChangeEmailActivity : AppCompatActivity() {

    private lateinit var newEmailEditText: EditText
    private lateinit var changeEmailButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_email_page)

        auth = FirebaseAuth.getInstance()

        newEmailEditText = findViewById(R.id.newEmailEditText)
        changeEmailButton = findViewById(R.id.changeEmailButton)
        progressBar = findViewById(R.id.progressBar)

        changeEmailButton.setOnClickListener {
            val newEmail = newEmailEditText.text.toString().trim()
            if (newEmail.isNotEmpty()) {
                changeEmail(newEmail)
            } else {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changeEmail(newEmail: String) {
        val user = auth.currentUser

        if (user != null) {
            progressBar.visibility = View.VISIBLE

            auth.fetchSignInMethodsForEmail(newEmail)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods
                        if (signInMethods.isNullOrEmpty()) {
                            user.updateEmail(newEmail).addOnCompleteListener { updateTask ->
                                progressBar.visibility = View.GONE
                                if (updateTask.isSuccessful) {
                                    updateEmailInDatabase(user.uid, newEmail)
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to update email: ${updateTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            Toast.makeText(
                                this,
                                "This email is already in use. Please try a different one.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(
                            this,
                            "Failed to check email availability: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEmailInDatabase(userId: String, newEmail: String) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        userRef.child("email").setValue(newEmail).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Email updated successfully in database", Toast.LENGTH_SHORT).show()
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Failed to update email in database: ${task.exception?.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
