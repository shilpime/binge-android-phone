package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class AddPackResponse : BaseResponse() {
    @SerializedName("data")
    var data: Data? = null

    inner class Data {
        @SerializedName("productType")
        var productType: String? = null // Eg. MYOP/Curated

        @SerializedName("basePackPrice")
        var basePackPrice: String? = null // Base pack price

        @SerializedName("selectedTenurePackPrice")
        var selectedTenurePackPrice: String? = null // Actual tenure price (Without deduction of prorated amount)

        @SerializedName("selectedTenureInDaysWithDSuffix") // 30D, 60D etc.
        var selectedTenureInDaysWithDSuffix: String? = null

        @SerializedName("selectedTenureType")
        var selectedTenureType: String? = null // Monthly, Quarterly, Yearly etc

        @SerializedName("modificationType")
        var modificationType: String? = null

        @SerializedName("firstPaidPackSubscriptionDate")
        var firstPaidPackSubscriptionDate: String? = null

        @SerializedName("payByDthWallet")
        var payByDthWalletData: PayByDthWalletData? = null

        @SerializedName("productId")
        var productId: String? = null

        @SerializedName("productName")
        var productName: String? = null

        @SerializedName("validityInDays")
        var validityInDays: String? = null

        @SerializedName("amount")
        var amount: String? = null // Actual payable amount (Tenure price - Prorated balance = Payable amount)

        @SerializedName("paymentStatusVerbiage")
        var paymentStatusVerbiage: PaymentStatusVerbiage? = PaymentStatusVerbiage()

        @SerializedName("paymentErrorVerbiages")
        var paymentErrorVerbiages : PaymentErrorVerbiages = PaymentErrorVerbiages()

        @SerializedName("paymentPayload")
        var paymentPayload: PaymentPayload? = PaymentPayload()

        @SerializedName("maxMandateAmount")
        var maxMandateAmount: String? = null

        @SerializedName("componentList")
        var componentList: List<ComponentList> = arrayListOf()

        @SerializedName("paymentTransactionId")
        var paymentTransactionId: String? = null

        @SerializedName("corelationId")
        var corelationId: String? = null

        @SerializedName("upFrontMoneyCollected")
        var upFrontMoneyCollected : Boolean ? = false

        @SerializedName("dth")
        var DTH : Boolean ? = false

    }

    data class PayByDthWalletData(
        @SerializedName("lowBalanceMessage") val walletPaymentVerbiage: String? = null,
        @SerializedName("lowBalance") val hasLowBalanceForThisTxn: Boolean? = null,
        @SerializedName("balance") val balance: String? = null
    )

    inner class PaymentStatusVerbiage {

        @SerializedName("header")
        var header: String? = null

        @SerializedName("footer")
        var footer: String? = null

        @SerializedName("message")
        var message: String? = null

    }

    inner class PaymentErrorVerbiages{
        @SerializedName("userCancelledVerbiage")
        var userCancelledVerbiage: String? = null

        @SerializedName("transactionPendingVerbiage")
        var transactionPendingVerbiage: String? = null

        @SerializedName("paymentFailureVerbiage")
        var paymentFailureVerbiage: String? = null

    }



    inner class Payload {

        @SerializedName("action")
        var action: String? = null

        @SerializedName("merchantId")
        var merchantId: String? = null

        @SerializedName("clientId")
        var clientId: String? = null

        @SerializedName("orderId")
        var orderId: String? = null

        @SerializedName("amount")
        var amount: String? = null

        @SerializedName("customerId")
        var customerId: String? = null

        @SerializedName("customerEmail")
        var customerEmail: String? = null

        @SerializedName("customerMobile")
        var customerMobile: String? = null

        @SerializedName("orderDetails")
        var orderDetails: String? = null

        @SerializedName("signature")
        var signature: String? = null

        @SerializedName("merchantKeyId")
        var merchantKeyId: String? = null

        @SerializedName("language")
        var language: String? = null

        @SerializedName("environment")
        var environment: String? = null

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

        @SerializedName("included")
        var included: Boolean? = null

        @SerializedName("premiumPartner")
        var premiumPartner: Boolean? = null

    }


    inner class PaymentPayload {

        @SerializedName("requestId")
        var requestId: String? = null

        @SerializedName("service")
        var service: String? = null

        @SerializedName("payload")
        var payload: Payload? = Payload()

    }


}