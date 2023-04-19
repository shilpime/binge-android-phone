package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CatchupAiredResponse {
    @SerializedName("data")
    @Expose
    var data: HomeResponse.Items? = null
}