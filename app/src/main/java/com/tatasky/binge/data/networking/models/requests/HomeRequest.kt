package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class HomeRequest(@SerializedName("pageType") val pageType: String,
                       @SerializedName("pageLimit") val pageLimit: String,
                       @SerializedName("pageOffset") val pageOffset: String,
                       @SerializedName("subscribed") var subscribed: Boolean = false,
                       @SerializedName("unsubscribed") val unsubscribed: Boolean = false,
                       @SerializedName("preferredLanguages") var preferredLanguages : String? = null,
                       @SerializedName("packName") var packName : String = "None"

                       )