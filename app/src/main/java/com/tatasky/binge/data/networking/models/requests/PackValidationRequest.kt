package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class PackValidationRequest(
    @SerializedName("packId") var packId: String? = null,
    @SerializedName("baId") var baId: String? = null
)