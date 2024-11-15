package com.example.book_tracker

import android.widget.Filter

class FilterList: Filter{

    private var filterList: ArrayList<ModelList>
    private var adapterList: AdapterList
    constructor(filterList: ArrayList<ModelList>,adapterList: AdapterList) : super() {
        this.filterList = filterList
        this.adapterList = adapterList
    }
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if(constraint != null && constraint.isNotEmpty()){

            constraint =  constraint.toString().uppercase()
            val filteredModels:ArrayList<ModelList> = ArrayList()
            for( i in 0 until filterList.size){
                if(filterList[i].list.uppercase().contains(constraint)){

                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            results.count = filterList.size
            results.values= filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapterList.listArrayList = results.values as ArrayList<ModelList>

        adapterList.notifyDataSetChanged()
    }
}