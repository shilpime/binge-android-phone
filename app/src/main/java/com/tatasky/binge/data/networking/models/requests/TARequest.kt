package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class TARequest(@SerializedName("placeHolder") val placeHolder: String,
                     @SerializedName("pageLimit") val pageLimit: String,
                     @SerializedName("pageType") val pageType: String? = null,
                     @SerializedName("id") var id: String = "",
                     @SerializedName("contentType") var contentType: String = "",
                     @SerializedName("showType") var showType: String = "",
                     @SerializedName("provider") var provider: String = "",
                     @SerializedName("filterLanguage") var filterLanguage: String = "",
                     @SerializedName("subGenre") var subGenre: String = "",
                     @SerializedName("related") var isRelated: Boolean = false,
                     @SerializedName("subPage") var subPage: Boolean = false,
                     @SerializedName("languageGenre") var languageGenre: Boolean = false,
                     @SerializedName("isLoggedIn") var isLoggedIn:Boolean = false,
                     @SerializedName("body") val body: EmptyBody,
                     @SerializedName("layoutType") var layoutType: String = "LANDSCAPE",
                     @SerializedName("freeToggle") var freeToggle: Boolean? = null
                     )