package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class OfferIds {
    @SerializedName("epids")
    val epids : List<Epid>?= null
}

class Epid {
    @SerializedName("epid")
    val epid : String = "Rental"
    @SerializedName("bid")
    val bid : String = "19507"
}
