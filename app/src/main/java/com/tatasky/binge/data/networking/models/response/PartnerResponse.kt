package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName
import com.tatasky.binge.utils.*

data class PartnerResponse(
    @SerializedName("data")
    val partnerList: PartnerList
)

data class PartnerList(
    @SerializedName("erosnow")
    val erosnow: PartnerData,
    @SerializedName("hotstar")
    val hotstar: PartnerData,
    @SerializedName("hungama")
    val hungama: PartnerData,
    @SerializedName("shemaroome")
    val shemaroome: PartnerData,
    @SerializedName("sonyliv")
    val sonyliv: PartnerData,
    @SerializedName("vootselect")
    val vootselect: PartnerData,
    @SerializedName("zee5")
    val zee5: PartnerData
)
