package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
data class UserPlaybackEligibilityResponse(
    @SerializedName("data")
    val data: UserPlaybackEligibilityResponseData
) : BaseResponse()

data class UserPlaybackEligibilityResponseData(
    @SerializedName("anonymousId")
    val anonymousId: String,
    @SerializedName("verbiage")
    val verbiage: String,
    @SerializedName("contentPlayBackAllowed")
    val contentPlayBackAllowed: Boolean,
    @SerializedName("enableManageApp")
    val enableManageApp: Boolean

)
