package com.tatasky.binge.ui.features.search

import androidx.recyclerview.widget.DiffUtil
import com.tatasky.binge.data.networking.models.response.ContentItem

/**
 * Created by Srikant Karnani on 11/1/20.
 */
class ContentItemDiffCallback(var oldList:List<ContentItem>, var newList:List<ContentItem>) :DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return true
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id==newList[newItemPosition].id && oldList[oldItemPosition].contentType==newList[newItemPosition].contentType
    }
}