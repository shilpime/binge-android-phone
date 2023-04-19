package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.SerializedName

/**
 * Created by Srikant Karnani on 24/10/19.
 */
class ToggleFavouriteResponse : BaseResponse() {

    @SerializedName("data")
    var data: Data? = null
    @SerializedName("isFavourite")
    var isFavourite: Boolean = false

    inner class Data {
    }
}
