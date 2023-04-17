package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

class InvoiceDownloadRequest(
    @SerializedName("invoiceNo") var invoiceNo:String?="")