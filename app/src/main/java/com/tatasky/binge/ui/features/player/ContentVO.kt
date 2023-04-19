package com.tatasky.binge.ui.features.player

import androidx.annotation.Keep
import com.hungama.sdk.player.models.ContentType
import com.hungama.sdk.player.models.IContentVO

@Keep
class ContentVO(
    private val providerContentId: String,
    private val title: String,
    private val hungamaContentType: ContentType) : IContentVO {
    override fun getPosterURL(): String {
        return ""
    }

    override fun getId(): String {
       return providerContentId
    }

    override fun getType(): ContentType {
        return hungamaContentType
    }

    override fun getTitle(): String {
        return title
    }

}
