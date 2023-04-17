package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName

@SuppressLint("ParcelCreator")
class MigrateUserTickTickResponse : BaseResponse() {

    @SerializedName("data")
    var data: PubnubResponse? = null


}