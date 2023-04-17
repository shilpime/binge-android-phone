package com.tatasky.binge.data.networking.models.response

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Created by Srikant Karnani on 14/10/19.
 */
class LookUpViaRmnResponse : BaseResponse() {
    @SerializedName("data")
    var data: LookUpModel? = null

    class LookUpModel {
        @SerializedName("rmn")
        var rmn: String? = null
        @SerializedName("sidList")
        var sidList: List<SubscriberModel>? = null
    }

    class SubscriberModel() : Parcelable {
        @SerializedName("sid")
        var sid: String? = null
        @SerializedName("sName")
        var sName: String? = null

        constructor(parcel: Parcel) : this() {
            sid = parcel.readString()
            sName = parcel.readString()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(sid)
            parcel.writeString(sName)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SubscriberModel> {
            override fun createFromParcel(parcel: Parcel): SubscriberModel {
                return SubscriberModel(parcel)
            }

            override fun newArray(size: Int): Array<SubscriberModel?> {
                return arrayOfNulls(size)
            }
        }
    }
}