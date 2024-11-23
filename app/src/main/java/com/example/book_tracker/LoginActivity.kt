package com.example.book_tracker
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)
        auth = FirebaseAuth.getInstance()
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        findViewById<Button>(R.id.next_button).setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() && password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show()
            } else if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show()
            } else if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show()
            } else {

                login(email, password)
            }
        }
        findViewById<TextView>(R.id.register).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))

        }
        findViewById<TextView>(R.id.ForgotPassword).setOnClickListener {
            startActivity(Intent(this, ForgotPassword::class.java))
        }
    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LibraryActivity::class.java))
                } else {
                    when (val exception = task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(this, "Incorrect password or email. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this, "Login Failed: ${exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
    }
}

