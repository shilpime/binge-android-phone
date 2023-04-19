package com.tatasky.binge.voot.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.data.networking.models.response.BaseResponse
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.utils.*

class VootPlayebackResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data:Data? = null

    class Data{
        /*{
            "FileID": "771021",
            "URL": "https://cdnapisec.kaltura.com/p/1982541/sp/198254100/playManifest/protocol/https/entryId/0_86dvo1ec/format/mpegdash/tags/mobile_sd/f/a.mpd",
            "Duration": 30,
            "Format": "DASH_Mobile_SD",
            "IsDefaultLang": false,
            "CoGuid": "0_86dvo1ec_0_r516vjwj,0_86dvo1ec_0_or4ghfxc,0_86dvo1ec_0_qbur6hff"
        }*/
        @SerializedName("id")
        @Expose
        var id: String? = null

        @SerializedName("url")
        @Expose
        var url: String? = null

        @SerializedName("duration")
        @Expose
        var duration: String? = null

        @SerializedName("format")
        @Expose
        var format: String? = null

        @SerializedName("drm")
        @Expose
        var drm : List<DRM>? = null

        var filteredDRM: List<DRM>? = ArrayList()
            get() = drm?.filter { isValidDRM(it) }

        private fun isValidDRM(drm: DRM): Boolean {
            return "WIDEVINE_CENC".equals(drm.scheme, true)
        }
    }
    class DRM{
        @SerializedName("licenseURL")
        @Expose
        var licenseURL: String? = null
        @SerializedName("scheme")
        @Expose
        var scheme: String? = null
    }
}