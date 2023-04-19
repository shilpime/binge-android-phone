package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class PurchasePackResponse : BaseResponse() {
    @SerializedName("data")
    var data: PartnerPacks? = null
}