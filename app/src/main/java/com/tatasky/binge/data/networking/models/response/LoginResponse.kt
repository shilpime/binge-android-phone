package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.utils.DTH_W_BINGE_OLD_USER

class LoginResponse : BaseResponse() {
    @SerializedName("data")
    var rrmSessionInfo: RrmSessionInfo? = null


    inner class BSub {
        @SerializedName(value = "sid", alternate = ["subscriberId"])
        @Expose
        var sid: String? = null

        @SerializedName(value = "fullName", alternate = ["subscriberName", "sName"])
        @Expose
        var sName: String? = null

        @SerializedName("rmn")
        @Expose
        var rmn: String? = null
    }

    open class UserData {

        @SerializedName("refreshToken")
        @Expose
        var refreshToken: String? = null

        @SerializedName("accessToken")
        @Expose
        var accessToken: String? = null

        @SerializedName("expiresIn")
        @Expose
        var expiresIn: String? = null

        @SerializedName("pubnubChannel")
        @Expose
        var pubnubChannel: String? = null

        @SerializedName("crmId")
        @Expose
        var crmId: String? = null

        @SerializedName("forceChangePwd")
        @Expose
        var forceChangePwd: Boolean = false

        @SerializedName("isFirstTimeLoggedIn")
        @Expose
        var isFirstTimeLoggedIn: Boolean = false

        @SerializedName("isPromotionEnable")
        @Expose
        var isPromotionEnable: Boolean = false

        @SerializedName("promotionStartDate")
        @Expose
        var promotionStartDate: Long = 0

        @SerializedName("promotionEndDate")
        @Expose
        var promotionEndDate: Long = 0

        @SerializedName("encryptedPassword")
        @Expose
        var encryptedPassword: String? = null

        @SerializedName("rrmSessionInfoDTO")
        @Expose
        var rrmSessionInfoDTO: RrmSessionInfo? = null

        @SerializedName("userDetails")
        @Expose
        var userDetails: UserDetails? = null

        @SerializedName("userProfile")
        @Expose
        var userProfile: UserProfile? = null

        @SerializedName("deviceDetails")
        @Expose
        var deviceDetails: List<DeviceDetail>? = null

    }

    inner class DeviceDetail {

        @SerializedName("friendlyName")
        @Expose
        var friendlyName: String? = null

        @SerializedName("deviceId")
        @Expose
        var deviceId: String? = null

        @SerializedName("model")
        @Expose
        var model: String? = null

        @SerializedName("manufacturer")
        @Expose
        var manufacturer: String? = null

    }

    inner class Entitlement {

        @SerializedName("type")
        @Expose
        var type: String? = null

        @SerializedName("pkgId")
        @Expose
        var pkgId: String? = null

    }

    class UserDetails : UserData() {

        private val dateCreated: Long = 0
        private val lastUpdated: Long = 0
        private val id: String? = null

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
        var entitlements: List<Entitlement>? = null

        @SerializedName("profiles")
        val profilePubNubList: List<ProfilePubNub>? = null

        @SerializedName("bingeSubscription")
        @Expose
        var bingeSubscriptions: List<BingeSubscription>? = null

        @SerializedName("emailId")
        @Expose
        var emailId: String? = null

        @SerializedName("bingeMobileAccounts")
        @Expose
        var bingeMobileAccounts: List<PubNubAccount>? = null

        @SerializedName("deviceInfo")
        @Expose
        var deviceInfo : List<PubNubAccount>?=null
    }

    /*This class holds data of LoginResponse*/
    class BingeSubscription {
        var platform: String? = null
        var rmn: String?= null
        @SerializedName("baId")
        var baId: String? = null

        @SerializedName("deviceAuthenticateToken")
        var deviceAuthenticateToken: String? = null

        @SerializedName("subscriberId")
        var subscriberId: String? = null

        @SerializedName("freeTrialAvailed")
        var freeTrialAvailed: Boolean = true

        @SerializedName("firstTimeLogin")
        var firstTimeLogin: Boolean = true

        @SerializedName("userAuthenticateToken")
        var userAuthenticateToken: String? = null

        @SerializedName("rrmSessionInfoDTO")
        var rrmInfo: RrmSessionInfo? = null

        @SerializedName("tvodEntitlements")
        @Expose
        var entitlements: List<Entitlement>? = null

        @SerializedName("deviceDetails")
        var deviceDetails: DeviceList? = null

        @SerializedName("emailId" ,alternate = ["email"])
        var emailId: String? = null

        @SerializedName("subscriptionType")
        var subscriptionType: String? = null

        @SerializedName("accountStatus")
        var bingeAccountStatus: String? = null

        @SerializedName("subscriberAccountStatus")
        var dthAccountStatus: String? = null

        @SerializedName("accountType")
        var accountType: String? = null

        @SerializedName("partnerSubscriptionsDetails")
        var partnerSubscriptions : PartnerPacks? = null

        @SerializedName("currentSubscription")
        var currentSubscription: String? = null

        @SerializedName("firstTimeLoginDate")
        var firstTimeLoginDate: String? = null

        //aliasName
        @SerializedName("expiry")
        var expiry: String? = null

        @SerializedName("aliasName")
        var aliasName: String? = null

        @SerializedName("firstName")
        var firstName: String? = null

        @SerializedName("lastName")
        var lastName: String? = null

        @SerializedName("profileId", alternate = ["defaultProfile"])
        var profileId: String? = null

        @SerializedName("bingeSubscriberId")
        var bingeSubscriberId: String = ""

        @SerializedName("imageUrl", alternate = ["image"])
        var imageUrl: String? = null

        @SerializedName("bingeAccountCount")
        var numberOfBingeAccounts : Int = 1

        @SerializedName("deviceLoginCount")
        var numberOfDevicesLoggedIn : Int = 1

        @SerializedName("fsTaken")
        var fsTaken : Boolean = false

        @SerializedName("sufficientBalance")
        var sufficientBalance : Boolean = true

        @SerializedName("accountSubStatus")
        var accountSubStatus : String? = null
        @SerializedName("message")
        var partiallyDunnedMessage: String? = null

        @SerializedName("devices")
        var devices : List<DeviceDetail>?=null

        @SerializedName("deviceSerialNumber")
        var deviceSerialNumber: String? = null

        @SerializedName("migrated")
        var migrated: Boolean = false

        @SerializedName("subscriptionInformationDTO")
        var subscriptionDetailInfo : CommonSubscriptionDetailsModel?=null

        //Freemium
        @SerializedName("parentalPinExist")
        var parentalPinExist: Boolean = true

        @SerializedName("parentalPinRating", alternate = ["ageRatingName"])//ageRatingName
        var ageRatingName: String? = null

        @SerializedName("ageRatingMasterMapping")
        var ageRatingMasterMapping: String? = null

        @SerializedName("loginFreeTrialAvailed")
        var loginFreeTrialAvailed: Boolean = true

        @SerializedName("dthStatus")
        var dthStatus: String = DTH_W_BINGE_OLD_USER

        @SerializedName("subscriptionStatus")
        var subscriptionStatus: String? = null

        @SerializedName("mixpanelid", alternate = ["mixpanelId"])
        var mixpanelid: String? = null

        @SerializedName("referenceId", alternate = ["referenceid"])
        var referenceId: String? = null

        @SerializedName("rechargeDueDate")
        var rechargeDueDate: String? = null

        @SerializedName("lastbillingType")
        var lastbillingType: String? = null

        @SerializedName("lastPackName")
        var lastPackName: String? = null

        @SerializedName("lastPackPrice")
        var lastPackPrice: String? = null

        @SerializedName("lastPackType")
        var lastPackType: String? = null

        @SerializedName("deviceDTOList")
        var deviceDTOList : List<DeviceDTOList>? = null


        @SerializedName("firstPaidPackSubscriptionDate")
        var firstPaidPackSubscriptionDate: String? = null

        @SerializedName("profileName")
        var profileName: String? = null

        @SerializedName("subscriberType")
        var subscriberType: String?= null

        @SerializedName("totalPaidPackRenewal")
        var totalPaidPackRenewal: String? = null

        @SerializedName("subscriptionStatusInfo")
        var subscriptionStatusInfo:SubscriptionStatusInfo? = null

        @SerializedName("primePackDetails")
        var primePackDetails: PrimePack? = null

        @SerializedName("helpCenterToken")
        var helpCenterToken: String? = null
    }


    inner class SubscriptionStatusInfo {
        @SerializedName("nonSubscribedToPack") // true: user has no pack or its current pack is expired, then we will take the user directly to the PG screen for the pack he selected before login.
        var nonSubscribedToPack: Boolean? = null

        @SerializedName("subscribedToSamePack") //True when pack is subscribed to the same pack.
        var subscribedToSamePack: Boolean? = null

        @SerializedName("nonSubscribedToSamePack") // True when pack is not subscribed to the same plan.
        var nonSubscribedToSamePack: Boolean? = null

        @SerializedName("newUser")
        var newUser: Boolean? = null

        @SerializedName("fdoRaised")
        var fdoRaised : Boolean? = null

        @SerializedName("loginToastFlag")
        var loginToastFlag : Boolean? = null // just show toast

        @SerializedName("loginToastMessage")
        var loginToastMessage : String? = null

        @SerializedName("allowPG")
        var allowPG : Boolean? = null

    }

    inner class DeviceDTOList {
        @SerializedName("deviceNumber")
        var deviceNumber: String? = null

        @SerializedName("SubscriptionId")
        var SubscriptionId: String? = null

        @SerializedName("deviceType")
        var deviceType: String? = null

        @SerializedName("deviceName")
        var deviceName: String? = null

        @SerializedName("isPrimary")
        var isPrimary: Boolean? = null

        @SerializedName("baId")
        var baId: String? = null

        @SerializedName("atvCancelled")
        var atvCancelled: Boolean? = null

        @SerializedName("ofsDevice")
        var ofsDevice: Boolean? = null

        @SerializedName("deviceStatus")
        var deviceStatus: String? = null
    }


    inner class ProfilePubNub {

        @SerializedName("id")
        var id: String? = null

        @SerializedName("profileName")
        var profileName: String? = null
        //bingeMobileAccounts

        @SerializedName("isDefaultProfile")
        var isDefaultProfile: Boolean = false

        @SerializedName("parentalPinExists")
        var parentalPinExists: Boolean = false

        @SerializedName("ageRatingName")
        var ageRatingName: String? = null

        @SerializedName("ageRatingMasterMapping")
        var ageRatingMasterMapping: String? = null

    }

    inner class CancelledDeviceInfo{
        @SerializedName("baId")
        var baId: String? = null

        @SerializedName("deviceCancellationFlag")
        var deviceCancellationFlag:Boolean  = false
    }

    inner class PubNubAccount{

        @SerializedName("deviceId")
        var deviceId: String? = null

        @SerializedName("baId")
        var baId: String? = null

        @SerializedName("status")
        var status: String? = null

        @SerializedName("deviceCancellationFlag")
        var deviceCancellationFlag:Boolean  = false

        @SerializedName("deviceType")
        var deviceType : String?=null

        @SerializedName("bingeAccountStatus")
        var bingeAccountStatus : String?= null

        @SerializedName("contentPlayBack")
        var contentPlayback : Boolean = true

        @SerializedName("logout")
        var forceLogout : Boolean = false

        @SerializedName("contentPlayBackHybrid")
        var contentPlayBackHybrid : Boolean = false

        @Transient
        @SerializedName("dontUseNow")
        var forceLogoutHybrid : Boolean = false

        @SerializedName("deviceList")
        var deviceList : List<String>?=null

        @SerializedName("deviceDetails")
        var deviceDetailsList:List<DeviceDetails>?=null

        @SerializedName("atvCancelled")
        val atvCancelled : Boolean = false

        @SerializedName("referenceId")
        val referenceId : String? = null
    }

    inner class DeviceDetails{
        @SerializedName("deviceId")
        @Expose
        var deviceId:String? = null
        @SerializedName("atvCancelled")
        @Expose
        var atvCancelled:Boolean? = null
    }

    inner class UserProfile {

        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("subscriberId")
        @Expose
        var subscriberId: String? = null

        @SerializedName("profileName")
        @Expose
        var profileName: String? = null

        @SerializedName("ageGroup")
        @Expose
        var ageGroup: Any? = null

        @SerializedName("gender")
        @Expose
        var gender: Any? = null

        @SerializedName("profilePic")
        @Expose
        var profilePic: Any? = null

        @SerializedName("isKidsProfile")
        @Expose
        var isKidsProfile: Boolean? = null

        @SerializedName("isDeleted")
        @Expose
        var isDeleted: Boolean? = null

        @SerializedName("defaultProfile")
        @Expose
        var defaultProfile: Boolean? = null
    }

    //this is to be used for getting parental control settings for "dth with Old binge stack" users
    inner class ComVivaSubscriberAgeRating {
        @SerializedName("profileId")
        var profileId: String? = null

        @SerializedName("isDefaultProfile")
        var isDefaultProfile: Boolean = false

        @SerializedName("parentalPinExists")
        var parentalPinExists: Boolean = false

        @SerializedName("ageRatingName")
        var ageRatingName: String? = null

        @SerializedName("ageRatingMasterMapping")
        var ageRatingMasterMapping: String? = null
    }
}


