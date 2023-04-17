package com.tatasky.binge.interfaces

import com.tatasky.binge.data.networking.models.response.ContentItem

interface CommonContentViewListener {
    fun onContentViewed(hbNumber : String, contentItem : ContentItem)
}