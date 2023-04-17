package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class Providers {
    @SerializedName("name")
    var providerName: String? = null

    @SerializedName("providerId")
    var providerId: String? = null

    @SerializedName("iconUrl")
    var iconUrl: String? = null

    @SerializedName("squareImageUrl")
    var squareImageUrl: String? = null

    @SerializedName("premiumPartner")
    var premiumPartner : Boolean? = null
}
