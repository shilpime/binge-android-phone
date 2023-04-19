package com.tatasky.binge.ui.features.search.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.databinding.LayoutSearchSuggestionGenreItemBinding
import com.tatasky.binge.databinding.LayoutSearchSuggestionItemBinding
import com.tatasky.binge.databinding.LayoutSearchSuggestionKeywordItemBinding
import com.tatasky.binge.ui.features.home.SuggestionSuggestors
import com.tatasky.binge.ui.features.search.model.SearchViewModel


class SuggestionsAdapter(private val searchViewModel: SearchViewModel) :
    RecyclerView.Adapter<SearchSuggestionViewHolder>() {


    var mList: MutableList<ContentItem> = mutableListOf()


    @SuppressLint("NotifyDataSetChanged")
    fun clearAdapter(){
        mList.clear()
        notifyDataSetChanged()
    }

    fun updateList(newData: List<ContentItem>) {

        val oldList: List<ContentItem> = this.mList

        val diffResult: DiffUtil.DiffResult = DiffUtil.calculateDiff(
            ItemDiffCallback(
                oldList,
                newData
            )
        )
        this.mList = newData as MutableList<ContentItem>
        diffResult.dispatchUpdatesTo(this)
    }


    // DiffUtil --> to increase performance of a RecyclerView
    class ItemDiffCallback(
        private var oldList: List<ContentItem>,
        private var newList: List<ContentItem>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return (oldList[oldItemPosition].id == newList[newItemPosition].id)
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


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchSuggestionViewHolder {
        return when (viewType) {
            R.layout.layout_search_suggestion_keyword_item -> {
                SearchSuggestionViewHolder.SuggestionDictionaryViewHolder(
                    LayoutSearchSuggestionKeywordItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.layout_search_suggestion_genre_item -> {
                SearchSuggestionViewHolder.SuggestionGenreViewHolder(
                    LayoutSearchSuggestionGenreItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            R.layout.layout_search_suggestion_item -> {
                SearchSuggestionViewHolder.SuggestionContentViewHolder(
                    LayoutSearchSuggestionItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> {
                throw IllegalArgumentException("Invalid ViewType Provided")
            }
        }
    }

    override fun onBindViewHolder(holder: SearchSuggestionViewHolder, position: Int) {
        when (holder) {
            is SearchSuggestionViewHolder.SuggestionDictionaryViewHolder -> holder.bind(
                mList[position],
                searchViewModel
            )
            is SearchSuggestionViewHolder.SuggestionContentViewHolder -> holder.bind(
                mList[position],
                searchViewModel
            )
            is SearchSuggestionViewHolder.SuggestionGenreViewHolder -> holder.bind(
                mList[position],
                searchViewModel
            )
        }
    }

    override fun getItemCount() = mList.size

    override fun getItemViewType(position: Int): Int {
        return if (mList[position].suggestor.equals(SuggestionSuggestors.KeywordSuggestor.name, true))
            R.layout.layout_search_suggestion_keyword_item
        else if (mList[position].suggestor.equals(SuggestionSuggestors.GenreSuggestor.name,true))
            R.layout.layout_search_suggestion_genre_item
        else
            R.layout.layout_search_suggestion_item
    }
}
