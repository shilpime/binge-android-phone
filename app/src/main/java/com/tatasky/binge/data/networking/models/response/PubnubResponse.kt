package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PubnubResponse {


    /*Silent Login API Handling using timestamp*/
    //silentLoginTimestamp
    @SerializedName("silentLoginEvent")
    @Expose
    var silentLoginEvent : String? = null
    @SerializedName("silentLoginTimestamp")
    @Expose
    var silentLoginTimestamp: String? = null

    @SerializedName("dthSubscriberId")
    @Expose
    var dthSubscriberId: String? = null
    /*END of keys*/
    @SerializedName(value = "sid", alternate = ["subscriberId"])
    @Expose
    var sid: String? = null

    @SerializedName(value = "fullName", alternate = ["subscriberName", "sName"])
    @Expose
    var sName: String? = null

    @SerializedName("rmn")
    @Expose
    var rmn: String? = null

    @SerializedName(value = "isPremium", alternate = ["premiumUser"])
    @Expose
    var isPremium: Boolean = false

    @SerializedName(value = "isPVR", alternate = ["hasPVR"])
    @Expose
    var isPVR: Boolean = false

    @SerializedName(value = "acStatus", alternate = ["accountStatus"])
    @Expose
    var acStatus: String? = null

    @SerializedName("entitlements")
    @Expose
    var entitlements: List<LoginResponse.Entitlement>? = null

    @SerializedName("profiles", alternate = ["bingeSubscriberProfiles"])
    val profilePubNubList: List<LoginResponse.ProfilePubNub>? = null

    @SerializedName("comvivaSubscriberAgeRating")
    val comvivaSubscriberAgeRatingList: List<LoginResponse.ComVivaSubscriberAgeRating>? = null

    @SerializedName("bingeSubscription")
    @Expose
    var bingeSubscriptions: List<LoginResponse.BingeSubscription>? = null

    @SerializedName("emailId")
    @Expose
    var emailId: String? = null

    @SerializedName("bingeMobileAccounts")
    @Expose
    var bingeMobileAccounts: List<LoginResponse.PubNubAccount>? = null

    @SerializedName("baId")
    var baId: String? = null

    @SerializedName("accountId")
    var accountId: String? = null

    @SerializedName("deviceInfo")
    @Expose
    var deviceInfo : List<LoginResponse.PubNubAccount>?=null

    @SerializedName("dthStatus")
    @Expose
    val dthStatus : String? = null

    @SerializedName("mobileNumber")
    @Expose
    var mobileNumber: String? = null

    @SerializedName("mixpanelId")
    @Expose
    val mixpanelid : String? = null

    @SerializedName("cancelledDeviceInfo")
    @Expose
    var cancelledDeviceInfo: List<LoginResponse.CancelledDeviceInfo>?=null

    @SerializedName("devices")
    @Expose
    var devices : List<DeviceList>?=null

    @SerializedName("deviceManagement")
    @Expose
    var deviceManagement : Boolean = false

    @SerializedName("prime")
    @Expose
    var prime : PrimePubnubDTO ? =null

    @SerializedName( "paymentStatus")
    @Expose
    var paymentStatus: String ?  = null

    @SerializedName("bingeList")
    @Expose
    var bingeList : BingeList?=null

    class BingeList {

    }
}