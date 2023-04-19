package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class DetailsResponse constructor(): BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    constructor(meta: MetaDetails, detail: Detail) : this() {
        data = Data()
        data!!.metaDetails = meta
        data!!.detail = detail
    }
    class Data {
        @SerializedName("meta")
        @Expose
        var metaDetails: MetaDetails? = null

        @SerializedName("detail")
        @Expose
        var detail: Detail? = null

        @SerializedName("seriesList")
        @Expose
        val seriesList: List<SeriesList>? = null

        /*@SerializedName("lastWatch")
        @Expose
        var lastWatched: ContentItem? = null*/

        @SerializedName("nextEpisodeId")
        var nextEpisodeId : String ="0"
        @SerializedName("previousEpisodeId")
        var previousEpisodeId : String ="0"

    }
    class SeriesList{

        @SerializedName("id")
        @Expose
        val id: String? = null

        @SerializedName("seriesName")
        @Expose
        val seriesName: String? = null
    }

}