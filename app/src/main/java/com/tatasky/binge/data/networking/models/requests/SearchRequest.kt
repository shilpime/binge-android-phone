package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class SearchRequest(
@SerializedName("filterGenre") var filterGenre: List<String>? = null,
@SerializedName("filterLanguage") var filterLanguage: List<String>? = null,
@SerializedName("preferGenre") var preferGenre: List<String>? = null,
@SerializedName("preferLang") var preferLang: List<String>? = null,
@SerializedName("queryString") var queryString: String? = null,//pageName
@SerializedName("pageName") var pageName: String? = "null",
@SerializedName("pageNumber") var pageNumber: Int = 1,
@SerializedName("intentUrl") var intentUrl: String? = null,
@SerializedName("filter") var filter : Boolean = false,
@SerializedName("freeToggle") var freeToggle : Boolean = false,
@SerializedName("contentType") var contentType : String? = null
)