package com.tatasky.binge.ui.features.notifications
//
//import android.app.NotificationManager
//import android.content.Context
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.view.Menu
//import android.view.MenuInflater
//import android.view.MenuItem
//import android.view.View
//import android.view.animation.TranslateAnimation
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.ViewModelStoreOwner
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.ItemTouchHelper
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.material.transition.MaterialSharedAxis
//import com.google.gson.Gson
//import com.tatasky.binge.BuildConfig
//import com.tatasky.binge.R
//import com.tatasky.binge.analytics.SOURCE_APP_LAUNCH
//import com.tatasky.binge.analytics.SOURCE_NOTIFICATION
//import com.tatasky.binge.analytics.SOURCE_NOTIFICATION_ERROR
//import com.tatasky.binge.customviews.RVLinearLayoutManager
//import com.tatasky.binge.data.networking.models.notifications.MoEngageGenericModel
//import com.tatasky.binge.data.networking.models.response.ContentItem
//import com.tatasky.binge.data.networking.models.response.HomeResponse
//import com.tatasky.binge.data.networking.models.response.NotificationResponse
//import com.tatasky.binge.databinding.FragmentNotificationsBinding
//import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
//import com.tatasky.binge.ui.base.frameworks.extensions.hide
//import com.tatasky.binge.ui.base.frameworks.extensions.show
//import com.tatasky.binge.ui.features.common.CommonSampleViewModel
//import com.tatasky.binge.ui.features.home.ItemViewType
//import com.tatasky.binge.ui.features.home.LandingActivity
//import com.tatasky.binge.ui.features.notifications.adapter.NotifictionListAdapter
//import com.tatasky.binge.ui.features.onboarding.OnBoardingActivity
//import com.tatasky.binge.ui.features.recharge.RechargeActivity
//import com.tatasky.binge.ui.features.splash.AppSplashActivity
//import com.tatasky.binge.utils.*
//import kotlinx.android.synthetic.main.fragment_notifications.*
//import java.util.*
//import javax.inject.Inject
//import kotlin.collections.ArrayList
//
//
//class NotificationsFragment : BaseFragment<FragmentNotificationsBinding, NotificationViewModel>() {
//
//    private var mItem: MenuItem? = null
//    private var mMenu: Menu? = null
//
//    private lateinit var watchAdapter: NotifictionListAdapter
//    private lateinit var transactionAdapter: NotifictionListAdapter
//    private var mainViewModel: CommonSampleViewModel? = null
//    private var isWatchVisible = true
//    private var isEditSettingVisible = false
//    private var watchList = ArrayList<ContentItem>()
//    private var transactionList = ArrayList<ContentItem>()
//
//    @Inject
//    lateinit var analytics: NotificationAnalytics
//
//    override fun getViewModelClass(): Class<NotificationViewModel> {
//        return NotificationViewModel::class.java
//    }
//
//    override fun layoutId(): Int {
//        return R.layout.fragment_notifications
//    }
//
//    override fun getViewModelOwner(): ViewModelStoreOwner {
//        return findNavController().getViewModelStoreOwner(R.id.nav_notifications)
//    }
//
//    override fun setObserver() {
//        viewModel.watchNotificationList.observe(viewLifecycleOwner, Observer {
//            it.getContentIfNotHandled()?.let {
//                if (it.size == 0) {
//                    setNoDataView()
//                } else {
//                    mMenu?.findItem(R.id.edit_notification)?.isVisible = true
//                    binding.tvEmpty.hide()
//                    binding.watchRecycler.show()
//                    watchList.clear()
//                    watchList.addAll(it)
//                    setWatchAdapter()
//                }
//            }
//        })
//        viewModel.unreadNotificationCount.observe(viewLifecycleOwner, Observer {
//            it.getContentIfNotHandled()?.let {
//                mainViewModel?.setTotalUnreadCount()
//            }
//        })
//    }
//
//    override fun toBeCalledOnce() {
//        binding.vm = viewModel
//        viewModel.pageType = getString(R.string.watch)
//        createTransactionList(readTransactionRawFile())
//        setTransactionAdapter()
//        setViewListeners()
//        viewModel.fetchNotificationList()
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        mainViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[CommonSampleViewModel::class.java]
//        analytics.trackNotificationScreenVisit()
//    }
//
//    private fun setViewListeners() {
//        binding.checkbox.setOnCheckedChangeListener { compoundButton, isChecked ->
//
//            viewModel.deleteArray.clear()
//            if (isChecked) {
//                binding.checkbox.text = getString(R.string.unselect_all)
//                if (isWatchVisible) {
//                    viewModel.deleteArray.addAll(watchList)
//                } else {
//                    viewModel.deleteArray.addAll(transactionList)
//                }
//            } else {
//                binding.checkbox.text = getString(R.string.select_all)
//            }
//            notifyAdapter()
//        }
//        binding.markAllRead.setOnClickListener {
//
//            if (isWatchVisible) {
//                if (viewModel.isWatchReadAll) {
//                    showToast(context, "Already marked as read.")
////                    viewModel.isWatchReadAll = false
////                    notifyAdapter()
//                } else {
//                    viewModel.isWatchReadAll = true
//                    watchAdapter.list.forEach {
//                        it.isRead = true
//                        viewModel.markNotificationRead(it.notificationInboxMessage)
//                    }
//                    notifyAdapter()
//                }
//            } else {
//                if (viewModel.isTransactionReadAll) {
////                    viewModel.isTransactionReadAll = false
////                    notifyAdapter()
//                    showToast(context, "Already marked as read.")
//                } else {
//                    viewModel.isTransactionReadAll = true
//                    transactionAdapter.list.forEach { it.isRead = true }
//                    notifyAdapter()
//                }
//            }
//        }
//
//        binding.remove.setOnClickListener {
//            if (isWatchVisible) {
//                if (watchList.isEmpty()) {
//                    showToast(context, "No data to remove.")
//                    return@setOnClickListener
//                }
//                removeSelectedWatchItems()
//                watchAdapter.updateHeaderView()
//                notifyAdapter()
//                if (watchAdapter.itemCount == 0) {
//                    setNoDataView()
//                }
//            } else {
//                if (transactionList.isEmpty()) {
//                    showToast(context, "No data to remove.")
//                    return@setOnClickListener
//                }
//
//                removeSelectedTransactionItems(transactionList)
//                transactionAdapter.updateHeaderView()
//                notifyAdapter()
//                if (transactionAdapter.itemCount == 0) {
//                    binding.transactionRecycler.hide()
//                    binding.tvEmpty.show()
//                }
//            }
//            binding.checkbox.isChecked = false
//            if (checkForEmptyData())
//                mItem?.let { it1 -> onTabEditClick(it1) }
//        }
//
//        binding.rlWatch.setOnClickListener {
//
//            if (isWatchVisible || viewModel.isEditVisible) {
//                return@setOnClickListener
//            }
//            binding.tvWatch.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkOnPrimary))
//            binding.tvTransaction.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkOnSecondary))
//            setTabVisible(View.VISIBLE, View.GONE)
//            hideEditView()
//            isWatchVisible = true
//            viewModel.deleteArray.clear()
//            viewModel.pageType = getString(R.string.watch)
//
//            if (checkForEmptyData()) {
//                setNoDataView()
//            }
//        }
//        binding.rlTransaction.setOnClickListener {
//
//            if (!isWatchVisible || viewModel.isEditVisible) {
//                return@setOnClickListener
//            }
//            binding.tvWatch.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkOnSecondary))
//            binding.tvTransaction.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkOnPrimary))
//            setTabVisible(View.GONE, View.VISIBLE)
//            hideEditView()
//            isWatchVisible = false
//            viewModel.deleteArray.clear()
//            viewModel.pageType = getString(R.string.transaction)
//            if (checkForEmptyData()) {
//                binding.transactionRecycler.hide()
//                binding.tvEmpty.show()
//            }
//        }
//
//
//        binding.notificationSetting.setOnClickListener {
//            //hideEditView()
//            isEditSettingVisible = true
//
//            findNavController().navigateSafe(NotificationFragmentDirections.actionNotificationFragmentToNotificationSettingsFragment())
//        }
//
//    }
//
//    private fun removeSelectedWatchItems() {
//        var removeList = ArrayList<ContentItem>()
//        for (item in watchList) {
//            if (viewModel.deleteArray.contains(item)) {
//                removeList.add(item)
//            }
//        }
//
//        if (removeList.size == watchList.size - watchList.filter { it.isHeader }.size)
//            removeList = watchList
//        removeList.forEach { viewModel.deleteNotification(it.notificationInboxMessage) }
//        watchList.removeAll(removeList)
//        analytics.trackNotificationDelete()
//    }
//
//
//    private fun removeSelectedTransactionItems(list: ArrayList<ContentItem>) {
//        var removeList = ArrayList<ContentItem>()
//        for (item in transactionList) {
//            if (viewModel.deleteArray.contains(item)) {
//                removeList.add(item)
//            }
//        }
//        if (removeList.size == transactionList.size - transactionList.filter { it.isHeader }.size)
//            removeList = transactionList
//        transactionList.removeAll(removeList)
//        analytics.trackNotificationDelete()
//    }
//
//    private fun notifyAdapter() {
//        if (isWatchVisible) {
//            watchAdapter.update()
//        } else {
//            transactionAdapter.update()
//        }
//    }
//
//    private fun setTabVisible(isWatch: Int, isTransaction: Int) {
//
//        binding.watchView.visibility = isWatch
//        binding.watchRecycler.visibility = isWatch
//        binding.transactionView.visibility = isTransaction
//        binding.transactionRecycler.visibility = isTransaction
//        binding.tvEmpty.hide()
//    }
//
//    private fun showEditView() {
//        if (checkForEmptyData()) {
//            return
//        }
////        binding.cardSettings.show()
////        val animate = TranslateAnimation(
////            0f,
////            0f,
////            binding.cardSettings.height.toFloat(),
////            0f
////        )
////        animate.duration = 500
////        binding.cardSettings.startAnimation(animate)
//        viewModel.isSelectAll = false
//        viewModel.isEditVisible = true
////        binding.cardSettings.startAnimation(animate)
//        notifyAdapter()
//        // findNavController().navigateSafe(NotificationsFragmentDirections.actionNotificationFragmentToEditNotificationsDialog())
//
//    }
//
//    fun checkForEmptyData(): Boolean {
//        if (isWatchVisible) {
//            if (watchList.isEmpty()) {
//                return true
//            }
//        } else {
//            return true
//        }
//        return false
//    }
//
//    private fun hideEditView() {
//        if (binding.cardSettings.visibility == View.VISIBLE) {
//            clickMenuItem()
//
//            val animSlideDown = TranslateAnimation(
//                0f,
//                0f,
//                0f,
//                binding.cardSettings.getHeight().toFloat()
//            )
//            animSlideDown.duration = 500
//            binding.cardSettings.startAnimation(animSlideDown)
//            binding.cardSettings.hide()
//            binding.checkbox.isChecked = false
//
//            viewModel.isEditVisible = false
//            notifyAdapter()
//
//            binding.checkbox.isChecked = false
//            //binding.edit.text = getString(R.string.edit)
//        }
//
//
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
//        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
//            this.duration = 500
//        }
//        enterTransition = forward
//
//        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
//            this.duration = 500
//        }
//        returnTransition = backward
//        reenterTransition = backward
//        exitTransition = forward
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        mMenu = menu
//        inflater.inflate(R.menu.menu_notification, menu)
//        if (viewModel.isEditVisible) {
//            val action = mMenu?.findItem(R.id.edit_notification)
//            if (action != null) {
//                action.title = getString(R.string.close)
//                action.isVisible = true
//            }
//            showEditView()
//        }
//        if(!watchList.isNullOrEmpty()){
//            mMenu?.findItem(R.id.edit_notification)?.isVisible = true
//        }
//    }
//
//    private fun clickMenuItem() {
//        if (mMenu != null) {
//            val action = mMenu?.findItem(R.id.edit_notification)
//            if (action != null) {
//                action.title = getString(R.string.edit)
//            }
//        }
//
//    }
//
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.edit_notification -> {
//                mItem = item
//                onTabEditClick(item)
//            }
//        }
//        return true
//    }
//
//    private fun onTabEditClick(item: MenuItem) {
//        if (viewModel.isEditVisible) {
//            item.title = getString(R.string.edit)
//            hideEditView()
//        } else {
//            if (!checkForEmptyData()) {
//                item.title = getString(R.string.close)
//                showEditView()
//            }
//
//        }
//    }
//
//    private fun notificationNavigate(contentItem: ContentItem, className: Class<*> = LandingActivity::class.java):Intent{
//        var payloadData: MoEngageGenericModel = contentItem.payload?: MoEngageGenericModel(
//            KEY_NOTIFICATION_HOME,null,null
//        )
//        var error:Boolean = false
//        val intent =  when (payloadData.screenName.toUpperCase(Locale.getDefault())) {
//            KEY_NOTIFICATION_MY_ACCOUNT -> {
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse(
//                        getString(
//                            R.string.deeplink_account,
//                            BuildConfig.hostName
//                        )
//                    ),
//                    context,
//                    className
//                )
//            }
//            KEY_NOTIFICATION_DETAIL -> {
//                Intent(
//                    Intent.ACTION_VIEW,
//                    null,
//                    context,
//                    className
//                ).apply {
//                    putExtra("screenData",Gson().toJson(
//                        contentItem.payload,
//                        MoEngageGenericModel::class.java
//                    ))
//                }
//            }
//            KEY_NOTIFICATION_SELFCARE -> {
//                if (className == AppSplashActivity::class.java)
//                    Intent(
//                        Intent.ACTION_VIEW,
//                        null,
//                        context,
//                        className
//                    ).apply { this.putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_SELFCARE) }
//                else {
////                    subscriptionAnalytics.trackRechargeInitiate(SOURCE_NOTIFICATION, "")
//                    Intent(context, RechargeActivity::class.java).apply {
//                        putExtra(RechargeActivity.RECHARGE_SID, "YES")
//                    }
//                }
//            }
//            KEY_NOTIFICATION_WATCHLIST -> {
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse(
//                        getString(
//                            R.string.deeplink_watchlist,
//                            BuildConfig.hostName
//                        )
//                    ),
//                    context,
//                    className
//                )
//            }
//            KEY_NOTIFICATION_HELP -> {
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse(
//                        getString(
//                            R.string.deeplink_faq,
//                            BuildConfig.hostName
//                        )
//                    ),
//                    context,
//                    className
//                )
//            }
//            KEY_NOTIFICATION_HOME -> {
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse(
//                        getString(
//                            R.string.deeplink_home,
//                            BuildConfig.hostName
//                        )
//                    ),
//                    context,
//                    className
//                ).putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION)
//            }
//            KEY_NOTIFICATION_HOME_ERROR -> {
//                error = true
//                Intent(
//                    Intent.ACTION_VIEW,
//                    Uri.parse(
//                        getString(
//                            R.string.deeplink_home,
//                            BuildConfig.hostName
//                        )
//                    ),
//                    context,
//                    className
//                )
//            }
//            KEY_NOTIFICATION_SEE_ALL -> {
//                try {
//                    val railItem = Gson().fromJson<HomeResponse.Items>(
//                        Gson().toJson(payloadData.any),
//                        HomeResponse.Items::class.java
//                    )
//                    if (railItem.sectionSource.equals(
//                            ItemViewType.PROVIDER.name,
//                            true
//                        )
//                    ) {
//                        val uri = getString(
//                            R.string.deeplink_app_see_all,
//                            BuildConfig.hostName,
//                            railItem.id,
//                            railItem.title
//                        )
//                        Intent(
//                            Intent.ACTION_VIEW,
//                            Uri.parse(uri),
//                            context,
//                            className
//                        )
//                    } else {
//                        val uri = getString(
//                            R.string.deeplink_see_all,
//                            BuildConfig.hostName,
//                            railItem.id,
//                            railItem.title,
//                            railItem.sectionSource,
//                            railItem.placeHolder
//                        )
//                        Intent(
//                            Intent.ACTION_VIEW,
//                            Uri.parse(uri),
//                            context,
//                            className
//                        )
//                    }
//                } catch (e: Exception) {
//                    error = true
//                    Intent(
//                        Intent.ACTION_VIEW,
//                        null,
//                        context,
//                        className
//                    )
//                }
//            }
//            KEY_NOTIFICATION_MANAGE_PACK -> {
//                if (className == AppSplashActivity::class.java)
//                    Intent(
//                        Intent.ACTION_VIEW,
//                        null,
//                        context,
//                        className
//                    ).apply { this.putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_MANAGE_PACK) }
//                else
//                    getSubscriptionActivityIntent(
//                        context,
//                        fromLogin = true,
//                        selectedAppId = null,
//                        fromScreen = SOURCE_NOTIFICATION
//                    )
//            }
//            KEY_NOTIFICATION_LOGIN -> {
//                if (!sharedPrefs.getLoginStatus()) {
//                    if (className == AppSplashActivity::class.java) {
//                        Intent(
//                            Intent.ACTION_VIEW,
//                            null,
//                            context,
//                            className
//                        ).apply { putExtra(KEY_SCREEN_NAME, KEY_NOTIFICATION_LOGIN) }
//                    } else
//                        Intent(
//                            context,
//                            OnBoardingActivity::class.java
//                        ).apply { putExtra("source", SOURCE_APP_LAUNCH) }
//                } else {
//                    Intent(
//                        Intent.ACTION_VIEW,
//                        null,
//                        context,
//                        className
//                    )
//                }
//            }
//            KEY_NOTIFICATION_PARTNER ->{
//                try{
//                    val args = Gson().fromJson<ContentItem>(
//                        Gson().toJson(payloadData.any),
//                        ContentItem::class.java
//                    )
//                    val uri = getString(
//                        R.string.deeplink_app_page,
//                        BuildConfig.hostName,
//                        args.pageType,
//                        args.provider,
//                        args.partnerId
//                    )
//                    Intent(
//                        Intent.ACTION_VIEW,
//                        Uri.parse(
//                            uri
//                        ),
//                        context,
//                        className
//                    )
//                } catch (e: Exception) {
//                    error = true
//                    Intent(
//                        Intent.ACTION_VIEW,
//                        null,
//                        context,
//                        className
//                    )
//                }
//            }
//            else -> {
//                error = true
//                Intent(
//                    Intent.ACTION_VIEW,
//                    null,
//                    context,
//                    className
//                )
//            }
//        }
//        if(error)
//            intent.putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION_ERROR)
//        else
//            intent.putExtra(KEY_FROM_SCREEN, SOURCE_NOTIFICATION)
//        return intent
//    }
//
//    private fun setWatchAdapter() {
//        watchAdapter = NotifictionListAdapter(
//            watchList,
//            viewModel,
//            requireContext(),
//            viewModel.sharedPrefs.getCloudenieryUrl(),
//            object : NotifictionListAdapter.ItemClickListener {
//                override fun onItemClick(view: View, position: Int) {
//                    watchAdapter.notifyItemChanged(position)
//                    var key =
//                        watchList[position].payload?.screenName?.toUpperCase(Locale.getDefault())
//                        startActivity(notificationNavigate(watchList[position]).apply {
//                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                        })
//                }
//            },
//            viewModel.sharedPrefs.getProviderLogo()
//        )
//        (context?.applicationContext?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)?.cancelAll()
//        binding.watchRecycler.layoutManager = RVLinearLayoutManager(context)
//        watchAdapter.setHasStableIds(true)
//        binding.watchRecycler.adapter = watchAdapter
//        val swipeHandler =
//            object : SwipeToDeleteCallback(requireContext(), viewModel) {
//                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                    val adapter =
//                        binding.watchRecycler.adapter as NotifictionListAdapter
//                    if (watchAdapter.itemCount == 2) {
//                        setNoDataView()
//                    }
//                    adapter.removeAt(viewHolder.adapterPosition)
//                }
//            }
//        val itemTouchHelper = ItemTouchHelper(swipeHandler)
//        itemTouchHelper.attachToRecyclerView(binding.watchRecycler)
//    }
//
//    private fun setTransactionAdapter() {
//        transactionAdapter = NotifictionListAdapter(
//            transactionList,
//            viewModel,
//            requireContext(),
//            viewModel.sharedPrefs.getCloudenieryUrl(),
//            object : NotifictionListAdapter.ItemClickListener {
//                override fun onItemClick(view: View, position: Int) {
//                    transactionAdapter.notifyItemChanged(position)
//                }
//            },
//            viewModel.sharedPrefs.getProviderLogo()
//
//        )
//        binding.transactionRecycler.layoutManager = RVLinearLayoutManager(context)
//        transactionAdapter.setHasStableIds(true)
//        binding.transactionRecycler.adapter = transactionAdapter
//        val swipeHandler =
//            object : SwipeToDeleteCallback(requireContext(), viewModel) {
//                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                    val adapter =
//                        binding.transactionRecycler.adapter as NotifictionListAdapter
//                    if (transactionAdapter.itemCount == 2) {
//                        binding.transactionRecycler.hide()
//                        binding.tvEmpty.show()
//                    }
//                    adapter.removeAt(viewHolder.adapterPosition)
//                }
//            }
//        val itemTouchHelper = ItemTouchHelper(swipeHandler)
//        itemTouchHelper.attachToRecyclerView(binding.transactionRecycler)
//        //  adapter.setItem(response.data.items)
//    }
//
//    private fun readTransactionRawFile(): NotificationResponse {
//        val objectArrayString: String =
//            requireContext().resources.openRawResource(R.raw.notification_transaction_data)
//                .bufferedReader()
//                .use { it.readText() }
//        var data = Gson().fromJson(objectArrayString, NotificationResponse::class.java)
//
//        return data
//    }
//
//    private fun createTransactionList(data: NotificationResponse): NotificationResponse {
//        val readAll = viewModel.isTransactionReadAll
//        if (readAll) {
//            data.data?.recent?.forEach { it.isRead = readAll }
//            data.data?.earlier?.forEach { it.isRead = readAll }
//        }
//        transactionList.clear()
//        val recent = ContentItem()
//        recent.isHeader = true
//        recent.title = getString(R.string.recent)
//        transactionList.add(recent)
//        transactionList.addAll(data.data?.recent!!)
//        val earlier = ContentItem()
//        earlier.isHeader = true
//        earlier.title = getString(R.string.earlier)
//        transactionList.add(earlier)
//        transactionList.addAll(data.data?.earlier!!)
//
//        return data
//    }
//
//    private fun setNoDataView() {
//        binding.watchRecycler.hide()
//        binding.tvEmpty.show()
//        mMenu?.findItem(R.id.edit_notification)?.isVisible = false
//    }
//
//}