package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.model.Languages

@SuppressLint("ParcelCreator")
class AnonymousResponse : BaseResponse() {
    @SerializedName("data")
    var data: Data? = null

    inner class Data {
        @SerializedName("anonymousId")
        var anonymousId: String? = null
        @SerializedName("profileId")
        var profileId: String? = null
        @SerializedName("preferredLanguages")
        var preferredLanguages: List<Languages>? = null
        @SerializedName("gAuthToken")
        var gAuthToken: String? = null
    }
}