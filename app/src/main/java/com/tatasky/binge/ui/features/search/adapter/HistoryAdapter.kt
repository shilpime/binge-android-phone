package com.tatasky.binge.ui.features.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.databinding.LayoutSearchHistoryBinding
import com.tatasky.binge.ui.features.search.model.SearchViewModel

/**
 * Created by Srikant Karnani on 5/1/20.
 */
class HistoryAdapter(val searchViewModel: SearchViewModel) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    private var mList= mutableListOf<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutSearchHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(mList[position], searchViewModel)
        holder.binding.cross.setOnClickListener {
            val keyword = holder.binding.tvSearchKeyword.text.toString()
            searchViewModel.clearRecentSearchItem(keyword)
        }
    }

    fun updateList(list:List<String>) {
        val diffResult = DiffUtil.calculateDiff(DiffCallback(this.mList, list), true)
        this.mList.clear()
        this.mList.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(val binding: LayoutSearchHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(searchKeyWord: String, searchViewModel: SearchViewModel) {
            binding.suggestion = searchKeyWord
            binding.vm = searchViewModel
        }
    }

    class DiffCallback(var oldList: List<String>, var newList: List<String>) :
        DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}