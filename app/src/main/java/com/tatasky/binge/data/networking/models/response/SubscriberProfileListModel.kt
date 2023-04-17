package com.tatasky.binge.data.networking.models.response

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

class SubscriberProfileListModel : BaseResponse() {

    @SerializedName("data")
    var userData: Data? = null

    @Parcelize
    class Data(

        @SerializedName("rmn")
        @Expose
        var rmn: String? = "",

        @SerializedName("baId")
        @Expose
        var id: String? = "",
        @SerializedName("firstName")
        @Expose
        var firstName: String? = "",
        @SerializedName("lastName")
        @Expose
        var lastName: String? = "",
        @SerializedName("profileImage")
        @Expose
        var image: String? = "",
        @SerializedName("email")
        @Expose
        var email: String? = "",
        @SerializedName("aliasName")
        @Expose
        var aliasName: String? = "T",
        @SerializedName("rechargeDueOn")
        @Expose
        var rechargeDueOn: String? = "",
        @SerializedName("balance")
        @Expose
        var balance: String? = "",
        @SerializedName("profileId")
        @Expose
        var profileId: String? = "",
        @SerializedName("deviceType")
        @Expose
        var deviceType: String? = "",
        @SerializedName("planName")
        @Expose
        var planName: String? = "Standard 299",
        @SerializedName("expireDate")
        @Expose
        var expireDate: String? = "09/07/20",
        @SerializedName("autoPlayTrailer")
        var isTrailerAutoPlay : Boolean = true,
        @SerializedName("watchNotification")
        var isWatchNotificationEnabled : Boolean = true,
        @SerializedName("transactionalNotification")
        var isTransactionalNotificationEnabled : Boolean = true,
        @Expose
        @SerializedName("languageList")
        val languageList : List<LangugageList> = ArrayList()

    ) : Parcelable


    @Parcelize
    class LangugageList(
        @SerializedName("id")
        @Expose
        var id: String = "",
        @SerializedName("name")
        @Expose
        var name: String = "") : Parcelable
}