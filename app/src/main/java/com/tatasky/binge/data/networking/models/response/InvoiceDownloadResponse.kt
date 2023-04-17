package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class InvoiceDownloadResponse :BaseResponse(){
    @SerializedName("data")
    @Expose
    var data: Data? = null

    class Data{
        @SerializedName("accountId")
        @Expose
        var accountId : String ?= null

        @SerializedName("invoiceNo")
        @Expose
        var invoiceNo : String ?= null

        @SerializedName("paymentInvoice")
        @Expose
        var paymentInvoice : String ?= ""

        @SerializedName("fileName")
        @Expose
        var fileName : String ?= null

        @SerializedName("baId")
        @Expose
        var baId : String ?= null

    }



}