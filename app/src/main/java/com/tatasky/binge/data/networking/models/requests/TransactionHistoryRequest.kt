package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class TransactionHistoryRequest(
        @SerializedName("baId") val baId: String?,
        @SerializedName("dsn") val dsn: String?,
        @SerializedName("limit") val limit: String?,
        @SerializedName("offset") val offset: String?
)
