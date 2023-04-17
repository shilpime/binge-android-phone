package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class PlaybackExpiryResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    class Data{
        @SerializedName("message")
        val message: String? = null

        @SerializedName("purchaseExpiry")
        val purchaseExpiry: String? = null
    }
}