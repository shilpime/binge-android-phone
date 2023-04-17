package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

data class TaRelatedRail(
    @SerializedName("useCase") val useCase : String,
    @SerializedName("contentType") val contentType : String)
