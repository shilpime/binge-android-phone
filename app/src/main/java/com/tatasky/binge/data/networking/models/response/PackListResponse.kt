package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class PackListResponse constructor() : BaseResponse() {
    @SerializedName("data")
    var data: PackListData? = null

    inner class PackListData {
        @SerializedName("verbiage")
        var verbiage: Verbiage? = null

        @SerializedName("expiryMessage")
        var expiryMessage:String? = null

        @SerializedName("packList")
        var packsList: List<PartnerPacks> = emptyList()
        @SerializedName("defaultPackId")
        var defaultPackSelectedId: String? = null
        @SerializedName("title")
        var title: String? = null
        @SerializedName("value")
        var subTitle: String? = null
        @SerializedName("sufficientBalance")
        var sufficientBalance: Boolean = true
        @SerializedName("offerEligibility")
        var additionalAppsInfo: List<OfferEligiblePacks>? = null
        @SerializedName("subscriberAccountStatus")
        var dthAccountStatus: String? = null
        @SerializedName("accountSubStatus")
        var accountSubStatus : String? = null
        @SerializedName("message")
        var partiallyDunnedMessage: String? = null
        @SerializedName("amount")
        var lowBalanceAmount: String? = null
        @SerializedName("eligibleForUpgrade")
        var firestickAvilableAtPinCode: Boolean = false
    }


    inner class Verbiage{
        @SerializedName("title")
        var title:String? = null

        @SerializedName("desc")
        var desc: String? = null

        @SerializedName("recommendedMessage")
        var recomendedMessage:String? = null

        @SerializedName("footerMessage")
        var footerMessage : String? = null
    }
}