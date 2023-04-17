package com.tatasky.binge.ui.features.recharge

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.data.networking.models.response.BaseResponse

@SuppressLint("ParcelCreator")
data class JuspayInitiationResponse(
    @SerializedName("data")
    val data: Data,
) : BaseResponse()

data class Data(
    @SerializedName("payload")
    val payload: Payload? = null,
    @SerializedName("requestId")
    val requestId: String = "",
    @SerializedName("service")
    val service: String = "",
)

data class Payload(
    @SerializedName("action")
    val action: String,
    @SerializedName("clientId")
    val clientId: String,
    @SerializedName("customerId")
    val customerId: String,
    @SerializedName("environment")
    val environment: String,
    @SerializedName("merchantId")
    val merchantId: String,
    @SerializedName("merchantKeyId")
    val merchantKeyId: String,
    @SerializedName("signature")
    val signature: String,
    @SerializedName("signaturePayload")
    val signaturePayload: String
)