package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class UpdateEmailRequestWithoutName(
    @SerializedName("emailId")
    val emailId: String,

    @SerializedName("rmn")
    val rmn: String,

    @SerializedName("subscriberId")
    val subscriberId: String,

    @SerializedName("baId")
    val baId: String,
)