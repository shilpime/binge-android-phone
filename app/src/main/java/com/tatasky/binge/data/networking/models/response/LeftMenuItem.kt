package com.tatasky.binge.data.networking.models.response

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LeftMenuItem() : Parcelable {
    @SerializedName("pageName")
    @Expose
    var pageName: String? = null

    @SerializedName("pageType")
    @Expose
    var pageType: String? = null

    @SerializedName("position")
    @Expose
    var position: Int? = null

    @SerializedName("searchPageName")
    @Expose
    var searchPageName: String? = null

    @SerializedName("subPageImage")
    @Expose
    var subPageImage: String? = null

    constructor(parcel: Parcel) : this() {
        pageName = parcel.readString()
        pageType = parcel.readString()
        position = parcel.readValue(Int::class.java.classLoader) as? Int
        searchPageName = parcel.readString()
        subPageImage = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(pageName)
        parcel.writeString(pageType)
        parcel.writeValue(position)
        parcel.writeString(searchPageName)
        parcel.writeString(subPageImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LeftMenuItem> {
        override fun createFromParcel(parcel: Parcel): LeftMenuItem {
            return LeftMenuItem(parcel)
        }

        override fun newArray(size: Int): Array<LeftMenuItem?> {
            return arrayOfNulls(size)
        }
    }
}