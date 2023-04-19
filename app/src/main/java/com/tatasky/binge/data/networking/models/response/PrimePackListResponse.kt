package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName


@SuppressLint("ParcelCreator")
class PrimePackListResponse : BaseResponse() {
	@SerializedName("data")
	var data: ArrayList<PrimePackItem>? = null
}