package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class FreemiumCurrentSusbcriptionResponse {

    @SerializedName("code")
    var code: Int? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("data")
    var data: Data? = Data()


    inner class Data {
        @SerializedName("productId")
        var productId: String? = null

        @SerializedName("productName")
        var productName: String? = null

        @SerializedName("amount")
        var amount: String? = null

        @SerializedName("packValidity")
        var packValidity: String? = null

        @SerializedName("expiryFooterMessage")
        var expiryFooterMessage: String? = null

        @SerializedName("PlanOtpions")
        var PlanOtpions: PlanOtpions? = PlanOtpions()

        @SerializedName("componentList")
        var componentList: List<ComponentList> = arrayListOf()

    }

    inner class ComponentList {

        @SerializedName("componentId")
        var componentId: String? = null

        @SerializedName("componentName")
        var componentName: String? = null

        @SerializedName("numberOfApps")
        var numberOfApps: String? = null

        @SerializedName("partnerList")
        var partnerList: List<PartnerList> = arrayListOf()
    }


    inner class PartnerList {

        @SerializedName("partnerId")
        var partnerId: String? = null
        @SerializedName("partnerName")
        var partnerName: String? = null
        @SerializedName("iconUrl")
        var iconUrl: String? = null

    }

    inner class PlanOtpions {

        @SerializedName("RenewPlanOption")
        var RenewPlanOption: Boolean? = null

        @SerializedName("RenewPlanMessage")
        var RenewPlanMessage: String? = null

        @SerializedName("changePlanOption")
        var changePlanOption: Boolean? = null

        @SerializedName("changePlanMessage")
        var changePlanMessage: String? = null

        @SerializedName("changeTenureMessage")
        var changeTenureMessage: String? = null

        @SerializedName("changeTenureOption")
        var changeTenureOption: Boolean? = null

        @SerializedName("canelPlanOption")
        var canelPlanOption: Boolean? = null

        @SerializedName("canelPlanMessage")
        var canelPlanMessage: String? = null

    }

}