package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class ValidateOtpGuestLoginRequest(
    @SerializedName("mobileNumber")
    val mobileNumber: String? = null,

    @SerializedName("otp")
    val otp: String? = null,
)