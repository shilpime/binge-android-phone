package com.tatasky.binge.ui.features.home.adapter

import androidx.recyclerview.widget.DiffUtil
import com.tatasky.binge.data.networking.models.response.ContentItem

class ContentItemFiltersDiffCallback(var oldList: List<ContentItem>, var newList: List<ContentItem>) :
    DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return false
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldSectionModel = oldList[oldItemPosition]
        val newSectionModel = newList[newItemPosition]
        return oldSectionModel.contentId == newSectionModel.contentId && oldSectionModel.secondsWatched==newSectionModel.secondsWatched && oldSectionModel.id==newSectionModel.id
    }
}