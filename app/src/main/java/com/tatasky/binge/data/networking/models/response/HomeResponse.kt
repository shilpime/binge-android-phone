package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.tatasky.binge.utils.isValidItems
import com.tatasky.binge.utils.isValidRentalContent


class HomeResponse : BaseResponse() {
    @SerializedName("data")
    @Expose
    var data: Data? = null

    class Data {

        @SerializedName("total")
        @Expose
        var total: Int = 0

        @SerializedName("offset")
        @Expose
        var offset: Int = 0

        @SerializedName("limit")
        @Expose
        var limit: String? = null

        @SerializedName("items")
        @Expose
        var items: MutableList<Items>? = null
        var dthStatus : String? = null
        val finalItems: MutableList<Items>
            get() = items!!.filter { isValidItems(it, dthStatus) } as MutableList<Items>
        //get() = items!!.filter { isValidItems(it) }
    }

    class Items {

        @SerializedName("packName")
        var packName: String = "" // used for pack Specific rails

        //Action added to provide packselection through deeplink in CT integration
        @SerializedName("action")
        var action: String = ""

        @SerializedName("recommendationPosition")
        var recommendationPosition : String?= null
        //searchPageName
        @SerializedName("searchPageName")
        val searchPageName : String? = null
        @SerializedName("itemCount")
        var itemCount : Int = 0
        @SerializedName("intentUrl")
        val intentUrl : String? = null
        @SerializedName("genreType")
        var genreType: String? = null
        @SerializedName("actionItemTittle")
        val actionItemTittle : String? = null
        @SerializedName("languageType")
        var languageType: String? = null
        var isBanner = false
        var providerId = ""
        var providerImg : String = "http://res.cloudinary.com/tatasky/image/fetch/https://s3.ap-south-1.amazonaws.com/tatasky-production-cms/cms-ui/images/zee_new_ixdpbo.png"
        fun getDescription() : String {
            return "$title is a streaming service that offers a wide variety of award-winning TV shows, movies, anime, documentaries and more."
        }
        fun getUnsubscribedTitle(): String {

            return "$title   |   Not Subscribed"
        }
        fun getBtnTitle(): String {

            return "Add $title to My Subscription"
        }
        var viewType: Int = 0

        @SerializedName("id", alternate = ["railId"])
        var id: Int = 0

        @SerializedName("refId")
        var refId : String = ""

        @SerializedName("title", alternate = ["railTitle"])
        var title: String = ""

        @SerializedName("sectionType")
        var sectionType: String = ""
        @SerializedName("sectionSource")
        var sectionSource: String = ""

        @SerializedName("layoutType")
        var layoutType: String = ""

        @SerializedName(value = "contentList", alternate = ["contentResults", "items","list"])
        var contentItem: ArrayList<ContentItem> = ArrayList()

        @SerializedName(value = "shuffleList")
        var shuffleList: ArrayList<PartnerData> = ArrayList()

        @SerializedName("taFallbackContentList")
        var taFallbackContentList: ArrayList<ContentItem>? = null

        var filteredShuffleList: List<PartnerData> = ArrayList()
            get() = shuffleList?.filter { checkEmptyList(it.filteredContentItems) }

        var filteredContentItems: List<ContentItem> = ArrayList()
            get() = contentItem.filter { isValidRentalContent(it, dthStatus, sectionSource, sectionType, refId ) }

        var dthStatus : String? = null
        var filteredProvider: List<ContentItem> = ArrayList()
            get() = filteredContentItems.filter { checkEmptyValue(it) }

        private fun checkEmptyValue(it: ContentItem): Boolean {
            return it.contentItem.size > 0
        }
        private fun checkEmptyList(it: List<ContentItem>): Boolean {
            return it.isNotEmpty()
        }

        @SerializedName("totalCount", alternate = ["totalSearchCount"])
        var totalCount: Int = 0

        @SerializedName("autoScroll")
        var isAutoScroll: Boolean = false
        @SerializedName("placeHolder")
        var placeHolder: String = ""
        @SerializedName("continueWatching")
        var continueWatching: Boolean = false
        @SerializedName("pagingState")
        var pagingState: String? = null
        @SerializedName("continuePagination", alternate = ["continuePaging"])
        var continuePagination : Boolean = false
        @SerializedName("configType")
        var configType:String?=null
        var lastPosition = 0

        @SerializedName("trendingProvider")
        var trendingProvider:String?=null

        @SerializedName("provider")
        var provider:String? = null

        @SerializedName("trendingContent")
        var trendingContent:String?=null

        @SerializedName("backgroundImage")
        var backgroundImage:String=""

        @SerializedName("maxLanguageErrorMessage")
        var maxLanguageErrorMessage:String = ""

        @SerializedName("maxLanguageAllowed")
        var maxLanguageAllowed:Int? = null

        var shuffleIndex = 0
    }
}