package com.tatasky.binge.ui.features.notifications

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.clevertap.android.sdk.CleverTapAPI
import com.google.gson.Gson
import com.moengage.core.BuildConfig
import com.moengage.core.PUSH_NOTIFICATION_MESSAGE
import com.moengage.core.PUSH_NOTIFICATION_TITLE
import com.moengage.core.internal.utils.ISO8601Utils
import com.moengage.inbox.core.model.InboxMessage
import com.tatasky.binge.R
import com.tatasky.binge.analytics.moengage.MoEngageHelper
import com.tatasky.binge.data.networking.models.notifications.MoEngageGenericModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import com.tatasky.binge.ui.base.frameworks.base.NotificationDispatcher
import com.tatasky.binge.ui.features.notifications.adapter.NotificationAdapter
import com.tatasky.binge.ui.features.notifications.clevertap.LinksModel
import com.tatasky.binge.ui.features.notifications.model.NotificationInboxItem
import com.tatasky.binge.utils.KEY_NOTIFICATION_DETAIL
import com.tatasky.binge.utils.KEY_SCREEN_DATA
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    private val useCase: CommonUseCase,
    val sharedPrefs: PrefsRepo,
    private val moEngageHelper: MoEngageHelper,
) : BaseViewModel() {

    //    var pageType: String? = ""
//    var isEditVisible: Boolean = false

    //    var isSelectAll: Boolean = false
//    var isWatchReadAll: Boolean = false
//    var isTransactionReadAll: Boolean = false
//    var isItemClicked: Int = -1
//    var deleteArray = ArrayList<ContentItem>()
    private var _watchNotificationList =
        MutableLiveData<SingleEvent<MutableList<NotificationInboxItem>>>()
//
//    private var _unreadNotificationCount = MutableLiveData<SingleEvent<Int>>()

    val watchNotificationList: LiveData<SingleEvent<MutableList<NotificationInboxItem>>> =
        _watchNotificationList
//    val unreadNotificationCount: LiveData<SingleEvent<Int>> = _unreadNotificationCount

    @Inject
    lateinit var analytics: NotificationAnalytics
    private val mSelectedNotificationCount = MutableLiveData(SingleEvent(-1))
    private val mNotificationAvailableStatus = MutableLiveData<SingleEvent<Boolean>>()

    //Changes for CT
    private val mNotificationAdapter = NotificationAdapter(
        sharedPrefs,
        mSelectedNotificationCount,
    ) { action, inboxMessage ->
        when (action) {
            NotificationAdapter.NotificationAction.READ -> {
                inboxMessage?.ctId?.let {
                    sharedPrefs.saveNotificationClickedId(it)
                }
                markNotificationRead(inboxMessage?.ctId)
            }
            NotificationAdapter.NotificationAction.DELETE -> {
                analytics.trackNotificationDelete()
                deleteNotification(inboxMessage?.ctId)
            }
            NotificationAdapter.NotificationAction.DATA_CLEAR -> {
                mNotificationAvailableStatus.value = SingleEvent(false)
            }
        }
    }

    fun getSelectedNotificationCount(): LiveData<SingleEvent<Int>> = mSelectedNotificationCount
    fun getNotificationAvailableStatus(): LiveData<SingleEvent<Boolean>> =
        mNotificationAvailableStatus

//    fun fetchNotificationList() {
//        val disposable = moEngageHelper.getAllNotificationsList()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                val listOfWatchNotification = mutableListOf<ContentItem>()
//                try {
//                    val currentMilliSeconds = Calendar.getInstance().timeInMillis
//                    val currentMilliSeconds24Before = currentMilliSeconds - 86400000L
//                    val recentNotifications = it?.filter { promotionalMessage -> ISO8601Utils.parse(promotionalMessage.receivedTime).time > currentMilliSeconds24Before }.takeIf { !it.isNullOrEmpty() }
//                    val earlierNotifications = it?.filter { promotionalMessage -> ISO8601Utils.parse(promotionalMessage.receivedTime).time <= currentMilliSeconds24Before }.takeIf { !it.isNullOrEmpty() }
//                    recentNotifications?.let {
//                       var isRecent = false
//                        it.forEach { promotionalMessage ->
//                            try {
//                                val moEngageGenericModel = Gson().fromJson(promotionalMessage.payload[KEY_SCREEN_DATA].toString(), MoEngageGenericModel::class.java)
//                                val contentItem = if (moEngageGenericModel.screenName == KEY_NOTIFICATION_DETAIL)
//                                    Gson().fromJson(Gson().toJson(moEngageGenericModel.any), ContentItem::class.java)
//                                else
//                                    ContentItem().apply {
//                                        contentTitle = promotionalMessage.payload[PUSH_NOTIFICATION_TITLE].toString()
//                                        id="NULL"
//                                    }
////                                contentItem.description = contentItem.description.takeIf { it.isNotBlank() }?:moEngageGenericModel.message.takeIf { !it.isNullOrBlank() }?:promotionalMessage.payload[PUSH_NOTIFICATION_MESSAGE].toString()
//                                contentItem.contentTitle = promotionalMessage.payload[PUSH_NOTIFICATION_TITLE].toString()
//                                contentItem.description = promotionalMessage.payload[PUSH_NOTIFICATION_MESSAGE].toString()
//                                contentItem.isRead = promotionalMessage.isClicked
//                                contentItem.notificationInboxMessage = promotionalMessage
//                                contentItem.payload = moEngageGenericModel
//                                if(!isRecent) {
//                                    isRecent = true
//                                    listOfWatchNotification.add(ContentItem().apply {
//                                        isHeader = true
//                                        title = "Recent"
//                                    })
//                                }
//                                listOfWatchNotification.add(contentItem)
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
//                        }
//                    }
//                    earlierNotifications?.let {
//                        var isEarlier = false
//                        it.forEach { promotionalMessage ->
//                            try {
//                                val moEngageGenericModel = Gson().fromJson(promotionalMessage.payload[KEY_SCREEN_DATA].toString(), MoEngageGenericModel::class.java)
//                                val contentItem = if (moEngageGenericModel.screenName == KEY_NOTIFICATION_DETAIL)
//                                    Gson().fromJson(Gson().toJson(moEngageGenericModel.any), ContentItem::class.java)
//                                else
//                                    ContentItem().apply {
////                                        contentTitle = promotionalMessage.payload[PUSH_NOTIFICATION_TITLE].toString()
//                                        id="NULL"
//                                    }
//                                contentItem.contentTitle = promotionalMessage.payload[PUSH_NOTIFICATION_TITLE].toString()
//                                contentItem.description = promotionalMessage.payload[PUSH_NOTIFICATION_MESSAGE].toString()
//                                contentItem.isRead = promotionalMessage.isClicked
//                                contentItem.notificationInboxMessage = promotionalMessage
//                                if(!isEarlier) {
//                                    isEarlier = true
//                                    listOfWatchNotification.add(ContentItem().apply {
//                                        isHeader = true
//                                        title = "Earlier"
//                                    })
//                                }
//                                listOfWatchNotification.add(contentItem)
//                            } catch (e: Exception) {
//                            }
//                        }
//                    }
//                    mNotificationAdapter.setNotificationList(listOfWatchNotification)
//                    _watchNotificationList.postValue(SingleEvent(listOfWatchNotification))
//                } catch (e: Exception) {
//
//                }
//            }, {
//            })
//        addDisposable(disposable)
//    }

    fun fetchNotificationCTList(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val listOfWatchNotification = mutableListOf<NotificationInboxItem>()
            val currentMilliSeconds = Calendar.getInstance().timeInMillis
            val currentMilliSeconds24Before = currentMilliSeconds - 86400000L
            val allInboxMessages = moEngageHelper.getAllMessagesCT();
            val recentNotifications =
                allInboxMessages?.filter { promotionalMessage -> promotionalMessage.date * 1000 > currentMilliSeconds24Before }
                    .takeIf { !it.isNullOrEmpty() }
            val earlierNotifications =
                allInboxMessages?.filter { promotionalMessage -> promotionalMessage.date * 1000 <= currentMilliSeconds24Before }
                    .takeIf { !it.isNullOrEmpty() }
            recentNotifications.let { messages ->
                var isRecent = false
                messages?.forEach {
                    var isDetailPage = false
                    var deeplink: String? = ""
                    var provider: String? = ""
                    var screenData = "{\"screenName\":\"HOME_ERROR\"}"
                    val homePayload = "{\"screenData\":{\"screenName\":\"HOME_ERROR\"}}"
                    var kv: String? = null
                    var baseContentItem = ContentItem()
                    var moEngageGenericModel: MoEngageGenericModel? = null
                    val inboxContentFirstItem = it.inboxMessageContents.first()
                    val links = inboxContentFirstItem.links
                    val title = inboxContentFirstItem.title
                    val message = inboxContentFirstItem.message
                    val notificationInboxItem = NotificationInboxItem()
                    var hasKV = false
                    links?.let {
                        if (links.length() > 0) {
                            for (index in 0 until links.length()) {
                                val linksModel = Gson().fromJson(
                                    links.getJSONObject(index)
                                        .toString(), LinksModel::class.java
                                )
                                if (linksModel.kv?.screenData != null && !hasKV) {
                                    hasKV = true
                                    val sd = linksModel.kv.screenData
                                    screenData = try {
                                        JSONObject(sd.replace(Char(160), ' ')).toString()
                                    } catch (e: Exception) {
                                        if (deeplink?.isEmpty() == true && screenData?.isEmpty()) {
                                            notificationInboxItem.deepLinkUrl =
                                                Uri.parse(context.getString(R.string.home))
                                                    .toString()
                                        }
                                        homePayload
                                    }
                                    if (screenData.isNotEmpty()) {
                                        kv = screenData
                                    }
                                }
                                if (linksModel.url?.android?.text?.isEmpty() != true) {
                                    deeplink = linksModel.url?.android?.text
                                    break
                                }
                            }
                        }
                    }
                    if (deeplink?.isEmpty() == true && !inboxContentFirstItem.actionUrl.isNullOrEmpty()) {
                        if (inboxContentFirstItem.actionUrl.length > 1) {
                            deeplink = inboxContentFirstItem.actionUrl
                        }
                    }
                    if (deeplink?.isNotEmpty() == true) {
                        if (deeplink?.contains("/detail") == true) {
                            isDetailPage = true
                            provider =
                                NotificationDispatcher.retriveProviderFromDeepLink(deeplink!!)
                        }
                    }

                    if (deeplink?.isEmpty() == true && (kv?.isNotEmpty() ?: homePayload) == true) {
                        try {
                            moEngageGenericModel = Gson().fromJson(
                                screenData,
                                MoEngageGenericModel::class.java
                            )
                            if (moEngageGenericModel.screenName.isNotEmpty()) {
                                baseContentItem = Gson().fromJson(
                                    Gson().toJson(moEngageGenericModel.any),
                                    ContentItem::class.java
                                )
                                if (moEngageGenericModel.screenName == KEY_NOTIFICATION_DETAIL) {
                                    isDetailPage = true
                                    provider = baseContentItem.provider
                                }
                            } else {
                                provider = ""
                            }
                        } catch (e: Exception) {
                            provider = ""
                        }
                    }
                    try {

                        notificationInboxItem.ctId = it.messageId
                        notificationInboxItem.contentTitle = title
                        notificationInboxItem.description = message
                        notificationInboxItem.isDetailPage = isDetailPage
                        notificationInboxItem.isRead = it.isRead
                        notificationInboxItem.deepLinkUrl = deeplink
                        notificationInboxItem.notificationTime = it.date * 1000
                        notificationInboxItem.provider = provider.toString()
                        screenData?.let {
                            notificationInboxItem.screenData = it
                        }
                        if (isDetailPage) {
                            inboxContentFirstItem.media?.let {
                                notificationInboxItem.image = it
                            }
                            if (notificationInboxItem.image.isEmpty()) {
                                notificationInboxItem.image = baseContentItem.image
                            }
                        }
                        moEngageGenericModel?.let {
                            notificationInboxItem.payload = moEngageGenericModel
                        }
                        if ((deeplink?.isEmpty() == true && screenData?.isEmpty()) || (deeplink?.isEmpty() == true && moEngageGenericModel == null)) {
                            notificationInboxItem.deepLinkUrl =
                                Uri.parse(context.getString(R.string.home)).toString()
                        }
                        if (!isRecent) {
                            isRecent = true
                            listOfWatchNotification.add(NotificationInboxItem().apply {
                                isHeader = true
                                this.title = "Recent"
                            })
                        }
                        if (notificationInboxItem.isDetailPage) {
                            notificationInboxItem.id = "NULL"
                        }
                        notificationInboxItem.isClicked =
                            sharedPrefs.isNotificationClickedIdExit(it.messageId)
                        listOfWatchNotification.add(notificationInboxItem)

                    } catch (e: Exception) {

                    }
                }
                earlierNotifications.let { messages ->

                    var isEarlier = false
                    messages?.forEach {
                        var isDetailPage = false
                        var deeplink: String? = ""
                        var provider: String? = ""
                        val homePayload = "{\"screenData\":{\"screenName\":\"HOME_ERROR\"}}"
                        var kv: String? = null
                        var screenData = "{\"screenName\":\"HOME_ERROR\"}"
                        var baseContentItem = ContentItem()
                        var moEngageGenericModel: MoEngageGenericModel? = null
                        val inboxContentFirstItem = it.inboxMessageContents.first()
                        val links = inboxContentFirstItem.links
                        val title = inboxContentFirstItem.title
                        val message = inboxContentFirstItem.message
                        val notificationInboxItem = NotificationInboxItem()
                        var hasKV = false
                        links?.let {
                            if (links.length() > 0) {
                                for (index in 0 until links.length()) {
                                    val linksModel = Gson().fromJson(
                                        links.getJSONObject(index)
                                            .toString(), LinksModel::class.java
                                    )
                                    if (linksModel.kv?.screenData != null && !hasKV) {
                                        hasKV = true
                                        val sd = linksModel.kv.screenData
                                        screenData = try {
                                            JSONObject(sd.replace(Char(160), ' ')).toString()
                                        } catch (e: Exception) {
                                            if (deeplink?.isEmpty() == true && screenData?.isEmpty()) {
                                                notificationInboxItem.deepLinkUrl =
                                                    Uri.parse(
                                                        context.getString(
                                                            R.string.deeplink_home,
                                                            com.tatasky.binge.BuildConfig.hostName
                                                        )
                                                    ).toString()
                                            }
                                            homePayload
                                        }
                                        if (screenData.isNotEmpty()) {
                                            kv = screenData
                                        }
                                    }
                                    if (linksModel.url?.android?.text?.isEmpty() != true) {
                                        deeplink = linksModel.url?.android?.text
                                        break
                                    }
                                }
                            }
                        }
                        if (deeplink?.isEmpty() == true && !inboxContentFirstItem.actionUrl.isNullOrEmpty()) {
                            if (inboxContentFirstItem.actionUrl.length > 1) {
                                deeplink = inboxContentFirstItem.actionUrl
                            }
                        }
                        if (deeplink?.isNotEmpty() == true) {
                            if (deeplink?.contains("/detail") == true) {
                                isDetailPage = true
                                provider = NotificationDispatcher.retriveProviderFromDeepLink(
                                    deeplink!!
                                )
                            }
                        }
                        if (deeplink?.isEmpty() == true && (kv?.isNotEmpty()
                                ?: homePayload) == true
                        ) {
                            try {
                                moEngageGenericModel = Gson().fromJson(
                                    JSONObject(screenData).toString(),
                                    MoEngageGenericModel::class.java
                                )
                                if (moEngageGenericModel?.screenName?.isNotEmpty() == true) {
                                    baseContentItem = Gson().fromJson(
                                        Gson().toJson(moEngageGenericModel?.any),
                                        ContentItem::class.java
                                    )
                                    if (moEngageGenericModel?.screenName == KEY_NOTIFICATION_DETAIL) {
                                        isDetailPage = true
                                        provider = baseContentItem?.provider
                                    }
                                } else {
                                    provider = ""
                                }
                            } catch (e: Exception) {
                                provider = ""
                            }
                        }
                        try {
                            notificationInboxItem.ctId = it.messageId
                            notificationInboxItem.contentTitle = title
                            notificationInboxItem.description = message
                            notificationInboxItem.isRead = it.isRead
                            notificationInboxItem.isDetailPage = isDetailPage
                            notificationInboxItem.notificationTime = it.date * 1000
                            screenData.let {
                                notificationInboxItem.screenData = it
                            }
                            if (isDetailPage) {
                                inboxContentFirstItem.media?.let {
                                    notificationInboxItem.image = it
                                }
                                if (notificationInboxItem.image.isEmpty()) {
                                    notificationInboxItem.image = baseContentItem.image
                                }
                            }
                            notificationInboxItem.deepLinkUrl = deeplink
                            notificationInboxItem.provider = provider.toString()
                            moEngageGenericModel?.let {
                                notificationInboxItem.payload = moEngageGenericModel
                            }
                            if ((deeplink?.isEmpty() == true && screenData?.isEmpty()) || (deeplink?.isEmpty() == true && moEngageGenericModel == null)) {
                                notificationInboxItem.deepLinkUrl =
                                    Uri.parse(context.getString(R.string.home)).toString()
                            }
                            if (!isEarlier) {
                                isEarlier = true
                                listOfWatchNotification.add(NotificationInboxItem().apply {
                                    isHeader = true
                                    this.title = "Earlier"
                                })
                            }
                            if (notificationInboxItem.isDetailPage) {
                                notificationInboxItem.id = "NULL"
                            }
                            notificationInboxItem.isClicked =
                                sharedPrefs.isNotificationClickedIdExit(it.messageId)
                            listOfWatchNotification.add(notificationInboxItem)
                        } catch (e: Exception) {

                        }
                    }

                    //For optimizting code
                }


            }
            viewModelScope.launch(Dispatchers.Main) {
                mNotificationAdapter.setNotificationList(listOfWatchNotification)
                _watchNotificationList.postValue(SingleEvent(listOfWatchNotification))
            }
        }

    }

//    private fun markNotificationRead(inboxMessage: InboxMessage?) {
//        inboxMessage?.let { moEngageHelper.markNotificationRead(it) }
//    }

    private fun markNotificationRead(inboxMessage: String?) {
        inboxMessage?.let { moEngageHelper.markNotificationRead(it) }
    }

    private fun deleteNotification(inboxMessage: String?) {
        inboxMessage?.let { moEngageHelper.deleteNotificationRead(it) }
    }

//    private fun markNotificationRead(inboxMessage: String?) {
//        inboxMessage?.let { moEngageHelper.markNotificationRead(it) }
//    }
    fun getNotificationAdapter(): NotificationAdapter = mNotificationAdapter

    fun setReadStatus() {
        mNotificationAdapter.let { notificationAdapter ->
            var notificationListItems = notificationAdapter.getNotificationList()
            if (notificationListItems.isNotEmpty()) {
                viewModelScope.launch(Dispatchers.IO) {
                    notificationListItems.forEach {
                        if (!it.isRead) {
                            moEngageHelper.clevertapHelper?.markReadInboxMessage(it.ctId)
                            moEngageHelper.clevertapHelper?.pushInboxNotificationViewedEvent(it.ctId)
                        }
                    }
                }

            }
        }
    }

    fun setNoNotificationText(): String? =
        sharedPrefs.getConfigResponse()?.data?.config?.notification

}
