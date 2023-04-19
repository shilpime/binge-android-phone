package com.tatasky.binge.ui.features.player.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.data.networking.models.response.ConfigResponse

class BitrateData {


    @SerializedName("vod")
    @Expose
    var vod: Vod? = null

}

class Vod {

    @SerializedName("sd")
    @Expose
    var sd: Sd? = null
    @SerializedName("hd")
    @Expose
    var hd: Hd? = null

}



class Sd {

    @SerializedName("low")
    @Expose
    var low: Int? = null
    @SerializedName("medium")
    @Expose
    var medium: Int? = null
    @SerializedName("high")
    @Expose
    var high: Int? = null
    @SerializedName("def")
    @Expose
    var def: Int? = null

}

class Hd {

    @SerializedName("low")
    @Expose
    var low: Int? = null
    @SerializedName("medium")
    @Expose
    var medium: Int? = null
    @SerializedName("high")
    @Expose
    var high: Int? = null
    @SerializedName("def")
    @Expose
    var def: Int? = null

}