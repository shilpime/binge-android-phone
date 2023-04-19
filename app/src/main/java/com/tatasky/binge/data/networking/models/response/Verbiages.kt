package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName


data class Verbiages (

    @SerializedName("categoryName" ) var categoryName : String? = null,
    @SerializedName("data") var data: VerbiageData = VerbiageData()
)

data class VerbiageData (
    @SerializedName("header") var header: String? = null,
    @SerializedName("subHeader") var subHeader : String? = null,
    @SerializedName("others") var others: Others = Others()
)

data class Others (
    @SerializedName("buttonTitle") var buttonTitle : String? = null,
    @SerializedName("exitButtonTitle") var exitButtonTitle : String? = null,
    val buttonHeader : String? = null
)
