package com.example.book_tracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logo_page)
        Handler(Looper.getMainLooper()).postDelayed({
            setContentView(R.layout.welcome_page)
            findViewById<Button>(R.id.login).setOnClickListener {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            findViewById<Button>(R.id.sign_up).setOnClickListener {
                startActivity(Intent(this, SignUpActivity::class.java))
            }
        }, 5000)
    }
}
