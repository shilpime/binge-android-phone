package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

/**
 * Created by Srikant Karnani on 16/1/20.
 */
data class ForgotPasswordRequest(
    @SerializedName("newPwd") val newPass: String,
    @SerializedName("confirmPwd") val cnfPass: String = newPass
)