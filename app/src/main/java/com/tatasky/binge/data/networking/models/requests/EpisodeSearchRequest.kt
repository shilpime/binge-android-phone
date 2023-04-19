package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class EpisodeSearchRequest(
    @SerializedName("parentType") val parentType : String?,
    @SerializedName("queryString")  val queryString: String?,
    @SerializedName("id") val seriesId: String?,
    @SerializedName("pageNumber") val pageNumber: Int?
)