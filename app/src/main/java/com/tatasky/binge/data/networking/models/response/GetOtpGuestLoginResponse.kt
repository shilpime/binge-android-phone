package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class GetOtpGuestLoginResponse : BaseResponse() {

    @SerializedName("data")
    var data: Data? = null

    inner class Data {
        @SerializedName("mobileNumber")
        var mobileNumber: String? = null

        // Key for verbiage of generate otp api
        @SerializedName("enterOTP")
        var enterOTP: String? = null
        @SerializedName("resendOtpHeading")
        var resendOtpHeading: String? = null
        @SerializedName("incorrectOtpVerbiage")
        var incorrectOtpVerbiage: String? = null
        @SerializedName("secondsVerbiage")
        var secondsVerbiage: String? = null
        @SerializedName("resendOtpInVerbiage")
        var resendOtpInVerbiage: String? = null
        @SerializedName("enterOtpVerbiage")
        var enterOtpVerbiage: String? = null
        @SerializedName("resendOtpVerbiage")
        var resendOtpVerbiage: String? = null
    }
}
