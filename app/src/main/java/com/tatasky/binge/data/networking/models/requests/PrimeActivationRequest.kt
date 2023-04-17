package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

/**
 * Created by Srikant on 28/05/21.
 */
data class PrimeActivationRequest(
	@SerializedName("primePackType") val primePackType: String,
	@SerializedName("subscriberId") val subscriberId: String
)