package com.tatasky.binge.ui.base.frameworks

import android.os.Parcel
import android.os.Parcelable

/**Parcelable Wrapper for setting the value in savedStateHandle*/
data class SingleEventParcelizeWrapper(
    var booleanEventValue: SingleEvent<Boolean?> = SingleEvent(null)
) : Parcelable {
    constructor(parcel: Parcel) : this() {
        booleanEventValue =
            SingleEvent(parcel.readValue(Boolean::class.java.classLoader) as? Boolean)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(booleanEventValue.getContentIfNotHandled())
    }

    companion object CREATOR : Parcelable.Creator<SingleEventParcelizeWrapper> {
        override fun createFromParcel(parcel: Parcel): SingleEventParcelizeWrapper {
            return SingleEventParcelizeWrapper(parcel)
        }

        override fun newArray(size: Int): Array<SingleEventParcelizeWrapper?> {
            return arrayOfNulls(size)
        }
    }
}
