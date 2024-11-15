package com.example.book_tracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.book_tracker.databinding.RowListBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterList : RecyclerView.Adapter<AdapterList.HolderCategory>, Filterable {
    private val context: Context
    public var listArrayList: ArrayList<ModelList>
    private var filterList: ArrayList<ModelList>
    private var filter: FilterList? = null

    private lateinit var binding: RowListBinding

    constructor(context: Context, listArrayList: ArrayList<ModelList>) {
        this.context = context
        this.listArrayList = listArrayList
        this.filterList = listArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        binding = RowListBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        val model = listArrayList[position]
        val id = model.id
        val list = model.list
        val uid = model.uid
        val timestamp = model.timestamp

        holder.listTv.text = list

        holder.deleteBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete this list?")
                .setPositiveButton("Confirm") { a, d ->
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
                    deleteList(model, holder)
                }
                .setNeutralButton("Cancel") { a, d ->
                    a.dismiss()
                }
                .show()
        }
    }

    private fun deleteList(model: ModelList, holder: HolderCategory) {

        val id = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Lists")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Unable to delete due to ${e.message}", Toast.LENGTH_SHORT)
                    .show()


            }


    }

    override fun getItemCount(): Int {
        return listArrayList.size
    }

    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var listTv: TextView = binding.listTv
        var deleteBtn: ImageButton = binding.deleteBtn
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterList(filterList, this)
        }
        return filter as FilterList
    }

}