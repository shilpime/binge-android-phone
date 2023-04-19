package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ProviderLogo {

    @SerializedName("SONYLIV")
    @Expose
    var SONYLIV: APP_NAME? = null

    @SerializedName("ZEE5", alternate = ["Zee5"])
    @Expose
    var ZEE5: APP_NAME? = null

    @SerializedName("PRIME", alternate = ["Prime"])
    @Expose
    var PRIME: APP_NAME? = null

    @SerializedName("SHEMAROOME", alternate = ["Shemaroome"])
    @Expose
    var SHEMAROOME: APP_NAME? = null

    @SerializedName("NETFLIX", alternate = ["Netflix"])
    @Expose
    var NETFLIX: APP_NAME? = null

    @SerializedName("HUNGAMA", alternate = ["Hungama"])
    @Expose
    var HUNGAMA: APP_NAME? = null

    @SerializedName("HOTSTAR", alternate = ["Hotstar"])
    @Expose
    var HOTSTAR: APP_NAME? = null

    @SerializedName("SUNNXT", alternate = ["SunNxt"])
    @Expose
    var SUNNXT: APP_NAME? = null

    @SerializedName("EROSNOW", alternate = ["ErosNow"])
    @Expose
    var EROSNOW: APP_NAME? = null

    @SerializedName("TATASKY")
    @Expose
    var TATASKY: APP_NAME? = null

    @SerializedName("VOOTSELECT", alternate = ["VootSelect"])
    @Expose
    var VOOTSELECT: APP_NAME? = null

    @SerializedName("VOOTKIDS", alternate = ["VootKids"])
    @Expose
    var VOOTKIDS: APP_NAME? = null

    @SerializedName("CURIOSITYSTREAM", alternate = ["CuriosityStream"])
    @Expose
    var CuriosityStream: APP_NAME? = null

    @SerializedName("EPICON")
    @Expose
    var EPIC_ON: APP_NAME? = null

    @SerializedName("DOCUBAY")
    @Expose
    var DOCU_BAY: APP_NAME? = null

    @SerializedName("HOICHOI")
    @Expose
    var HOICHOI: APP_NAME? = null

    @SerializedName("MXPLAYER")
    @Expose
    var MXPLAYER: APP_NAME? = null

    @SerializedName("CHAUPAL")
    @Expose
    var CHAUPAL: APP_NAME? = null

    @SerializedName("PLANETMARATHI")
    @Expose
    var PLANETMARATHI: APP_NAME? = null

    @SerializedName("fdbsbd")
    @Expose
    var NAMMAFLIX: APP_NAME? = null

    @SerializedName("GAMEZOP")
    @Expose
    var GAMEZOP: APP_NAME? = null

    @SerializedName("LIONSGATE")
    @Expose
    var LIONSGATE: APP_NAME? = null
}

class APP_NAME {
    @SerializedName("unsubscribedMob")
    @Expose
    var unsubscribedMob: String? = null

    @SerializedName("unsubscribedWeb")
    @Expose
    var unsubscribedWeb: String? = null

    @SerializedName("logoRectangular")
    @Expose
    var logoRectangular: String? = null

    @SerializedName("logoCircular")//logoCircular
    @Expose
    var logoCircular: String? = null
}

class PackButton {
    @SerializedName("modify")
    @Expose
    var modifyButton: Boolean = false

    @SerializedName("cancellation")
    @Expose
    var cancelButton: Boolean = false
}