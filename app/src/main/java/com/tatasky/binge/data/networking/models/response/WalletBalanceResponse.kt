package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class WalletBalanceResponse : BaseResponse() {

    @SerializedName("data")
    var data: Data? = null

    data class Data(
        @SerializedName("daysLeft") val daysLeft: String?,
        @SerializedName("proRataAmount") val proRataAmount: String="0",
        @SerializedName("recommendedAmount") val recommendedRechargeAmount: String="0",
        @SerializedName("minimumRecharge") val minimumRechargeAmount: String="0",
        @SerializedName("bingeSubscriptionAmount") val bingeSubscriptionAmount: String?=null,
        @SerializedName("dbrAmount") val dbrAmount: String?=null,
        @SerializedName("retrofitAmount") val retrofitAmount : String?=null,
        @SerializedName("totalAmount") val totalAmount : String?=null,
        @SerializedName("message") val message : String?=null,
        @SerializedName("balanceQueryRespDTO") val balanceQueryRespDTO: BalanceQueryResponse?,
        @SerializedName("bingeSubscriptionAmountList") val mapOfBingeSubscription : List<Map<String?, String?>?>?,
        @SerializedName("lowBalance") val hasLowBalanceForThisTxn : Boolean? = null,
        @SerializedName("endDateVerbiage") val endDateVerbiage:String? = null,
        @SerializedName("highlightDateVerbiage") val highlightDateVerbiage:Boolean = false,
        @SerializedName("lowBalanceMessage") val walletPaymentVerbiage : String? = null
    )

    data class BalanceQueryResponse(
        @SerializedName("balance") val balance: String,
        @SerializedName("dbr") val dbr: String,
        @SerializedName("endDate") val endDate: String,
        @SerializedName("mbr") val mbr: String,
        @SerializedName("subscriberId") val subscriberId: String
    )
}