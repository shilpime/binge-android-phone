package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class BAIdListResponse : BaseResponse() {
    @SerializedName("data")
    var data: SubscriberIdListResponse.SubscriberDetail? = null
}