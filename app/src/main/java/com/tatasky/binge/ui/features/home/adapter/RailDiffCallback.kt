package com.tatasky.binge.ui.features.home.adapter

import androidx.recyclerview.widget.DiffUtil
import com.tatasky.binge.data.networking.models.response.HomeResponse

class RailDiffCallback(
    var oldList: List<HomeResponse.Items>,
    var newList: List<HomeResponse.Items>
) :
    DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldSectionModel = oldList[oldItemPosition]
        val newSectionModel = newList[newItemPosition]
        return oldSectionModel.sectionType == newSectionModel.sectionType && oldSectionModel.id == newSectionModel.id
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
        var areSame = true
        if (oldSectionModel.id == newSectionModel.id && oldSectionModel.contentItem.size == newSectionModel.contentItem.size) {
            oldSectionModel.contentItem.forEachIndexed { index, item ->
                areSame = areSame && newSectionModel.contentItem[index].id == item.id
            }
        } else {
            areSame = false
        }
        return areSame
    }
}