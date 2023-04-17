package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class OfferEligiblePacks {
    @SerializedName("title")
    var title: String? = null

    @SerializedName("offerEligibilityId")
    var id: String? = null

    @SerializedName("value")
    var value: String? = null
}