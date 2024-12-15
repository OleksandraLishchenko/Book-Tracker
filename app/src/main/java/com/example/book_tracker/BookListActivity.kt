package com.example.book_tracker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.book_tracker.databinding.BookListPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log

class BookListActivity : AppCompatActivity() {
    private lateinit var binding: BookListPageBinding
    private lateinit var bookList: ArrayList<ModelBook>
    private lateinit var adapterBook: AdapterBook
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BookListPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()


        val listId = intent.getStringExtra("listId")
        val listName = intent.getStringExtra("listName")

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterBook.filter.filter(s)
                } catch (e: Exception) {

                }
            }
            override fun afterTextChanged(s: Editable?) {

            }


        })

        title = listName

        bookList = ArrayList()
        adapterBook = AdapterBook(this, bookList)
        binding.booksRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.booksRecyclerView.adapter = adapterBook
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterBook.filter.filter(s)
                } catch (e: Exception) {

                }
            }
            override fun afterTextChanged(s: Editable?) {

            }


        })
        if (listId != null) {
            loadBooks(listId)
        } else {
            Toast.makeText(this, "List ID not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }


    private fun loadBooks(listId: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Lists").child(listId)

        Log.d("BookListActivity", "Loading books for listId: $listId")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookList.clear()

                if (snapshot.exists()) {
                    Log.d("BookListActivity", "Snapshot exists. Number of children: ${snapshot.childrenCount}")

                    for (ds in snapshot.children) {
                        val bookId = ds.child("id").getValue(String::class.java) ?: ""
                        val title = ds.child("title").getValue(String::class.java) ?: ""
                        val author = ds.child("author").getValue(String::class.java) ?: ""
                        val genre = ds.child("genre").getValue(String::class.java) ?: ""
                        val notes = ds.child("notes").getValue(String::class.java) ?: ""
                        val listId = ds.child("listId").getValue(String::class.java) ?: ""
                        val uid = ds.child("uid").getValue(String::class.java) ?: ""

                        val book = ModelBook(
                            id = bookId,
                            title = title,
                            author = author,
                            genre = genre,
                            notes = notes,
                            listId = listId,
                            uid = uid
                        )

                        bookList.add(book)

                        Log.d("BookListActivity", "Fetched Book: $title, Author: $author")
                    }

                    adapterBook.notifyDataSetChanged()

                    if (bookList.isEmpty()) {
                        Toast.makeText(this@BookListActivity, "No books found in this list.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@BookListActivity, "No books found in this list.", Toast.LENGTH_SHORT).show()
                    Log.d("BookListActivity", "Snapshot does not exist.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("BookListActivity", "Failed to load books: ${error.message}")
                Toast.makeText(this@BookListActivity, "Failed to load books: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}
