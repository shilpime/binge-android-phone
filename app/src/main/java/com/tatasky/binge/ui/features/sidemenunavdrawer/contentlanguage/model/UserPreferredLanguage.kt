package com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage.model

import android.annotation.SuppressLint
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.data.networking.models.response.BaseResponse
import com.tatasky.binge.data.networking.models.response.ContentItem

@SuppressLint("ParcelCreator")
data class UserPreferredLanguage(
    @SerializedName("data")
    val data: Data
):BaseResponse()


data class Data(
    @SerializedName("profileList")
    val profileList: List<Profile>? = null
)

data class Profile(
    @SerializedName("profileId")
    val profileId:String,
    @SerializedName("preferredLanguages")
    val preferredLanguages: List<Languages>
)

data class Languages(
    @SerializedName("id")
    val id:Int,
    @SerializedName("name")
    val name: String
)
