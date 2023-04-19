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
        @SerializedName("settings")
        var settings: Settings? = null
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

    data class Settings(
        @SerializedName("editProfile") val editProfile: String? = null,
        @SerializedName("videoLang") val videoLang: String? = null,
        @SerializedName("parentalLang") val parentalControl: String? = null,
        @SerializedName("autoPlay") val autoPlay: String? = null,
        @SerializedName("notificationSett") val notificationSett: String? = null,
        @SerializedName("transactionHist") val transactionHist: String? = null,
        @SerializedName("name") val name: String? = null,
        @SerializedName("email") val email: String? = null,
        @SerializedName("rmn") val rmn: String? = null,
        @SerializedName("save") val save: String? = null,
        @SerializedName("confirm") val confirm: String? = null,
        @SerializedName("choose") val choose: String? = null,
        @SerializedName("capture") val capture: String? = null,
        @SerializedName("from") val from: String? = null,
        @SerializedName("remove") val remove: String? = null,
        @SerializedName("close") val close: String? = null,
        @SerializedName("manageDevices") val manageDevices: String? = null,
        @SerializedName("loggedIn") val loggedIn: String? = null,
        @SerializedName("maxDevices") val maxDevices: String? = null,
        @SerializedName("mobileDevice") val mobileDevice: String? = null,
        @SerializedName("tvDevice") val tvDevice: String? = null,
        @SerializedName("thisDevice") val thisDevice: String? = null,
        @SerializedName("sureRemove") val sureRemove: String? = null,
        @SerializedName("ctaYes") val ctaYes: String? = null,
        @SerializedName("removeDevice") val removeDevice: String? = null,
        @SerializedName("primary") val primary: String? = null,
        @SerializedName("switchAccount") val switchAccount: String? = null,
        @SerializedName("logout") val logout: String? = null
    ) {
        fun getTrimmedVerbiages() = copy(
            editProfile = editProfile?.trim(),
            videoLang = videoLang?.trim(),
            parentalControl = parentalControl?.trim(),
            autoPlay = autoPlay?.trim(),
            notificationSett = notificationSett?.trim(),
            transactionHist = transactionHist?.trim(),
            name = name?.trim(),
            email = email?.trim(),
            rmn = rmn?.trim(),
            save = save?.trim(),
            confirm = confirm?.trim(),
            choose = choose?.trim(),
            capture = capture?.trim(),
            from = from?.trim(),
            remove = remove?.trim(),
            close = close?.trim(),
            manageDevices = manageDevices?.trim(),
            loggedIn = loggedIn?.trim(),
            maxDevices = maxDevices?.trim(),
            mobileDevice = mobileDevice?.trim(),
            tvDevice = tvDevice?.trim(),
            thisDevice = thisDevice?.trim(),
            sureRemove = sureRemove?.trim(),
            ctaYes = ctaYes?.trim(),
            removeDevice = removeDevice?.trim(),
            primary = primary?.trim(),
            switchAccount = switchAccount?.trim(),
            logout = logout?.trim()
        )
    }
}
