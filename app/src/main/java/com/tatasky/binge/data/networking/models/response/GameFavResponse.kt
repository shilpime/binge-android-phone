package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

class GameFavResponse : BaseResponse() {
    @SerializedName("data")
    var data: Data? = null


    data class Data (
        @SerializedName("status") var status : Boolean? = null ,
        @SerializedName("totalCount"     ) var totalCount     : Int?            = null,
        @SerializedName("continuePaging" ) var continuePaging : Boolean?        = null,
        @SerializedName("pagingState"    ) var pagingState    : String?         = null,
        @SerializedName("configType"     ) var configType     : String?         = null,
        @SerializedName("list"           ) var list           : ArrayList<ContentItem> = arrayListOf()

    )
}