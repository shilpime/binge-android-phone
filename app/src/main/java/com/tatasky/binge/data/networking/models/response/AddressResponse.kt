package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class AddressResponse : BaseResponse() {
    @SerializedName("data")
    var data: SubscriberAddress? = null
}

data class SubscriberAddress(
    @SerializedName("address")
    var address: String? = null,
    @SerializedName("city")
    var city: String? = null,
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("ocsFlag")
    var ocsFlag: String? = null,
    @SerializedName("pincode")
    var pincode: String? = null,
    @SerializedName("serviceRegionId")
    var serviceRegionId: String? = null,
    @SerializedName("slotSuggestions")
    var slotSuggestions: List<SlotSuggestion>? = null,
    @SerializedName("state")
    var state: String? = null
)

data class SlotSuggestion(
    @SerializedName("start")
    var start: String? = null,
    @SerializedName("end")
    var end: String? = null
)