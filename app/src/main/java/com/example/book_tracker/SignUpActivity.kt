package com.example.book_tracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_page)
        auth = FirebaseAuth.getInstance()
        val fullNameEditText = findViewById<EditText>(R.id.fullNameInput)
        val emailEditText = findViewById<EditText>(R.id.emailInput)
        val passwordEditText = findViewById<EditText>(R.id.passwordInput)
        findViewById<Button>(R.id.next_button).setOnClickListener {
            val fullName = fullNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            when {
                fullName.isEmpty() -> {
                    Toast.makeText(this, "Please enter your full name.", Toast.LENGTH_SHORT).show()
                }
                email.isEmpty() -> {
                    Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show()
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show()
                }
                !isEmailValid(email) -> {
                    Toast.makeText(this, "Invalid email format. Please enter a valid email.", Toast.LENGTH_SHORT).show()
                }
                !isPasswordStrong(password) -> {
                    Toast.makeText(this, "Password must be at least 8 characters long, contain a mix of uppercase, lowercase, digits, and special characters.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    signUp(fullName, email, password)
                }
            }
        }
        findViewById<TextView>(R.id.login).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun isEmailValid(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun isPasswordStrong(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&=+])[A-Za-z\\d@$!%*?&]{6,}$"
        return password.matches(passwordPattern.toRegex())
    }

    private fun signUp(fullName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(this, "Sign-Up Successful!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, LibraryActivity::class.java))
                            }
                        }
                } else {
                    when (val exception = task.exception) {
                        is FirebaseAuthUserCollisionException -> {
                            Toast.makeText(this, "This email is already registered. Please use a different email or log in.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this, "Sign-Up Failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }
}

