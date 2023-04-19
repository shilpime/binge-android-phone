package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SubscriptionPubnubResponse {

    @SerializedName("subscriptions")
    @Expose
    var subscriptionList: List<SubscriptionDto>  = ArrayList()
    @SerializedName("baId")
    @Expose
    var baId=""

    class SubscriptionDto{

        @SerializedName("appProductName")
        @Expose
        var appProductName:  String= ""
        @SerializedName("isFree")
        @Expose
        var  isFree: Boolean= false
        @SerializedName("expiry")
        @Expose
        var  expiry: String=""
    }
}