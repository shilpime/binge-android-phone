package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("baId") var baId: String?,
    @SerializedName("loginType") var loginType: String?,
    @SerializedName("rmn") var rmn: String?,
    @SerializedName("sid") var subscriberId: String?,
    @SerializedName("isHybrid") val isHybrid: Boolean = true
)