package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class DeviceDetail {
    @SerializedName("dateCreated")
    @Expose
    var dateCreated: String? = null

    @SerializedName("lastUpdated")
    @Expose
    var lastUpdated: String? = null

    @SerializedName("referenceId")
    @Expose
    var referenceId: String? = null

    @SerializedName("deviceSerialNumber")
    @Expose
    var deviceSerialNumber: String? = null

    @SerializedName("deviceStatus")
    @Expose
    var deviceStatus: String? = null

    @SerializedName("deleted")
    @Expose
    var deleted: Boolean? = null

    @SerializedName("entitlements")
    @Expose
    var entitlements: List<Entitlement>? = null

    @SerializedName("deviceType")
    @Expose
    var deviceType: String? = null

    @SerializedName("googleId")
    @Expose
    var googleId: Any? = null

    @SerializedName("connectionType")
    @Expose
    var connectionType: String? = null

    @SerializedName("showFreePoster")
    @Expose
    var showFreePoster: Boolean? = null

    @SerializedName("bingePackEligible")
    @Expose
    var bingePackEligible: String? = null

    @SerializedName("firstPackActDate")
    @Expose
    var firstPackActDate: String? = null

    @SerializedName("packDctDate")
    @Expose
    var packDctDate: String? = null

    @SerializedName("baId")
    @Expose
    var baId: String? = null

    @SerializedName("mirrored")
    @Expose
    var mirrored: String? = null

    inner class Entitlement {
        @SerializedName("dateCreated")
        @Expose
        var dateCreated: String? = null

        @SerializedName("lastUpdated")
        @Expose
        var lastUpdated: String? = null

        @SerializedName("id")
        @Expose
        var id: Int? = null

        @SerializedName("packageId")
        @Expose
        var packageId: String? = null

        @SerializedName("packageName")
        @Expose
        var packageName: String? = null

        @SerializedName("planType")
        @Expose
        var planType: String? = null

        @SerializedName("planValue")
        @Expose
        var planValue: Int? = null

        @SerializedName("paymentModel")
        @Expose
        var paymentModel: String? = null
    }
}