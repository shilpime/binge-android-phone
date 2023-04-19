package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

/**
 * Created by Srikant Karnani on 16/1/20.
 */
data class ChangePasswordRequest(
    @SerializedName("newPwd") val newPass: String,
    @SerializedName("confirmPwd") val cnfPass: String = newPass,
    @SerializedName("oldPwd") val oldPass: String,
    @SerializedName("login") var isLogin : Boolean = false
)