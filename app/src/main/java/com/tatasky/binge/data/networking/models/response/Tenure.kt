package com.tatasky.binge.data.networking.models.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class Tenure: Parcelable {

    @SerializedName("lastActiveTenure")
    var lastActiveTenureForInactiveUser: Boolean? = null

    @SerializedName("tenureDurationDays")
    var tenureDurationInDaysWithDSuffix: String? = null /*Selected tenure duration Eg. 30D, 180D*/

    @SerializedName("tenureType")
    var tenureType: String? = null

    @SerializedName("tenureDuration")
    var tenureDuration: String? = null

    @SerializedName("mrp")
    var mrp: String? = null

    @SerializedName("offeredPrice")
    var offeredPrice: String? = null

    @SerializedName("offeredPriceValue")
    var offeredPriceValue : String? = null


    @SerializedName("discountedPercentage")
    var discountedPercentage: String? = null

    @SerializedName("tenureImage")
    var tenureImage: String? = null

    @SerializedName("currentTenure")
    var currentTenure: Boolean? = null

    @SerializedName("enable")
    var enable: Boolean? = null

    @SerializedName("tenureId")
    var tenureId: String? = null
}
