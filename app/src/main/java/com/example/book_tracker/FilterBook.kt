package com.example.book_tracker

import android.widget.Filter
class FilterBook(
    private val filterList: ArrayList<ModelBook>,
    private val adapterBook: AdapterBook
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val results = FilterResults()

        if (constraint.isNullOrEmpty()) {
            results.count = filterList.size
            results.values = filterList
        } else {
            val query = constraint.toString().lowercase()
            val filteredBooks = filterList.filter {
                it.title.lowercase().contains(query) || it.author.lowercase().contains(query)
            }
            results.count = filteredBooks.size
            results.values = ArrayList(filteredBooks)
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapterBook.bookList = results?.values as ArrayList<ModelBook>
        adapterBook.notifyDataSetChanged()
    }
}

