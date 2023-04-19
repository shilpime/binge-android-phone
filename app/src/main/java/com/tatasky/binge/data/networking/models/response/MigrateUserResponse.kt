package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class MigrateUserResponse : BaseResponse() {

    @SerializedName("data")
    var data: Data? = null

    inner class Data {
        //gAuthToken
        @SerializedName("gAuthToken")
        var gAuthToken: String? = null
        @SerializedName("anonymousId")
        var anonymousId: String? = null
        @SerializedName("mixpanelId")
        var mixpanelId: String? = null
        @SerializedName("dthStatus")
        var dthStatus: String? = null
        @SerializedName("referenceId")
        var referenceId: String? = null
        @SerializedName("rmn")
        var rmn: String? = null
        @SerializedName("isPastBingeUser")
        var isPastBingeUser: Boolean = false
        @SerializedName("userAuthenticateToken")
        var userAuthenticateToken: String? = null

        //rmn, isPastBingeUser,
    }
}