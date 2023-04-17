package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Created by Srikant on 03/06/21.
 */
class PrimePubnubDTO {
	@SerializedName("type")
	@Expose
	var type: String? = null

	@SerializedName("planId")
	@Expose
	var primePackId: String? = null

	@SerializedName("status")
	@Expose
	var primeStatus: String? = null
}