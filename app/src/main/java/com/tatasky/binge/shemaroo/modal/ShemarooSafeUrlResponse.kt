package com.tatasky.binge.shemaroo.modal


import com.google.gson.annotations.SerializedName
import com.tatasky.binge.data.networking.models.response.BaseResponse

class ShemarooSafeUrlResponse : BaseResponse() {
    @SerializedName("adaptive_urls")
    var adaptiveUrls: List<AdaptiveUrls>? = null
}
