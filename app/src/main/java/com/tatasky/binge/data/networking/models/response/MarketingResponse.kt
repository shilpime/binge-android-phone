package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MarketingResponse : BaseResponse() {
    val data: Data? = null

    class Data {
        var autoScrollTime:Int? = null
        var list: ArrayList<MarketingResponseList>? = null
    }
}