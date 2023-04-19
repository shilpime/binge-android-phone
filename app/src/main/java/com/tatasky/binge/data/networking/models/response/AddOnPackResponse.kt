package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class AddOnPackResponse : BaseResponse() {
    @SerializedName("data")
    var data: AddOnPackData? = null

    inner class AddOnPackData{
        @SerializedName("adonPackList")
        var addOnPackData: List<PartnerPacks>? = null
    }
}