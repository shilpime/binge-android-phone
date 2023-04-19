package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.utils.*

class HoichoiPlayebackResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    class Data{

        @SerializedName("partner")
        @Expose
        var partner: String? = null

        @SerializedName("expiryDuration")
        @Expose
        var expiryDuration: String? = null

        @SerializedName("message")
        @Expose
        var message: String? = null
        @SerializedName("expiryUnit")
        @Expose
        var expiryUnit: String? = null

        @SerializedName("token")
        @Expose
        var token: String? = null


    }

}
