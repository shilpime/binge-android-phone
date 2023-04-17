package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class FtvCampaignResponse : BaseResponse() {
    @SerializedName("data")
    var data: FtvCampaignResponse.Data? = null

    inner class Data {
        @SerializedName("diyInstallation")
        var diyInstallation: Boolean = false

        @SerializedName("installationRequired")
        var installationRequired: Boolean = false
    }
}