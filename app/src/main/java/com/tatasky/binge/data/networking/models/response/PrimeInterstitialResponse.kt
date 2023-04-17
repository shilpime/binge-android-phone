package com.tatasky.binge.data.networking.models.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
class PrimeInterstitialResponse : BaseResponse(){

    @SerializedName("data")
    @Expose
    val data: Data? = null

    @Transient
    var packList : ArrayList<PrimePackItem> ?= null

    class Data {
        @SerializedName("id")
        @Expose
        val id: String? = null

        @SerializedName("image1")
        @Expose
        val image1: String? = null

        @SerializedName("image2")
        @Expose
        val image2: String? = null

        @SerializedName("image3")
        @Expose
        val image3: String? = null

        @SerializedName("image4")
        @Expose
        val image4: String? = null

        @SerializedName("firstInformation")
        @Expose
        val firstInformation: String? = null

        @SerializedName("secondInformation")
        @Expose
        val secondInformation: String? = null

        @SerializedName("thirdInformation")
        @Expose
        val thirdInformation: String? = null

        @SerializedName("fourthInformation")
        @Expose
        val fourthInformation: String? = null

        @SerializedName("fifthInformation")
        @Expose
        val fifthInformation: String? = null


        @SerializedName("sixthInformation")
        @Expose
        val sixthInformation: String? = null

        @SerializedName("benefits")
        @Expose
        val benefits: ArrayList<Benefits>? = null

        @SerializedName("type")
        @Expose
        val type: String? = null

        @SerializedName("firstPrimeLogo")
        @Expose
        val firstPrimeLogo: String? = null

        @SerializedName("secondPrimeLogo")
        @Expose
        val secondPrimeLogo: String? = null


    }
    class Benefits{
        @SerializedName("benefitImage")
        @Expose
        val benefitImage: String? = null

        @SerializedName("benefitText")
        @Expose
        val benefitText: String? = null
    }

}