package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class VootPwaResponse : BaseResponse() {
    @SerializedName("data")
    var data: HashMap<String,Any>? = null
}