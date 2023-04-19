package com.tatasky.binge.data.networking.models.response

import kotlinx.android.parcel.Parcelize


@Parcelize
class AppleRedemptionResponse: BaseResponse(){
    var data:Data?=null
    data class Data(val redemption_url: String)
}
