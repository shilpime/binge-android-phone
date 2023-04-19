package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class NewBingeUserResponse : BaseResponse() {
    @SerializedName("data")
    var bingeUserData: LoginResponse.BingeSubscription? = null

    @SerializedName("subMessage")
    val subMessage: String? = null
}

/*
class BingeUserResponse {
    @SerializedName("baId")
    var baId: String? = null

    @SerializedName("defaultProfile")
    var defaultProfile: String? = null

    @SerializedName("deviceAuthenticateToken")
    var deviceAuthenticateToken: String? = null

    @SerializedName("subscriberId")
    var subscriberId: String? = null

    @SerializedName("userAuthenticateToken")
    var userAuthenticateToken: String? = null

    @SerializedName("aliasName")
    var aliasName: String? = null

    @SerializedName("emailId")
    var emailId: String? = null

    @SerializedName("rrmSessionInfoDTO")
    var rrmInfo: RrmSessionInfo? = null

    @SerializedName("tvodEntitlements")
    @Expose
    var entitlements: List<LoginResponse.Entitlement>? = null

    @SerializedName("deviceDetails")
    var deviceList: DeviceList? = null
}*/
