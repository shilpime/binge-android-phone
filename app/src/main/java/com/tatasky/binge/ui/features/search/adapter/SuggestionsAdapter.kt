package com.tatasky.binge.ui.features.search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.databinding.LayoutSearchSuggestionBinding
import com.tatasky.binge.ui.features.search.ContentItemDiffCallback
import com.tatasky.binge.ui.features.search.model.SearchViewModel

/**
 * Created by Srikant Karnani on 5/1/20.
 */
class SuggestionsAdapter(val searchViewModel: SearchViewModel) :
    RecyclerView.Adapter<SuggestionsAdapter.ViewHolder>() {
    private var mList: MutableList<ContentItem> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutSearchSuggestionBinding.inflate(
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
    }

    fun updateList(list: List<ContentItem>) {
        val diffCallback = ContentItemDiffCallback(mList, list)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        mList.clear()
        mList.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(val binding: LayoutSearchSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(suggestedItem: ContentItem, searchViewModel: SearchViewModel) {
            binding.suggestion = suggestedItem
            binding.vm = searchViewModel
        }
    }
}