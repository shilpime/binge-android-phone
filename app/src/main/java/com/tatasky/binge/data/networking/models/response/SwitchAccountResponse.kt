package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class SwitchAccountResponse : BaseResponse() {

    @SerializedName("data")
    var data: SwitchResponseData? = null

    @SerializedName("subMessage")
    val subMessage: String? = null

    inner class SwitchResponseData {
        @SerializedName("baId")
        var baId: String? = null

        @SerializedName("defaultProfile")
        var profileId: String? = null

        @SerializedName("mixpanelId", alternate = ["mixpanelid"])
        var mixpanelId: String? = null
    }
}