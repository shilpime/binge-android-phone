package com.tatasky.binge.ui.features.parentalcontrol.bottomsheet

import android.os.Parcel
import android.os.Parcelable

class ParentalControlBottomSheetResultStatus {
    companion object {
        const val PIN_VERIFIED = "pin verified"
        const val SUCCESS_DISMISS = "success dismiss"
        const val DISMISS = "dismiss"
    }
}

data class ParentalControlBottomSheetResult(
    val resultStatus: String? = null,
    val pinValue: String? = null,
    val actionBeforeOpeningBottomSheet: String? = null,
    val fromNudge: Boolean? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(resultStatus)
        parcel.writeString(pinValue)
        parcel.writeString(actionBeforeOpeningBottomSheet)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParentalControlBottomSheetResult> {
        override fun createFromParcel(parcel: Parcel): ParentalControlBottomSheetResult {
            return ParentalControlBottomSheetResult(parcel)
        }

        override fun newArray(size: Int): Array<ParentalControlBottomSheetResult?> {
            return arrayOfNulls(size)
        }
    }
}