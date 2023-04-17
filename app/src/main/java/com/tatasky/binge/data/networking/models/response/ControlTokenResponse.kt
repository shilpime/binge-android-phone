package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class ControlTokenResponse : BaseResponse() {
    @SerializedName("data")
    var data: Data? = null


    data class Data(
        @SerializedName("token")
        var token: String? = null,
        @SerializedName("expiresIn")
        var expires_in: Long = 0
    )
}