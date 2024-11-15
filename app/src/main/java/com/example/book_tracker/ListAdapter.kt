package com.example.book_tracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.book_tracker.databinding.ItemListBinding

class ListAdapter(private var list: MutableList<ListItem>) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    class ListViewHolder(val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val currentItem = list[position]

        holder.binding.listName.text = currentItem.list
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun updateList(newList: MutableList<ListItem>) {
        list = newList
        notifyDataSetChanged()
    }
}
