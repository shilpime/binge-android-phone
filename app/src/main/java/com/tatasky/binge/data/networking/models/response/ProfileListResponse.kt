package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProfileListResponse : BaseResponse() {

    @SerializedName("data")
    @Expose
    var bingeSubscriptions : List<LoginResponse.BingeSubscription>? = null
}