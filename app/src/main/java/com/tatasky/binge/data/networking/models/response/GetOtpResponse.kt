package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

/**
 * Created by Srikant Karnani on 10/10/19.
 */

class GetOtpResponse : BaseResponse() {

    @SerializedName("data")
    var data: UserData? = null

    inner class UserData {
        @SerializedName("subscriberId")
        var sid: String? = null
        @SerializedName("rmn")
        var rmn: String? = null
        @SerializedName("maskedNumber")
        var maskedNumber: String? = null
    }
}
