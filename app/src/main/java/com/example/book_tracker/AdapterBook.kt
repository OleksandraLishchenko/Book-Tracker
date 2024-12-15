package com.example.book_tracker

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.book_tracker.databinding.RowBookBinding
import com.example.book_tracker.databinding.RowListBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AdapterBook(
    private val context: Context,
    public var bookList: ArrayList<ModelBook>
) : RecyclerView.Adapter<AdapterBook.HolderBook>(),Filterable {
    private var filterBook: ArrayList<ModelBook>
    private var filter: FilterBook? = null
    private lateinit var binding: RowBookBinding
    init {
        this.filterBook = bookList
    }

    inner class HolderBook(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookTitle: TextView = binding.bookTitleTv
        val bookAuthor: TextView = binding.bookAuthorTv
        val moveBtn: Button = binding.moveBookBtn
        val deleteBtn: ImageButton = binding.deleteBtn
        val cardView: CardView = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderBook {
        binding = RowBookBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderBook(binding.root)
    }

    override fun onBindViewHolder(holder: HolderBook, position: Int) {
        val model = bookList[position]
        if (model.title.isEmpty() || model.author.isEmpty()) {
            holder.itemView.visibility = View.GONE
            return
        }
        if (model != null) {
            holder.bookTitle.text = model.title
            holder.bookAuthor.text = model.author
            holder.cardView.visibility = View.VISIBLE
            holder.itemView.setOnClickListener {
                val intent = Intent(context, BookDetailActivity::class.java)
                intent.putExtra("bookId", model.id)
                intent.putExtra("listId", model.listId)
                context.startActivity(intent)
            }

            holder.moveBtn.setOnClickListener {
                showMoveDialog(model)
            }
        } else {
            View.GONE.also { holder.cardView.visibility = it }
        }
        holder.deleteBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete Book")
                .setMessage("Are you sure you want to delete this book?")
                .setPositiveButton("Confirm") { _, _ ->
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
                    deleteBook(model, position)
                }
                .setNeutralButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }

    }
    override fun getItemCount(): Int {
        return bookList.size
    }

    private fun deleteBook(model: ModelBook, position: Int) {
        val ref = FirebaseDatabase.getInstance().getReference("Lists").child(model.listId)
        ref.child(model.id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Book deleted successfully", Toast.LENGTH_SHORT).show()
                bookList.removeAt(position)
                notifyItemRemoved(position)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showMoveDialog(book: ModelBook) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Move Book")
        val spinner = Spinner(context)

        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

        val ref = FirebaseDatabase.getInstance().getReference("Lists")
        val listNames = ArrayList<String>()
        val listIds = ArrayList<String>()

        ref.orderByChild("uid").equalTo(currentUserUid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val listName = ds.child("list").value.toString()
                    val listId = ds.child("id").value.toString()
                    listNames.add(listName)
                    listIds.add(listId)
                }

                val adapter =
                    ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, listNames)
                spinner.adapter = adapter

                builder.setView(spinner)
                builder.setPositiveButton("Move") { _, _ ->
                    val selectedPosition = spinner.selectedItemPosition
                    if (selectedPosition != -1) {
                        val newListId = listIds[selectedPosition]
                        moveBookToNewList(book, newListId)
                    } else {
                        Toast.makeText(context, "No list selected", Toast.LENGTH_SHORT).show()
                    }
                }
                builder.setNegativeButton("Cancel", null)
                builder.create().show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load lists", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun moveBookToNewList(book: ModelBook, newListId: String) {
        val database = FirebaseDatabase.getInstance()
        val oldRef = database.getReference("Lists").child(book.listId).child(book.id)
        val newRef = database.getReference("Lists").child(newListId)

        oldRef.removeValue().addOnSuccessListener {

            book.listId = newListId

            newRef.child(book.id).setValue(book).addOnSuccessListener {
                Toast.makeText(context, "Book moved successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Failed to add book to new list: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(context, "Failed to remove book from old list: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterBook(filterBook, this)
        }
        return filter as FilterBook
    }
}

