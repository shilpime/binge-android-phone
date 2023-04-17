package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class PreviouslyUsedMobileNumbersResponse : BaseResponse() {
    @SerializedName("data")
    var data: Data? = null

    inner class Data {
        @SerializedName("mobileNumbersList")
        var mobileNUmberList: List<UsedMobileNumber>? = null
    }
}

class UsedMobileNumber() : Parcelable {
    @SerializedName("mobileNumber")
    var mobileNumber: String? = null

    @SerializedName("premiumUser")
    var premiumUser: Boolean? = null

    @SerializedName("freeTrialEligible")
    var freeTrialEligible: Boolean = true // Let it be true by default to show free trial login UI

    constructor(parcel: Parcel) : this() {
        mobileNumber = parcel.readString()
        premiumUser = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        freeTrialEligible = parcel.readValue(Boolean::class.java.classLoader) as Boolean
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mobileNumber)
        parcel.writeValue(premiumUser)
        parcel.writeValue(freeTrialEligible)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UsedMobileNumber> {
        override fun createFromParcel(parcel: Parcel): UsedMobileNumber {
            return UsedMobileNumber(parcel)
        }

        override fun newArray(size: Int): Array<UsedMobileNumber?> {
            return arrayOfNulls(size)
        }
    }
}