package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class LionsGateResponse: BaseResponse() {
    var isPrepand: Boolean = false

    @SerializedName("data")
    @Expose
    var data: Data? = null

    inner class Data{
        @SerializedName("kid")
        @Expose
        val kid : String? = null

        @SerializedName("token")
        @Expose
        val token : String? = null

        @SerializedName("widevineLicenceUrl")
        @Expose
        val widevineLicenceUrl : String? = null
    }
}