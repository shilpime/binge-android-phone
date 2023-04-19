package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

/**
 * Created by Srikant on 27/05/21.
 */

@SuppressLint("ParcelCreator")
class PrimeActivationResponse : BaseResponse() {
	@SerializedName("data")
	var data: Data? = null

	class Data {
		@SerializedName("url")
		var url: String? = null
	}
}