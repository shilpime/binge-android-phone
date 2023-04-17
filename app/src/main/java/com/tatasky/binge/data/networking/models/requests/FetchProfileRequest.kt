package com.tatasky.binge.data.networking.models.requests

import com.google.gson.annotations.SerializedName

data class FetchProfileRequest(@SerializedName("baId") val baId: String, @SerializedName("rmn") val rmn: String)