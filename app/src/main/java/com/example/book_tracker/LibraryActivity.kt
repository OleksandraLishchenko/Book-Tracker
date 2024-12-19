package com.example.book_tracker

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.book_tracker.databinding.LibraryPageBinding
import com.google.common.collect.Lists
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineStart
import android.util.Base64


class LibraryActivity : AppCompatActivity() {
    private lateinit var binding: LibraryPageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var listArrayList: ArrayList<ModelList>
    private lateinit var adapterList: AdapterList


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LibraryPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.Addbook.setOnClickListener {
            startActivity(Intent(this, AddBookActivity::class.java))
        }
        auth = FirebaseAuth.getInstance()

        loadLists()
        loadUserProfilePhoto()
        addDynamicPhotoListener()

        val userPhotoButton = findViewById<ImageButton>(R.id.UserPhoto1)
        userPhotoButton.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }



        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterList.filter.filter(s)
                } catch (e: Exception) {

                }
            }
            override fun afterTextChanged(s: Editable?) {

            }


        })
        binding.addlist.setOnClickListener {
            val intent = Intent(this, AddListActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserProfilePhoto() {
        val user = auth.currentUser
        val userId = user?.uid ?: return

        val photoRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("profilePhoto")
        photoRef.get().addOnSuccessListener {
            val base64Photo = it.value as? String
            if (base64Photo != null) {
                val decodedBytes = Base64.decode(base64Photo, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                val userPhotoButton = findViewById<ImageButton>(R.id.UserPhoto1)
                userPhotoButton.setImageBitmap(bitmap)
                userPhotoButton.clipToOutline = true
                userPhotoButton.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load profile photo.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLists() {
        listArrayList = ArrayList()
        val currentUserUid = auth.currentUser?.uid

        if (currentUserUid != null) {
            val ref = FirebaseDatabase.getInstance().getReference("Lists")
            ref.orderByChild("uid").equalTo(currentUserUid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        listArrayList.clear()
                        for (ds in snapshot.children) {
                            val model = ds.getValue(ModelList::class.java)
                            model?.let { listArrayList.add(it) }
                        }
                        adapterList = AdapterList(this@LibraryActivity, listArrayList)
                        binding.lists.adapter = adapterList
                    }
                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }


    private fun moveBookToList(bookId: String, oldListId: String, newListId: String) {
        val refOld =
            FirebaseDatabase.getInstance().getReference("Books").child(oldListId).child(bookId)
        refOld.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val bookData = snapshot.value as HashMap<String, Any>
                refOld.removeValue()
                val refNew = FirebaseDatabase.getInstance().getReference("Books").child(newListId)
                refNew.child(bookId).setValue(bookData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@LibraryActivity,
                            "Book moved successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this@LibraryActivity,
                            "Failed to move book: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@LibraryActivity,
                    "Operation cancelled",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }


        })
    }
    private fun addDynamicPhotoListener() {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        val photoRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            .child("profilePhoto")
        photoRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val base64Photo = snapshot.value as? String
                if (base64Photo != null) {
                    val decodedBytes = Base64.decode(base64Photo, Base64.DEFAULT)
                    val bitmap =
                        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    val userPhotoButton = findViewById<ImageButton>(R.id.UserPhoto1)
                    userPhotoButton.setImageBitmap(bitmap)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LibraryActivity, "Failed to listen for profile photo updates.", Toast.LENGTH_SHORT).show()
            }


        })
}}