package com.example.book_tracker

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.book_tracker.databinding.EditBookPageBinding
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class EditBookActivity : AppCompatActivity() {
    private lateinit var binding: EditBookPageBinding
    private lateinit var progressDialog: ProgressDialog
    private var bookCoverBase64: String? = null
    private var bookId: String? = null
    private var listId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditBookPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        bookId = intent.getStringExtra("bookId")
        listId = intent.getStringExtra("listId")

        loadExistingBookDetails()


        binding.uploadCoverBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 1000)
        }

        binding.saveChangesBtn.setOnClickListener {
            saveChangesToFirebase()
        }

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadExistingBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Lists").child(listId!!).child(bookId!!)
        ref.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                binding.bookTitleEditText.setText(snapshot.child("title").value.toString())
                binding.bookAuthorEditText.setText(snapshot.child("author").value.toString())
                binding.bookGenreEditText.setText(snapshot.child("genre").value.toString())
                binding.bookNotesEditText.setText(snapshot.child("notes").value.toString())

                val coverBase64 = snapshot.child("cover").value.toString()
                if (coverBase64.isNotEmpty()) {
                    val decodedBytes = Base64.decode(coverBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    binding.bookCoverIv.setImageBitmap(bitmap)
                    bookCoverBase64 = coverBase64
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
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

    private fun saveChangesToFirebase() {
        progressDialog.show()
        val updatedData = mutableMapOf<String, Any>()

        val newTitle = binding.bookTitleEditText.text.toString().trim()
        if (newTitle.isNotEmpty()) updatedData["title"] = newTitle

        val newAuthor = binding.bookAuthorEditText.text.toString().trim()
        if (newAuthor.isNotEmpty()) updatedData["author"] = newAuthor

        val newGenre = binding.bookGenreEditText.text.toString().trim()
        if (newGenre.isNotEmpty()) updatedData["genre"] = newGenre

        val newNotes = binding.bookNotesEditText.text.toString().trim()
        if (newNotes.isNotEmpty()) updatedData["notes"] = newNotes

        if (bookCoverBase64 != null) updatedData["cover"] = bookCoverBase64!!

        if (updatedData.isEmpty()) {
            progressDialog.dismiss()
            Toast.makeText(this, "No changes to save", Toast.LENGTH_SHORT).show()
            return
        }

        val ref = FirebaseDatabase.getInstance().getReference("Lists").child(listId!!).child(bookId!!)
        ref.updateChildren(updatedData)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Book details updated successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update book: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
