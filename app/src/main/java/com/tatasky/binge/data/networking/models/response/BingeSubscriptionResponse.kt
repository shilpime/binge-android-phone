package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

/**
 * Created by Srikant Karnani on 4/3/20.
 */
class BingeSubscriptionResponse : BaseResponse() {
    @SerializedName("data")
    var transactionDetail: TransactionDetail? = null
    inner class TransactionDetail {
        @SerializedName("subscriberId")
        var sId: String? = null
        @SerializedName("transactionId")
        var transactionId: String? = null
    }
}