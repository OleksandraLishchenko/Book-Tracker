package com.example.book_tracker

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.book_detail_page)

        val bookId = intent.getStringExtra("bookId")
        val listId = intent.getStringExtra("listId")

        Log.d("BookDetailActivity", "Received bookId: $bookId, listId: $listId")

        if (bookId != null && listId != null) {
            loadBookDetails(listId, bookId)
        } else {
            Toast.makeText(this, "Book not found", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<ImageButton>(R.id.backBtn).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<Button>(R.id.editBookButton).setOnClickListener {
            val intent = Intent(this, EditBookActivity::class.java)
            intent.putExtra("bookId", bookId)
            intent.putExtra("listId", listId)
            startActivity(intent)
        }
    }

    private fun loadBookDetails(listId: String, bookId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Lists").child(listId).child(bookId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookTitleTv = findViewById<TextView>(R.id.bookTitleTv)
                val bookAuthorTv = findViewById<TextView>(R.id.bookAuthorTv)
                val bookGenreTv = findViewById<TextView>(R.id.bookGenreTv)
                val bookNotesTv = findViewById<TextView>(R.id.bookNotesTv)
                val bookCoverIv = findViewById<ImageView>(R.id.bookCoverDetailIv)

                if (snapshot.exists()) {
                    val title = snapshot.child("title").value.toString()
                    val author = snapshot.child("author").value.toString()
                    val genre = snapshot.child("genre").value.toString()
                    val notes = snapshot.child("notes").value.toString()
                    val coverBase64 = snapshot.child("cover").value.toString()

                    bookTitleTv.text = title
                    bookAuthorTv.text = author
                    bookGenreTv.text = genre
                    bookNotesTv.text = notes

                    if (coverBase64.isNotEmpty()) {
                        val decodedBytes = Base64.decode(coverBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        bookCoverIv.setImageBitmap(bitmap)
                    }
                } else {
                    Toast.makeText(this@BookDetailActivity, "Book details not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("BookDetailActivity", "Failed to load book details: ${error.message}")
                Toast.makeText(this@BookDetailActivity, "Failed to load details", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

