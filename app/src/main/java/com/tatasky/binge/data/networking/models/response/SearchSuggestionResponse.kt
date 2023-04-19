package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Srikant Karnani on 9/1/20.
 */
class SearchSuggestionResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: List<ContentItem>? = null
}