package com.tatasky.binge.data.networking.models.response

import kotlinx.android.parcel.Parcelize


@Parcelize
class ManagedAppResponse: BaseResponse(){
    var data:Data?=null
    data class Data(val accessToken: String, val ttl:String, val href:String, val checksum:String="")
}
