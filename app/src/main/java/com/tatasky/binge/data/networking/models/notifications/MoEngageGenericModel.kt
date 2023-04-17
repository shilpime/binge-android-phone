package com.tatasky.binge.data.networking.models.notifications

import com.google.gson.annotations.SerializedName

data class MoEngageGenericModel(
    @SerializedName("screenName") var screenName: String,
    @SerializedName("message") var message: String?,
    @SerializedName("data") var any: Any?
)