package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class OffersInPack {
    @SerializedName("title")
    var title: String? = null

    @SerializedName("offerEligibilityId")
    var id: String? = null

    @SerializedName("value")
    var isAvailable : Boolean = false

    @SerializedName("image")
    var imgUrl : String? = null
}