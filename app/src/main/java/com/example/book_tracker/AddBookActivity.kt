package com.example.book_tracker

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.book_tracker.databinding.AddBookPageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class AddBookActivity : AppCompatActivity() {
    private lateinit var binding: AddBookPageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var progressDialog: ProgressDialog
    private lateinit var listNames: ArrayList<String>
    private lateinit var listIds: ArrayList<String>

    private var selectedListId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AddBookPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        loadLists()

        binding.submitButton.setOnClickListener {
            validateData()
        }

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadLists() {
        listNames = ArrayList()
        listIds = ArrayList()
        val currentUserUid = auth.currentUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("Lists")

        ref.orderByChild("uid").equalTo(currentUserUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val listName = ds.child("list").value.toString()
                        val listId = ds.child("id").value.toString()
                        listNames.add(listName)
                        listIds.add(listId)
                    }
                    val adapter = ArrayAdapter(
                        this@AddBookActivity,
                        android.R.layout.simple_spinner_dropdown_item,
                        listNames
                    )
                    binding.listSpinner.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AddBookActivity, "Failed to load lists", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun validateData() {
        val title = binding.bookTitleEditText.text.toString().trim()
        val author = binding.bookAuthorEditText.text.toString().trim()
        val genre = binding.bookGenreEditText.text.toString().trim()
        val notes = binding.bookNotesEditText.text.toString().trim()
        selectedListId = listIds[binding.listSpinner.selectedItemPosition]

        if (title.isEmpty() || author.isEmpty() || genre.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
        } else {
            addBookToFirebase(title, author, genre, notes)
        }
    }

    private fun addBookToFirebase(title: String, author: String, genre: String, notes: String) {
        progressDialog.show()
        val timestamp = System.currentTimeMillis()
        val bookData = HashMap<String, Any>()
        bookData["title"] = title
        bookData["author"] = author
        bookData["genre"] = genre
        bookData["notes"] = notes
        bookData["id"] = "$timestamp"
        bookData["listId"] = selectedListId
        bookData["uid"] = "${auth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Lists").child(selectedListId)
        ref.child("$timestamp")
            .setValue(bookData)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Book added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add book: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
