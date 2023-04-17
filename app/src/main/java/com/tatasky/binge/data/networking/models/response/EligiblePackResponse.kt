package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class EligiblePackResponse constructor() : BaseResponse() {

    @SerializedName("data")
    var data: List<PartnerPacks>? = arrayListOf()



}

