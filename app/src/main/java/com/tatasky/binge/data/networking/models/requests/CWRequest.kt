package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class CWRequest(@SerializedName("subscriberId") val subscriberId: String,
                     @SerializedName("profileId") val profileId: String,
                     @SerializedName("id") var contentId: String = "",
                     @SerializedName("seeAll") var seeAll: Boolean = false,
                     @SerializedName("cw") var cw: Boolean = true,
                     @SerializedName("contentType") var contentType: String = "",
                     @SerializedName("pagingState") var pagingState: String? = null,
                     @SerializedName("offset") var offset: Int = 0,
                     @SerializedName("watchDuration") var watchDuration: Int = 0,
                     @SerializedName("totalDuration") var totalDuration: Int = 0,
                     @SerializedName("uniqueId") var uniqueId: String,
                     @SerializedName("provider") var provider: String? = "",
                     var isLoggedIn : Boolean = true)