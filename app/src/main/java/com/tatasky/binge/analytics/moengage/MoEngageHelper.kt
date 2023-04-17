package com.tatasky.binge.analytics.moengage

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.inbox.CTInboxMessage
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tatasky.binge.utils.Properties
import com.moengage.core.analytics.MoEAnalyticsHelper
import com.moengage.core.internal.data.events.EventUtils
import com.moengage.inbox.core.MoEInboxHelper
import com.moengage.inbox.core.model.InboxData
import com.moengage.inbox.core.model.InboxMessage
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.SUBSCRIBED
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.utils.*
import io.reactivex.Single
import org.json.JSONObject
import java.util.*
import javax.inject.Singleton
import kotlin.collections.HashMap
import kotlin.concurrent.thread
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList


@Singleton
class MoEngageHelper(private val context: Context) {

    private var mMoEHelper: MoEAnalyticsHelper = MoEAnalyticsHelper
    //An array of events which needs to be excluded from CT event logging but are part of ME
    private val redundantEventFilter = arrayOf<String>(EVENT_APP_LAUNCH)
    fun registerInitialSuperProperties(
        sid: String,
        userDetails: LoginResponse.BingeSubscription,
        aid: String?,
        burnRateType: String
    ) {
        val initialSuperProperties  = HashMap<String, Any>()
        var uniqueIdCleverTap: String = "";

        try {
            initialSuperProperties[SID] = sid
            initialSuperProperties[ANONYMOUS_ID] = aid?:""
//            initialSuperProperties[PLATFORM] = MOE_PLATFORM
            userDetails.let {
                initialSuperProperties[PROFILE_ID] = it.profileId?:""

                /////////===========clevertap stars==========///////
                initialSuperProperties["MSG-push"] = true
                initialSuperProperties[NAME_CLEVER] =
                    (it.firstName ?: "") + " " + (it.lastName ?: "")
                initialSuperProperties[PHONE_CLEVER] = "+91" + (it.rmn ?: "")
                initialSuperProperties[EMAIL_CLEVER] = it.emailId ?: ""


                it.dthStatus.let { dthStatus->
                    when{

                        (dthStatus != DTH_W_BINGE_OLD_USER) ->{
                            if(it.subscriberId?.isNotEmpty() == true){
                                it.subscriberId?.let { identity->
                                    uniqueIdCleverTap=identity
                                }
                            }
                        }else ->{
                        uniqueIdCleverTap=sid
                    }
                    }
                }
                ////////============clevertap ends =========////////

                initialSuperProperties[RMN] = it.rmn?:""
                initialSuperProperties[EMAIL] = it.emailId?:""
                initialSuperProperties[FNAME] = it.firstName?:""
                initialSuperProperties[LNAME] = it.lastName?:""

                if (it.subscriptionType!=null && it.subscriptionType!= FREEMIUM){
                    initialSuperProperties[FIRE_TV] = if(it.subscriptionType.equals(subscriptionTypeFtv, true)) "YES" else "NO"
                    initialSuperProperties[ATV] = if(it.subscriptionType.equals(subscriptionTypeAtv, true)) "YES" else "NO"
                }
                initialSuperProperties[BINGE_ACCOUNT_COUNT] = it.numberOfBingeAccounts

              /*  if (it.partnerSubscriptions?.subscriptionStatus!=null){
                    initialSuperProperties[SUBSCRIBED] = if (!it.partnerSubscriptions?.subscriptionStatus.equals(
                            SubscriptionPackStatusEnum.ACTIVE.status,
                            true
                        )
                    ) "NO" else "YES"
                }*/

                it.firstTimeLoginDate?.let { initialSuperProperties[FIRST_TIME_LOGIN] = it }
//                it.partnerSubscriptions?.packCreationDate?.let { initialSuperProperties[DATE_OF_SUBSCRIPTION] = it }
                it.partnerSubscriptions?.packCreationDate?.let {
                    getDateObject(it, START_DATE_FORMAT)?.let {date->
                        initialSuperProperties[DATE_OF_SUBSCRIPTION] =date
                    }
                }

                it.partnerSubscriptions?.packCreationDate?.let {
                    getDateObject(it, START_DATE_FORMAT)?.let {date->
                        initialSuperProperties[com.tatasky.binge.analytics.PACK_START_DATE] =date
                    }
                }

                it.partnerSubscriptions?.rechargeDue?.let {
                    getUtcDateWithOneSecObject(it, DEAFULT_DATE_FORMAT_FROM_BE)?.let { date ->
                        initialSuperProperties[com.tatasky.binge.analytics.PACK_RENEWAL_DATE] = date
                    }
                }
                initialSuperProperties[LAST_USED_AT] = Date()
                initialSuperProperties[LOGGED_IN_DEVICE_COUNT] = it.numberOfDevicesLoggedIn

                it.partnerSubscriptions?.let {
                    it.freeTrialStatus?.let {
                        initialSuperProperties[FREE_TRIAL] = if(it) "YES" else "NO"
                    }
                }

                it.partnerSubscriptions?.let {
                    it.productName?.let {
                        initialSuperProperties[PACK_NAME] = it
                    }
                }

               /* if(it.partnerSubscriptions?.subscribedBingeProduct != null){
                    initialSuperProperties[PACK_TYPE] ="PAID"
                }*/

                it.partnerSubscriptions?.let {
                    it.currentTenure?.let {
                        initialSuperProperties[PACK_TYPE] = it
                    }
                }

                it.partnerSubscriptions?.let {
                  it.packPrice?.let {
                      initialSuperProperties[PACK_PRICE] = it }
                }

//                initialSuperProperties[SUBSCRIPTION_TYPE] = it.subscriptionDetailInfo?.subscriptionType?:"UNSUBSCRIBED"

//                if (it.freeTrialAvailed!=null){
//                    initialSuperProperties[FREE_TRIAL_ELIGIBLE] = if(it.freeTrialAvailed) "NO" else "YES"
//                }

                it.partnerSubscriptions?.let {
                    if (it.subscriptionStatus!=null){
                        initialSuperProperties[RENEWAL_DUE] = if(it.subscriptionStatus.equals(SubscriptionPackStatusEnum.ACTIVE.status, true) && !it.isCancelled) "YES" else "NO"
                    }
                }


               if (burnRateType!=null && burnRateType!=""){
                   initialSuperProperties[BURN_RATE_TYPE] = burnRateType
               }

//                if (it.partnerSubscriptions?.planCTADetails?.renewPlanOption!=null){
//                initialSuperProperties[SI_PAYMENT_MODE] =  if (it.partnerSubscriptions?.planCTADetails?.renewPlanOption!!) "NO" else "YES"
//                }

                it.partnerSubscriptions?.let {
                    it.planCTADetails?.let {
                        it.renewPlanOption?.let {
                            initialSuperProperties[SI_PAYMENT_MODE] =  if (it) "NO" else "YES"
                            initialSuperProperties[RECURRING_PAYMENT] =  if (it) "NO" else "YES"
                        }
                    }
                }
//                if (it.partnerSubscriptions?.planCTADetails?.renewPlanOption!=null){
//                    initialSuperProperties[RECURRING_PAYMENT] = if (it.partnerSubscriptions?.planCTADetails?.renewPlanOption!!) "NO" else "YES"
//                }
                it.partnerSubscriptions?.let {
                    it.eligibleFirestick?.let {
                        initialSuperProperties[FTV_ELIGIBLE] = if(it) "YES" else "NO"
                    }
                }

                /*if(it.partnerSubscriptions?.eligibleFirestick!=null){
                    initialSuperProperties[FTV_ELIGIBLE] = if(it.partnerSubscriptions?.eligibleFirestick == true) "YES" else "NO"
                }*/
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mMoEHelper.setUniqueId(context, sid)
        mMoEHelper.setUserAttribute(context, initialSuperProperties)

        //This to be called only for clevertap hence moengage call should be before this assignment
        if(!uniqueIdCleverTap.isNullOrEmpty()){
            initialSuperProperties[IDENTITY] = uniqueIdCleverTap;
        }
        clevertapHelper?.onUserLogin(initialSuperProperties)
    }

    fun updateSuperPropertiesClevertap(
        sid: String,
        userDetails: LoginResponse.BingeSubscription
    ) {
        val initialSuperProperties = HashMap<String, Any?>()
        try {
            initialSuperProperties[SID] = sid

            userDetails.let {

                initialSuperProperties[IDENTITY] = sid

                initialSuperProperties[PHONE_CLEVER] = "+91" + (it.rmn ?: "")
                initialSuperProperties[RMN] = it.rmn ?: ""
                initialSuperProperties[NAME_CLEVER] =
                    (it.firstName ?: "") + " " + (it.lastName ?: "")
                initialSuperProperties[FNAME] = it.firstName?:""
                initialSuperProperties[LNAME] = it.lastName?:""
                initialSuperProperties[EMAIL_CLEVER] = it.emailId ?: ""
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        clevertapHelper?.pushProfile(initialSuperProperties)
        Log.d(CLEVERTAP_LOG,initialSuperProperties.toString())
    }


    fun setUniqueId(uniqueId:String){
        mMoEHelper.setUniqueId(context, uniqueId)

    }

    fun updateProperty(propertyName : String, propertyValue : String){
        mMoEHelper.setUserAttribute(context, propertyName, propertyValue)
        clevertapHelper?.setUserAttribute(propertyName,propertyValue)

    }
    /*
     Added for CT App Inbox implementation
     */
    fun getAllMessagesCT():ArrayList<CTInboxMessage>?{
        return  clevertapHelper?.allInboxMessages
    }

    fun updateProperty(propertyName : String, propertyValue : Int){
        mMoEHelper.setUserAttribute(context, propertyName, propertyValue)
        clevertapHelper.setUserAttributeInt(propertyName,propertyValue)
    }

    fun updateProperty(propertyName : String, propertyValue : Date?){
        propertyValue?.let {
            mMoEHelper.setUserAttribute(context, propertyName, propertyValue)
            clevertapHelper.setUserAttributeDate(propertyName,propertyValue)
        }
    }

    fun trackEvent(eventName: String) {
        try {
            validateAndEvent(checkForValidEventName(eventName))
        } catch (e: Exception) {
            d("exception in mixpanel event", e.toString())
        }

    }

    fun trackEvent(eventName: String, parameter: Properties) {
        try {
            validateAndEvent(checkForValidEventName(eventName), parameter)
        } catch (e: Exception) {
            d("exception in mixpanel event with parameter", e.toString())
        }

    }

    /*
    * Referencing to exact Property function in Moengage Library, due to same function created by us for Clevertap integration.
    * */
    private fun validateAndEvent(eventName: String) {

       // mMoEHelper.trackEvent(context,eventName, Properties())

        mMoEHelper.trackEvent(context,eventName,com.moengage.core.Properties())

        if (!redundantEventFilter.contains(eventName)) {
            clevertapHelper?.trackEvent(eventName)
        }
    }

    /*
  * Referencing to exact Property function in Moengage Library, due to same function created by us for Clevertap integration.
  * */
    private fun validateAndEvent(eventName: String, params: Properties) {
      //  mMoEHelper.trackEvent(context, eventName, params)

        mMoEHelper.trackEvent(context, eventName, params.getProperties())

        if (!redundantEventFilter.contains(eventName)) {
            //clevertapHelper?.trackEvent(eventName,params)
            clevertapHelper?.trackEvent(eventName, params.getCtProperties())
        }
    }

    /*
    * New function created for CT integration where notifications are deleted
    */
    fun deleteNotificationRead(inboxMessage: String) {
        clevertapHelper?.deleteInboxMessage(inboxMessage)
    }


    @Throws(IllegalStateException::class)
    fun getAllNotificationsList(): Single<MutableList<InboxMessage>?> {
        MoEInboxHelper.getInstance().fetchAllMessages(context)?.let {
            return Single.just(it.inboxMessages.toMutableList())
        } ?: kotlin.run { return Single.just(null)}
    }

    fun getUnreadNotificationCount() = MoEInboxHelper.getInstance().getUnClickedMessagesCount(context)

    fun deleteNotificationRead(inboxMessage: InboxMessage) {
        MoEInboxHelper.getInstance().deleteMessage(context, inboxMessage)
    }

    /*
    * Moengage code has been commenetd and implementation to delete all notifications for Clevertap are being implemented
    */
    fun deleteAllNotification() {
//        try {
//            thread(start = false) {
//                val inbox = MoEInboxHelper.getInstance().fetchAllMessages(context)//InboxData
//                inbox?.inboxMessages?.let {
//                    for (item in it) {
//                        MoEInboxHelper.getInstance().deleteMessage(context, item)
//                    }
//                }
//
//            }
//        } catch (e : Exception){}

        CoroutineScope(Dispatchers.IO).launch {
            clevertapHelper?.allInboxMessages.let {
                it?.forEach {
                    clevertapHelper?.deleteInboxMessage(it)
                }
            }
        }
     ((context.applicationContext).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
    }

    /*
    Get all notification for CT
     */
    fun getAllNotification(): java.util.ArrayList<CTInboxMessage>? {
        return  clevertapHelper?.allInboxMessages
    }


    fun markNotificationRead(inboxMessage: InboxMessage) {
        MoEInboxHelper.getInstance().trackMessageClicked(context, inboxMessage)
    }

    /*
     Mark notification read for CT messages
     */
    fun markNotificationRead(inboxMessage: String) {
     clevertapHelper?.pushInboxNotificationClickedEvent(inboxMessage)
    }


    fun logout() {
          // comment as after discussion with clevertap team , no need to delete token when user logout
//        try {
//            thread(start = false) {
//                FirebaseMessaging.getInstance().deleteToken()
//            }.apply {
//                setUncaughtExceptionHandler { thread, throwable ->
//                    d("exception in moengage event with parameter", throwable.toString())
//                }
//            }.start()
//        } catch (e: Exception) {
//            d("exception in moengage event with parameter", e.toString())
//        }
        ((context.applicationContext).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
//        mMoEHelper.logoutUser()
        mMoEHelper.setUniqueId(context, "")
        clevertapHelper?.trackEvent(EVENT_LOGOUT)
    }

    ////////===============clevertap starts===========////////////////

    val clevertapHelper = CleverTapAPI.getDefaultInstance(context)

    fun generateFCMToken(){

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("firebase", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            sendTokenToClevertap(token)

        })
    }
    fun sendTokenToClevertap(token:String){
        clevertapHelper?.pushFcmRegistrationId(token,true)
        Log.d(CLEVERTAP_LOG,"fcmToken=$token")
    }


    private fun CleverTapAPI?.setUserAttribute(propertyName: String, propertyValue: String) {

        var ap=HashMap<String,Any>()
        val ss=if (propertyName.equals(EMAIL)) EMAIL_CLEVER else propertyName
        ap.put(ss,propertyValue)
        clevertapHelper?.pushProfile(ap)
        Log.d(CLEVERTAP_LOG,"propertyName=$propertyName and value=${propertyValue}")
    }


    private fun CleverTapAPI?.setUserAttributeInt(propertyName: String, propertyValue: Int) {

        var ap = HashMap<String, Any>()
        val ss = if (propertyName.equals(EMAIL)) EMAIL_CLEVER else propertyName
        ap[ss] = propertyValue
        clevertapHelper?.pushProfile(ap)
        Log.d(CLEVERTAP_LOG, "propertyName=$propertyName and value=${propertyValue}")
    }

    private fun CleverTapAPI?.setUserAttributeDate(propertyName: String, propertyValue: Date) {

        var ap = HashMap<String, Any>()
        val ss = if (propertyName.equals(EMAIL)) EMAIL_CLEVER else propertyName
        ap[ss] = propertyValue
        clevertapHelper?.pushProfile(ap)
        Log.d(CLEVERTAP_LOG, "propertyName=$propertyName and value=${propertyValue}")
    }





    private fun CleverTapAPI?.trackEvent(eventName: String) {
        clevertapHelper?.pushEvent(eventName)
        Log.d(CLEVERTAP_LOG,"event=$eventName")
    }

    // Rewriting this function
    private fun CleverTapAPI?.trackEvent(eventName: String, hashMap: java.util.HashMap<String,Any?>) {
        try {
            clevertapHelper?.pushEvent(eventName, hashMap)
            Log.d(CLEVERTAP_LOG, "event=$eventName and eventDetails=${hashMap}")
        } catch (e: Exception) {
            clevertapHelper.trackEvent(eventName)

     }
    }

    fun updateIdentity(sid: String) {
        sid?.let {
            var identityHashMap = hashMapOf<String, Any>()
            identityHashMap[IDENTITY] = sid
            clevertapHelper?.pushProfile(identityHashMap)
        }
    }

    /////////==============clevertap ends===============///////////////

}






