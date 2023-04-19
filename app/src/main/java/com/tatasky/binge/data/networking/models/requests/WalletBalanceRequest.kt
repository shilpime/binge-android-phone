package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class WalletBalanceRequest(
    @SerializedName("baId") var baid: String,
    @SerializedName("upgradePackId") var packId: String?=null,
    @SerializedName("proratedAmount") var proratedAmount : String? = null
)