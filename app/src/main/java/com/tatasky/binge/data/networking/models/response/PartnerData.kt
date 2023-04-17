package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.utils.filterProviderOnly

class PartnerData {

    @SerializedName("layoutType",alternate = ["shuffleType"])
    var layoutType : String = ""
    @SerializedName("contentList")
    var contentList : List<ContentItem> = ArrayList()
    @SerializedName("providerName")
    var provider : String? = null
    var filteredContentItems: List<ContentItem> = ArrayList()
        get() = contentList.filter { filterProviderOnly(it.provider) }
}