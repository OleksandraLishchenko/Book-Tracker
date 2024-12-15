package com.example.book_tracker

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
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
import java.io.ByteArrayOutputStream

class AddBookActivity : AppCompatActivity() {
    private lateinit var binding: AddBookPageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var progressDialog: ProgressDialog
    private lateinit var listNames: ArrayList<String>
    private lateinit var listIds: ArrayList<String>
    private var bookCoverBase64: String? = null

    private var selectedListId = ""
    private val IMAGE_PICK_CODE = 1000

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

        binding.uploadCoverBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

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
        val bookData = hashMapOf(
            "title" to title,
            "author" to author,
            "genre" to genre,
            "notes" to notes,
            "cover" to (bookCoverBase64 ?: ""),
            "id" to "$timestamp",
            "listId" to selectedListId,
            "uid" to auth.uid!!
        )

        val ref = database.child("Lists").child(selectedListId).child("$timestamp")
        ref.setValue(bookData)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Book added successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add book: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            binding.bookCoverIv.setImageBitmap(bitmap)
            bookCoverBase64 = encodeImageToBase64(bitmap)
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
