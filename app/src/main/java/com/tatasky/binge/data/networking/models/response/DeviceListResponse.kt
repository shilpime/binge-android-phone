package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.analytics.ATV

@SuppressLint("ParcelCreator")
class DeviceListResponse : BaseResponse(){
    val data: Data? = null
    class Data {
        @SerializedName("subscriberId")
        val subscriberId: String? = null
        @SerializedName("baId")
        val baId: String = ""
        @SerializedName("deviceList")
        var deviceList: ArrayList<DeviceList>? = null
        @SerializedName("smallDeviceFooterMessage")
        val smallDeviceFooterMessage: String? = null
        @SerializedName("largeDeviceFooterMessage")
        val largeDeviceFooterMessage: String? = null

        @SerializedName("removeDeviceVerbiage")
        val removeDeviceVerbiage: String? = null

        @SerializedName("smallDeviceCount")
        val smallDeviceCount: Int? = null

    }
}

class DeviceList {

    @SerializedName("baId")
    var baId: String = ""
    @SerializedName("deviceNumber")
    var deviceNumber: String = ""
    @SerializedName("deviceType")//deviceType
    var deviceType: String = ""
    @SerializedName("deviceStatus")
    var deviceStatus: String = ""
    @SerializedName("deviceName")//deviceName
    var deviceName: String = ""
    @SerializedName("primary")//deviceName
    var primary: Boolean = false
    @SerializedName("deviceSerialNumber", alternate = ["deviceId"])
    val deviceSerialNumber: String? = null

    var deviceNameTruncated:String = ""
        get() {
            return if (deviceName.length > 35)
                deviceName.substring(0, 35) + "..."
            else
                deviceName
        }
}
