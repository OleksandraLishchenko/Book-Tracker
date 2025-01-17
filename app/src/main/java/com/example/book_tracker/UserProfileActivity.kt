package com.example.book_tracker

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream

class UserProfileActivity : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var userPhotoButton: ImageButton
    private lateinit var userPhotoView: ImageView
    private lateinit var userEmailTextView: TextView
    private lateinit var userCreationDateTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var deleteAccountButton: Button
    private lateinit var changeEmailButton: ImageButton

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_profile_page)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        userNameTextView = findViewById(R.id.userName)
        userPhotoButton = findViewById(R.id.uploadPhoto)
        userPhotoView = findViewById(R.id.UserPhoto)
        userEmailTextView = findViewById(R.id.userEmail)
        userCreationDateTextView = findViewById(R.id.userCreationDate)
        logoutButton = findViewById(R.id.logout_button)
        deleteAccountButton = findViewById(R.id.dltacc_button)
        changeEmailButton = findViewById(R.id.changeEmailButton)

        loadUserData()

        userPhotoButton.setOnClickListener {
            chooseImageFromGallery()
        }

        logoutButton.setOnClickListener {
            logOutUser()
        }

        deleteAccountButton.setOnClickListener {
            confirmAndDeleteAccount()
        }

        changeEmailButton.setOnClickListener {
            val intent = Intent(this@UserProfileActivity, ChangeEmailActivity::class.java)
            startActivity(intent)
        }
    }

    private fun logOutUser() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun confirmAndDeleteAccount() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "No user found.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Yes") { _, _ ->
                deleteAccount(user.uid)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteAccount(userId: String) {
        val userRef = database.getReference("Users").child(userId)
        userRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                auth.currentUser?.delete()?.addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to delete account: ${deleteTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Failed to delete user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserData() {
        val user = auth.currentUser
        val userId = user?.uid ?: return

        val userRef = database.getReference("Users").child(userId).child("fullName")
        userRef.get().addOnSuccessListener {
            val fullName = it.value as? String
            userNameTextView.text = fullName ?: "Name not found"
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load user name.", Toast.LENGTH_SHORT).show()
        }

        val photoRef = database.getReference("Users").child(userId).child("profilePhoto")
        photoRef.get().addOnSuccessListener {
            val base64Photo = it.value as? String
            if (base64Photo != null) {
                val decodedBytes = Base64.decode(base64Photo, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                userPhotoView.setImageBitmap(bitmap)
                userPhotoView.clipToOutline = true
            }
        }

        userEmailTextView.text = "Email: ${user?.email ?: "Email not found"}"
        val creationDate = user?.metadata?.creationTimestamp
        if (creationDate != null) {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(creationDate)
            userCreationDateTextView.text = "Account Created: $date"
        } else {
            userCreationDateTextView.text = "Account Created: Not available"
        }
    }

    private fun chooseImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            userPhotoView.setImageBitmap(bitmap)
            userPhotoView.clipToOutline = true

            val base64Image = encodeImageToBase64(bitmap)
            saveUserPhotoToDatabase(base64Image)
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun saveUserPhotoToDatabase(base64Image: String) {
        val user = auth.currentUser
        val userId = user?.uid ?: return
        val userRef = database.getReference("Users").child(userId)

        userRef.child("profilePhoto").setValue(base64Image).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Profile photo updated successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to update profile photo.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
