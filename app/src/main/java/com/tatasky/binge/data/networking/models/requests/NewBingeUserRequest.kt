package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class NewBingeUserRequest(
    @SerializedName("login") var login: String? = null,
    @SerializedName("subscriberId") var subscriberId: String? = null,
    @SerializedName("bingeSubscriberId") var bingeSubscriberId: String? = null,
    @SerializedName("rmn") var rmn: String? = null,
    @SerializedName("eulaChecked") var eulaChecked: Boolean = true,
    @SerializedName("baId") var baId: String? = null,
    @SerializedName("mobileNumber") val mobileNumber: String? = null,
    @SerializedName("dthStatus") var dthStatus: String? = null,
    @SerializedName("pastBingeUser") var isPastBingeUser: Boolean? = true,
    @SerializedName("dsn") var dsn: String? = null,
    @SerializedName("packageId") var packageId: String? = null,
    @SerializedName("referenceId") var referenceId: String? = null,
    @SerializedName("cartId") var cartId: String? = null,
    @SerializedName("silentLoginEvent") var silentLoginEvent: String?= null,
    //isPastBingeUser
    var isCreate: Boolean = true,
)