package com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.model

import com.google.gson.annotations.SerializedName

data class preferredLanguageBody(
    @SerializedName("baId")
    val baId: String,
    @SerializedName("bingeSubscriberId")
    val bingeSubscriberId: String,
    @SerializedName("mobileNumber")
    val mobileNumber: String
)