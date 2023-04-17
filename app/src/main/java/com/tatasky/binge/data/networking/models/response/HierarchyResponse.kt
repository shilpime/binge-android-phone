package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class HierarchyResponse : BaseResponse() {
    @SerializedName("data")
    var data: List<HomeResponse.Items>? = null

    var cacheTimeStamp : Long = 0L
}