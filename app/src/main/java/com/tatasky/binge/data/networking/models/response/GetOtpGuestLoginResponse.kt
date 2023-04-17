package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class GetOtpGuestLoginResponse : BaseResponse() {

    @SerializedName("data")
    var data: Data? = null

    inner class Data {
        @SerializedName("mobileNumber")
        var mobileNumber: String? = null
    }
}