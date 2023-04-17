package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TransactionHistoryList {
    @SerializedName("transactionId")
    @Expose
    var transactionId: String? = null

    @SerializedName("amount", alternate = ["transactionAmount"])
    @Expose
    var amount: String? = null

    @SerializedName("description", alternate = ["remarks"])
    @Expose
    var description: String? = null

    @SerializedName("date", alternate = ["transactionDate"])
    @Expose
    var date: String? = null

    @SerializedName("expiryDate")
    @Expose
    var expiryDate: String? = null

    @SerializedName("paymentModeVerbiage")
    @Expose
    var paymentModeVerbiage: String? = null

    @SerializedName("paymentMethodType")
    @Expose
    var paymentMethodType: String? = null

    @SerializedName("invoiceNo")
    @Expose
    var invoiceNo: String? = null

    @SerializedName("webUrl")
    @Expose
    var weburl:String?=""

}