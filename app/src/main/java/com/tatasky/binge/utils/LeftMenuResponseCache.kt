package com.tatasky.binge.utils

import com.tatasky.binge.data.networking.models.response.LeftMenuItem

object LeftMenuResponseCache {
    private val mLeftMenuItemList: MutableList<LeftMenuItem> = arrayListOf()
    val leftMenuItemList: List<LeftMenuItem> = mLeftMenuItemList

    fun setLeftMenuItemList(list: List<LeftMenuItem>?) {
        list?.let {
            mLeftMenuItemList.clear()
            mLeftMenuItemList.addAll(it)
        }
    }
}