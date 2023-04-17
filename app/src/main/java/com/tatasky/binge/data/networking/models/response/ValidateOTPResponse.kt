package com.tatasky.binge.data.networking.models.response

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


class ValidateOTPResponse() : BaseResponse() {

    @SerializedName("data")
    var data: UserData? = null

    constructor(parcel: Parcel) : this() {

    }

    inner class UserData {
        @SerializedName("userAuthenticateToken")
        var userAuthenticateToken: String? = null

        @SerializedName("deviceAuthenticateToken")
        var deviceAuthenticateToken: String? = null
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ValidateOTPResponse> {
        override fun createFromParcel(parcel: Parcel): ValidateOTPResponse {
            return ValidateOTPResponse(parcel)
        }

        override fun newArray(size: Int): Array<ValidateOTPResponse?> {
            return arrayOfNulls(size)
        }
    }
}
