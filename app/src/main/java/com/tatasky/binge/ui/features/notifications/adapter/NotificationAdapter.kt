package com.tatasky.binge.ui.features.notifications.adapter

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.collection.SparseArrayCompat
import androidx.collection.contains
import androidx.collection.forEach
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

import com.moengage.core.internal.utils.ISO8601Utils
import com.moengage.inbox.core.model.InboxMessage
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_APP_LAUNCH
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
import com.tatasky.binge.analytics.SOURCE_NOTIFICATION_ERROR
import com.tatasky.binge.data.networking.models.notifications.MoEngageGenericModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.data.networking.models.response.HomeResponse
import com.tatasky.binge.databinding.LayoutItemNotificationBinding
import com.tatasky.binge.databinding.LayoutItemNotificationClevertapBinding
import com.tatasky.binge.databinding.NotificationHeaderviewBinding
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.home.ItemViewType
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.notifications.model.NotificationInboxItem
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.splash.AppSplashActivity
import com.tatasky.binge.utils.*
import java.util.*
import java.util.concurrent.TimeUnit

const val TYPE_HEADER: Int = 1
const val TYPE_LIST: Int = 2

class NotificationAdapter(
    private val sharedPrefs: PrefsRepo,
    private val mSelectedItemsSize: MutableLiveData<SingleEvent<Int>>,
    private val itemReadDeleteListener: ((NotificationAction, NotificationInboxItem?) -> Unit),
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val mNotificationList = mutableListOf<NotificationInboxItem>()
    private var mInEditMode = false
    private val mSelectedItems = SparseArrayCompat<NotificationInboxItem>()
    private var mHeaderCount = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEADER) {
            mHeaderCount++
            return TitleViewHolder(
                NotificationHeaderviewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            return NotificationItemViewHolder(
                LayoutItemNotificationClevertapBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TitleViewHolder -> {
                val content = mNotificationList[position]
                holder.bind(content.title)
            }
            is NotificationItemViewHolder -> {
                val context = holder.binding.root.context
                val contentItem = mNotificationList[position]
                holder.bind(contentItem, sharedPrefs)
                holder.binding.checkbox.isChecked = mSelectedItems.contains(position)
                changeNotificationBackground(contentItem.isClicked, holder.binding)
                if (mInEditMode) {
                    holder.binding.checkbox.show()
                    holder.binding.ivProvider.hide()
                    holder.binding.duration.hide()

                    var textViewAnimator = ObjectAnimator.ofFloat(
                        holder.binding.bgNotification,
                        "translationX",
                        0f,
                        dpToPx(context, 5).toFloat()
                    )
                    textViewAnimator.duration = 200
                    textViewAnimator.start()
                } else {

                    var textViewAnimator = ObjectAnimator.ofFloat(
                        holder.binding.bgNotification,
                        "translationX",
                        dpToPx(context, 5).toFloat(),
                        0f
                    );
                    textViewAnimator.duration = 200
                    textViewAnimator.start()
                    holder.binding.checkbox.hide()
                    holder.binding.ivProvider.show()
                    holder.binding.duration.show()
                }

//                holder.binding.root.setOnClickListener {
//                    if (mInEditMode) {
//                        if (mSelectedItems.containsKey(position)) {
//                            mSelectedItems.remove(position)
//                            holder.binding.checkbox.isChecked = false
//                        } else {
//                            mSelectedItems.put(position, contentItem.notificationInboxMessage)
//                            holder.binding.checkbox.isChecked = true
//                        }
//                        mSelectedItemsSize.value = SingleEvent(mSelectedItems.size())
//                    } else {
//                        handleReadAction(contentItem, holder.binding)
//                        context.startActivity(notificationNavigate(context, contentItem).apply {
//                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                        })
//
//                    }
//                }
                holder.binding.root.setOnClickListener {
                    if (mInEditMode) {
                        if (mSelectedItems.containsKey(position)) {
                            mSelectedItems.remove(position)
                            holder.binding.checkbox.isChecked = false
                        } else {
                            mSelectedItems.put(position, contentItem)
                            holder.binding.checkbox.isChecked = true
                        }
                        mSelectedItemsSize.value = SingleEvent(mSelectedItems.size())
                    } else {
                        handleReadAction(contentItem, holder.binding)
                        if (contentItem.deepLinkUrl?.isNotEmpty() == true) {
                            val launchIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                    contentItem.deepLinkUrl?.trim()
                                ),
                                context,
                                AppSplashActivity::class.java
                            ).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                            context.startActivity(launchIntent)
                        } else {
                            contentItem.screenData?.let {
                                val data = mutableMapOf<String, String>(
                                    Pair(
                                        KEY_SCREEN_DATA,
                                        it
                                    )
                                )
                                contentItem.payload?.let { it1 ->
                                    val intent = createIntentWithUri(
                                        context, LandingActivity::class.java,
                                        it1, data
                                    )
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    context.startActivity(intent)
                                }
                            }
                        }

                    }
                }

            }

        }
    }

    private fun createIntentWithUri(
        context: Context,
        className: Class<*>,
        payloadData: MoEngageGenericModel,
        pushPayload: MutableMap<String, String>
    ): Intent {
        var error: Boolean = false
        if (!payloadData.screenName.isNullOrEmpty()) {
            val intent = when (payloadData.screenName.toUpperCase(Locale.getDefault())) {
                KEY_NOTIFICATION_GAMES -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context.getString(
                                R.string.deeplink_games,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    )
                }
                KEY_NOTIFICATION_GAMES_HOME -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context.getString(
                                R.string.deeplink_games,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    )
                }
                KEY_NOTIFICATION_MY_ACCOUNT -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context.getString(
                                R.string.deeplink_account,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    )
                }
                KEY_NOTIFICATION_DETAIL -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        null,
                        context,
                        className
                    ).apply {
                        pushPayload.forEach {

                            putExtra(it.key, it.value)

                        }
                    }
                }

                KEY_NOTIFICATION_SELFCARE -> {
                    if (className == AppSplashActivity::class.java)
                        Intent(
                            Intent.ACTION_VIEW,
                            context.getString(
                                R.string.deeplink_recharge,
                                BuildConfig.hostName
                            ).toUri(),
                            context,
                            className
                        ).apply { this.putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_SELFCARE) }
                    else {
                        Intent(context, RechargeActivity::class.java).apply {
                            putExtra(RechargeActivity.RECHARGE_SID, "YES")
                        }
                    }
                }
                KEY_BINGE_LIST.uppercase(Locale.ENGLISH),
                KEY_NOTIFICATION_WATCHLIST -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context?.getString(
                                R.string.deeplink_watchlist,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    )
                }
                KEY_NOTIFICATION_HELP -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context?.getString(
                                R.string.deeplink_faq,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    )
                }
                KEY_NOTIFICATION_HOME -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context?.getString(
                                R.string.deeplink_home,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    ).putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION)
                }
                KEY_NOTIFICATION_HOME_ERROR -> {
                    error = true
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context?.getString(
                                R.string.deeplink_home,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    )
                }
                KEY_NOTIFICATION_SEE_ALL -> {
                    try {
                        val railItem = Gson().fromJson<HomeResponse.Items>(
                            Gson().toJson(payloadData.any),
                            HomeResponse.Items::class.java
                        )
                        if (railItem.sectionSource.equals(
                                ItemViewType.PROVIDER.name,
                                true
                            )
                        ) {
                            val uri = context?.getString(
                                R.string.deeplink_app_see_all,
                                BuildConfig.hostName,
                                railItem.id,
                                railItem.title
                            )
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(uri),
                                context,
                                className
                            )
                        } else {
                            val uri = context?.getString(
                                R.string.deeplink_see_all,
                                BuildConfig.hostName,
                                railItem.id,
                                railItem.title,
                                railItem.sectionSource,
                                railItem.placeHolder
                            )
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(uri),
                                context,
                                className
                            )
                        }
                    } catch (e: Exception) {
                        error = true
                        Intent(
                            Intent.ACTION_VIEW,
                            null,
                            context,
                            className
                        ).apply { pushPayload.forEach { putExtra(it.key, it.value) } }
                    }
                }
                KEY_NOTIFICATION_MANAGE_PACK -> {
                    try {
                        var contentItem = Gson().fromJson<HomeResponse.Items>(
                            Gson().toJson(payloadData.any),
                            HomeResponse.Items::class.java
                        )
                        val uri = context?.getString(
                            R.string.deeplink_subscription,
                            BuildConfig.hostName,
                            contentItem.action,
                            contentItem.packName
                        )
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(uri),
                            context,
                            className
                        )
                    } catch (e: Exception) {
                        error = true
                        val uri = context?.getString(
                            R.string.deeplink_subscription,
                            BuildConfig.hostName,
                            "",
                            ""
                        )
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(uri),
                            context,
                            className
                        )
//                    e.printStackTrace()
                    }
                }
                KEY_NOTIFICATION_LOGIN -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context?.getString(
                                R.string.deeplink_login,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    )
                }
                KEY_SETTING.uppercase(Locale.ENGLISH) -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context?.getString(
                                R.string.deeplink_settings,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    )
                }
                KEY_SEARCH.uppercase(Locale.ENGLISH) -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context?.getString(
                                R.string.deeplink_search,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    )
                }
                KEY_SETTING.uppercase(Locale.ENGLISH) -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context?.getString(
                                R.string.deeplink_settings,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    )
                }
                KEY_DEVICE_MANAGEMENT.uppercase(Locale.ENGLISH) -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context?.getString(
                                R.string.deeplink_device_management,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    )
                }
                KEY_CATEGORIES.uppercase(Locale.ENGLISH) -> {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            context?.getString(
                                R.string.deeplink_categories,
                                BuildConfig.hostName
                            )
                        ),
                        context,
                        className
                    ).apply {
                    }
                }
                KEY_NOTIFICATION_PARTNER -> {
                    try {
                        val args = Gson().fromJson<ContentItem>(
                            Gson().toJson(payloadData.any),
                            ContentItem::class.java
                        )
                        val uri = context?.getString(
                            R.string.deeplink_app_page,
                            BuildConfig.hostName,
                            args.pageType,
                            args.provider,
                            args.partnerId
                        )
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(
                                uri
                            ),
                            context,
                            className
                        )
                    } catch (e: Exception) {
                        error = true
                        Intent(
                            Intent.ACTION_VIEW,
                            null,
                            context,
                            className
                        ).apply { pushPayload.forEach { putExtra(it.key, it.value) } }
                    }
                }
                else -> {
                    error = true
                    Intent(
                        Intent.ACTION_VIEW,
                        null,
                        context,
                        className
                    )
                }
            }
            if (error)
                intent.putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION_ERROR)
            else
                intent.putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION)
            return intent
        } else {
            return Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    context.getString(
                        R.string.deeplink_home,
                        BuildConfig.hostName
                    )
                ),
                context,
                className
            ).putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION)
        }
    }




    override fun getItemCount(): Int = mNotificationList.size

    override fun getItemViewType(position: Int): Int {
        return if (mNotificationList[position].isHeader)
            TYPE_HEADER
        else TYPE_LIST
    }

    private fun handleReadAction(
        notificationInboxItem: NotificationInboxItem,
        binding: LayoutItemNotificationClevertapBinding? = null,
    ) {
        if (!notificationInboxItem.isClicked) {
            notificationInboxItem.isClicked = true
            itemReadDeleteListener.invoke(
                NotificationAction.READ,
                notificationInboxItem
            )
            binding?.let { changeNotificationBackground(true, it) }

        }
    }

    private fun changeNotificationBackground(
        read: Boolean,
        binding: LayoutItemNotificationClevertapBinding,
    ) {
        if (read) {
            binding.bgNotification.background = null
        } else {
            binding.bgNotification.background = ContextCompat.getDrawable(
                binding.root.context,
                R.drawable.ic_language_background
            )
        }
    }

    private fun checkForAllNotificationClear() {
        if (mNotificationList.size <= mHeaderCount) {
            mNotificationList.clear()
            itemReadDeleteListener.invoke(NotificationAction.DATA_CLEAR, null)
        }
    }

    private fun notificationNavigate(
        context: Context,
        contentItem: ContentItem,
        className: Class<*> = LandingActivity::class.java,
    ): Intent {
        val payloadData: MoEngageGenericModel = contentItem.payload ?: MoEngageGenericModel(
            KEY_NOTIFICATION_HOME, null, null
        )
        var error: Boolean = false
        val intent = when (payloadData.screenName.uppercase(Locale.getDefault())) {
            KEY_NOTIFICATION_GAMES -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_games,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_NOTIFICATION_GAMES_HOME ->{
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_games,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_NOTIFICATION_MY_ACCOUNT -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_account,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_NOTIFICATION_DETAIL -> {
                Intent(
                    Intent.ACTION_VIEW,
                    null,
                    context,
                    className
                ).apply {
                    putExtra("screenData", Gson().toJson(
                        contentItem.payload,
                        MoEngageGenericModel::class.java
                    ))
                }
            }
            KEY_NOTIFICATION_SELFCARE -> {
                if (className == AppSplashActivity::class.java)
                    Intent(
                        Intent.ACTION_VIEW,
                        context.getString(
                            R.string.deeplink_recharge,
                            BuildConfig.hostName
                        ).toUri(),
                        context,
                        className
                    ).apply { this.putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_SELFCARE) }
                else {
                    Intent(context, RechargeActivity::class.java).apply {
                        putExtra(RechargeActivity.RECHARGE_SID, "YES")
                    }
                }
            }
            KEY_NOTIFICATION_WATCHLIST -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_watchlist,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_NOTIFICATION_HELP -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_faq,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_NOTIFICATION_HOME -> {
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_home,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                ).putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION)
            }
            KEY_NOTIFICATION_HOME_ERROR -> {
                error = true
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        context.getString(
                            R.string.deeplink_home,
                            BuildConfig.hostName
                        )
                    ),
                    context,
                    className
                )
            }
            KEY_NOTIFICATION_SEE_ALL -> {
                try {
                    val railItem = Gson().fromJson(
                        Gson().toJson(payloadData.any),
                        HomeResponse.Items::class.java
                    )
                    if (railItem.sectionSource.equals(
                            ItemViewType.PROVIDER.name,
                            true
                        )
                    ) {
                        val uri = context.getString(
                            R.string.deeplink_app_see_all,
                            BuildConfig.hostName,
                            railItem.id,
                            railItem.title
                        )
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(uri),
                            context,
                            className
                        )
                    } else {
                        val uri = context.getString(
                            R.string.deeplink_see_all,
                            BuildConfig.hostName,
                            railItem.id,
                            railItem.title,
                            railItem.sectionSource,
                            railItem.placeHolder
                        )
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(uri),
                            context,
                            className
                        )
                    }
                } catch (e: Exception) {
                    error = true
                    Intent(
                        Intent.ACTION_VIEW,
                        null,
                        context,
                        className
                    )
                }
            }
            KEY_NOTIFICATION_MANAGE_PACK -> {
                val subscriptionUri =
                    "${
                        context
                            .getString(
                                R.string.deeplink_subscription_generic_placeholder,
                                BuildConfig.hostName
                            )
                    }?${payloadData.any}"
                Intent(
                    Intent.ACTION_VIEW,
                    subscriptionUri.toUri(),
                    context,
                    className
                ).apply {
                    if (payloadData.any == null)
                        this.putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_MANAGE_PACK)
                    else
                        this.putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_MANAGED_APP_USE_CASE)
                }
            }
            KEY_NOTIFICATION_LOGIN -> {
                if (!sharedPrefs.getLoginStatus()) {
                    if (className == AppSplashActivity::class.java) {
                        Intent(
                            Intent.ACTION_VIEW,
                            null,
                            context,
                            className
                        ).apply { putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_LOGIN) }
                    } else
                        Intent(
                            context,
                            LandingActivity::class.java
                        ).apply { putExtra("source", SOURCE_APP_LAUNCH) }
                } else {
                    Intent(
                        Intent.ACTION_VIEW,
                        null,
                        context,
                        className
                    )
                }
            }
            KEY_NOTIFICATION_PARTNER -> {
                try {
                    val args = Gson().fromJson(
                        Gson().toJson(payloadData.any),
                        ContentItem::class.java
                    )
                    val uri = context.getString(
                        R.string.deeplink_app_page,
                        BuildConfig.hostName,
                        args.pageType,
                        args.provider,
                        args.partnerId
                    )
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            uri
                        ),
                        context,
                        className
                    )
                } catch (e: Exception) {
                    error = true
                    Intent(
                        Intent.ACTION_VIEW,
                        null,
                        context,
                        className
                    )
                }
            }
            else -> {
                error = true
                Intent(
                    Intent.ACTION_VIEW,
                    null,
                    context,
                    className
                )
            }
        }
        if (error)
            intent.putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION_ERROR)
        else
            intent.putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION)
        return intent
    }

    fun setNotificationList(notificationList: List<NotificationInboxItem>) {
        mNotificationList.clear()
        mNotificationList.addAll(notificationList)
        notifyDataSetChanged()
    }

    fun setEditMode(status: Boolean) {
        mInEditMode = status
        if (status) {
            mSelectedItemsSize.value = SingleEvent(0)
        } else {
            mSelectedItemsSize.value = SingleEvent(-1)
        }
        mSelectedItems.clear()
        notifyDataSetChanged()
    }

    fun markAllRead() {
        var anyUnreadNotification = false
        for (item in mNotificationList) {
            if (!item.isClicked) {
                anyUnreadNotification = true
                handleReadAction(item)
            }
            if (anyUnreadNotification)
                notifyDataSetChanged()
        }
    }

    fun handleAllSelection(selected: Boolean) {
        mSelectedItems.clear()
        if (selected) {
            mNotificationList.forEachIndexed { index, contentItem ->
                if (getItemViewType(index) == TYPE_LIST)
                    mSelectedItems.put(index, contentItem)
            }
        }
        mSelectedItemsSize.value = SingleEvent(mSelectedItems.size())
        notifyDataSetChanged()
    }


    fun removeNotification() {
        val listToRemove = mutableListOf<NotificationInboxItem>()
        mSelectedItems.forEach { key, inboxMessage ->
            itemReadDeleteListener.invoke(
                NotificationAction.DELETE,
                inboxMessage
            )
            listToRemove.add(mNotificationList[key])
        }
        mNotificationList.removeAll(listToRemove)
        checkForAllNotificationClear()
        setEditMode(false)
    }

    fun removeNotificationOnPosition(position: Int) {
        itemReadDeleteListener.invoke(
            NotificationAction.DELETE,
            mNotificationList[position]
        )
        mNotificationList.removeAt(position)
        notifyItemRemoved(position)
        checkForAllNotificationClear()
    }

    class TitleViewHolder(val binding: NotificationHeaderviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.title = title
        }
    }

    class NotificationItemViewHolder(
        val binding: LayoutItemNotificationClevertapBinding,
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contentItem: NotificationInboxItem, sharedPrefs: PrefsRepo) {
            binding.contentItem = contentItem
            val currentTime = System.currentTimeMillis()
            val notificationReceivedTime =
                contentItem.notificationTime
            val diffTime = currentTime - notificationReceivedTime
            when {
                TimeUnit.MILLISECONDS.toMinutes(diffTime) == 0L -> {
                    binding.duration.text = "Now"
                }
                TimeUnit.MILLISECONDS.toHours(diffTime) in 1..24 -> {
                    binding.duration.text = binding.root.context.getString(
                        R.string.hour,
                        TimeUnit.MILLISECONDS.toHours(diffTime)
                    )
                }
                TimeUnit.MILLISECONDS.toDays(diffTime) > 0 -> {
                    binding.duration.text =
                        binding.root.context.getString(
                            R.string.day,
                            TimeUnit.MILLISECONDS.toDays(diffTime)
                        )
                }
                else -> {
                    binding.duration.text = binding.root.context.getString(
                        R.string.minute,
                        TimeUnit.MILLISECONDS.toMinutes(diffTime)
                    )
                }
            }
            binding.ivProvider.show()
            binding.imageLogo.show()
            binding.imageLogo.setImageResource(R.drawable.medium_binge_logo)
            binding.image.show()
            if (contentItem.ctId == "NULL" || contentItem.isDetailPage == false) {
                binding.image.scaleType = ImageView.ScaleType.FIT_CENTER
                binding.image.setImageResource(R.drawable.medium_binge_logo)
//                binding.imageCardView.setCardBackgroundColor(R.color.transparent)
                binding.ivProvider.hide()
            } else {
                binding.image.scaleType = ImageView.ScaleType.FIT_XY
                val width = dpToPx(binding.image.context, 98)
                val height = dpToPx(binding.image.context, 64)
                val url = getCloudinaryUrl(
                    sharedPrefs.getCloudenieryUrl(),
                    width, height,
                    contentItem.getImageItem()
                )
                imageLoad(binding.image, url)
                updateCircularProviderImage(
                    binding.ivProvider,
                    contentItem.provider,
                    sharedPrefs.getProviderLogo(),
                    R.drawable.ic_rail_placeholder
                )
            }

            updateProviderImage(
                binding.ivProvider,
                contentItem.provider,
                sharedPrefs.getProviderLogo(),
                R.drawable.ic_rail_placeholder
            )
        }


    }

    fun getNotificationList() = mNotificationList

    enum class NotificationAction {
        READ,
        DELETE,
        DATA_CLEAR
    }
}