package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class ManagedAppDrawerResponse : BaseResponse() {

    @SerializedName("data")
    var data: DrawerResponse? = null

    inner class DrawerResponse{
        @SerializedName("verbiage")
        var verbiage: Verbiage? = null
        @SerializedName("partnerList")
        private var _partnerList: List<PartnerPacks.PartnerList>? = null

    }

    inner class Verbiage{
        @SerializedName("title")
        var title:String? = null

        @SerializedName("desc")
        var desc: String? = null

        @SerializedName("makePlanText")
        var makePlanText: String? = null
        @SerializedName("valuePlanText")
        var valuePlanText: String? = null
    }

    inner class TickTickDrawerDetail{
        @SerializedName("baseAmountColorValue")
        var baseAmountColorValue: String? = null

        @SerializedName("ottAppsTitle")
        var ottAppsTitle:String? = null

        @SerializedName("colorTitleValue")
        var colorTitleValue: String? = null

        @SerializedName("ownPack")
        var ownPack: String? = null

        @SerializedName("valuePack")
        var valuePack: String? = null

        @SerializedName("drawerFixedPlanCTA")
        var drawerFixedPlanCTA: String? = null

        @SerializedName("moreApp")
        var moreApp: String? = null

        @SerializedName("plusImage")
        var plusImage: String? = null

        @SerializedName("valuePackImage")
        var valuePackImage: String? = null

        @SerializedName("planStartMessageShort")
        var planStartMessageShort: String? = null

        @SerializedName("openTickTickDrawer")
        var openTickTickDrawer: Boolean = false

        @SerializedName("redirectionUrl")
        var redirectionUrl: String? = ""

        @SerializedName("termsAndConditions")
        var termsAndConditions: String? = ""

        @SerializedName("partnersImage")
        var partnersImage: List<PartnerPacks.PartnerList> = arrayListOf()
    }
}