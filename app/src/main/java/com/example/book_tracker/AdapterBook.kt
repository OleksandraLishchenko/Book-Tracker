package com.example.book_tracker

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.book_tracker.databinding.RowBookBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterBook(
    private val context: Context,
    private var bookList: ArrayList<ModelBook>,
    private val listNames: ArrayList<String>,
    private val listIds: ArrayList<String>
) : RecyclerView.Adapter<AdapterBook.HolderBook>() {
    inner class HolderBook(val binding: RowBookBinding) : RecyclerView.ViewHolder(binding.root) {
        val bookTitle: TextView = binding.bookTitleTv
        val bookAuthor: TextView = binding.bookAuthorTv
        val moveBtn: Button = binding.moveBookBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderBook {
        val binding = RowBookBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderBook(binding)
    }

    override fun onBindViewHolder(holder: HolderBook, position: Int) {
        val model = bookList[position]
        holder.bookTitle.text = model.title
        holder.bookAuthor.text = model.author

        holder.moveBtn.setOnClickListener {
            showMoveDialog(model)
        }
    }

    override fun getItemCount(): Int = bookList.size

    private fun showMoveDialog(book: ModelBook) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Move Book")
        val spinner = Spinner(context)
        val adapter =
            ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listNames)
        spinner.adapter = adapter
        builder.setView(spinner)
        builder.setPositiveButton("Move") { _, _ ->
            val newListId = listIds[spinner.selectedItemPosition]
            moveBookToNewList(book, newListId)
        }
        builder.setNegativeButton("Cancel", null)

        builder.create().show()
    }

    private fun moveBookToNewList(book: ModelBook, newListId: String) {
        val oldRef =
            FirebaseDatabase.getInstance().getReference("Books").child(book.listId).child(book.id)
        oldRef.removeValue()
        val newRef = FirebaseDatabase.getInstance().getReference("Books").child(newListId)
        book.listId = newListId  // Update book's listId
        newRef.child(book.id).setValue(book)
            .addOnSuccessListener {
                Toast.makeText(context, "Book moved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to move book: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }
}
