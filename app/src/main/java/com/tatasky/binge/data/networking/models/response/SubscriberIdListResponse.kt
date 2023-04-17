package com.tatasky.binge.data.networking.models.response

import android.annotation.SuppressLint
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.tatasky.binge.utils.DTH_WO_BINGE_USER
import java.lang.reflect.Type
import java.util.*


@SuppressLint("ParcelCreator")
class SubscriberIdListResponse() : BaseResponse() {
    @SerializedName("data")
//    val data : Data? = null
    var subscribersList: List<SubscriberDetail>? = null

    inner class Data{
        /*@SerializedName("dthStatus")
        var dthStatus: String? = null

        @SerializedName("nonDthSubscribersList")//nonDthSubscribersList
        var nonDthSubscribersList: List<SubscriberDetail>? = null*/

        @SerializedName("subscribersList")
        var subscribersList: List<SubscriberDetail>? = null
    }

    inner class SubscriberDetail {
        @SerializedName("rmn",  alternate = ["mobileNumber"])
        var rmn: String? = null

        @SerializedName("subscriberName")
        var name: String? = null

        @SerializedName("email", alternate = ["emailId"])
        var email: String? = null

        @SerializedName("subscriberId")
        var sid: String = ""

        @SerializedName("bingeSubscriberId")
        var bingeSubscriberId: String? = null

        @SerializedName("accountStatus")
        var accountStatus: String? = null

        @SerializedName("accountSubStatus")
        var accountSubStatus : String? = null

        @SerializedName("accountDetailList", alternate = ["accountDetailsDTOList"])
        var listOfBaIds: List<LoginResponse.BingeSubscription> = mutableListOf()

        @SerializedName("loginErrorMessage")
        var loginErrorMsg : String?=null

        @SerializedName("statusType")
        var statusType : String?  = null

        @SerializedName("bingeSubscriptionMessage")
        var bingeSubscriptionMessage : String? = null

        @SerializedName("bingeSubscriptionHeader")
        var bingeSubscriptionHeader : String? = null

        @SerializedName("dthStatus")
        var dthStatus: String? = null

        @SerializedName("isPastBingeUser")
        val isPastBingeUser : Boolean? = true

        @SerializedName("referenceId", alternate = ["referenceid"])
        var referenceId: String? = null
//        val bingeDeactivated
//            get() = listOfBaIds.all { it.bingeAccountStatus.equals(AccountStatusEnum.DEACTIVATED.status, true) }
//        val bingeActive
//            get() = listOfBaIds.all { it.bingeAccountStatus.equals(AccountStatusEnum.ACTIVE.status, true) }

        val bingeDeactivated
            get() = true == statusType?.equals("inactive" , true)
        val bingeActive
            get() = true == statusType?.equals("active" , true)
    }

    internal class MyObjectDeserializer : JsonDeserializer<SubscriberIdListResponse?> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): SubscriberIdListResponse {
            val jObj = json.asJsonObject
            val jObj2 = jObj["data"]
            if (jObj2.isJsonArray) {
//                val jElement2 = jObj2.asJsonObject["subscribersList"]//asJsonArray("subscribersList")
                var moreTags : List<SubscriberDetail>? = Collections.emptyList()
//                if(jElement2.isJsonArray){
                    moreTags = context.deserialize<List<SubscriberDetail>>(jObj2.asJsonArray, object : TypeToken<List<SubscriberDetail>?>() {}.type)
//                }

                //assuming there is an appropriate constructor
                return SubscriberIdListResponse().apply {
                    code = jObj["code"].asInt
                    message = jObj["message"].asString
//                    data?.nonDthSubscribersList = tags
                    subscribersList = moreTags
                }
            }
            return SubscriberIdListResponse()
        }
    }
}