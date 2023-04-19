package com.tatasky.binge.ui.base.frameworks.base

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.BaseResponse
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.interfaces.ConfettiDialogEventListener
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.ui.base.MyApp
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.ui.features.dialog.ConfettiDialogModel
import com.tatasky.binge.ui.features.dialog.ConfettiDialogViewModel
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.dialog.DialogViewModel
import com.tatasky.binge.utils.*
import dagger.android.support.AndroidSupportInjection
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.regex.Pattern
import javax.inject.Inject

abstract class BaseBottomSheetDialogFragment<VB : ViewDataBinding,VM : BaseViewModel>(private val isExpanded:Boolean = false, private val tabSupported : Boolean = true) : BottomSheetDialogFragment() {

    abstract fun getViewModelClass(): Class<VM>
    abstract fun layoutId(): Int
    private var isRetryOnNetwork: Boolean = false
    protected var isNetworkAvailable = true
    private var isLoaded: Boolean = false
    private var isSessionTimedOut = false
    private val mHandler = Handler(Looper.getMainLooper())
    protected var showProgress:Runnable = Runnable {  }
    var mActivity : BaseActivity<*> ?= null
    var loaderDelayTime = 0L
    var mBottomSheetBehavior: BottomSheetBehavior<*>? = null
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var sharedPrefs: PrefsRepo

    @Inject
    lateinit var localBroadcastHelper: LocalBroadcastHelper

    protected lateinit var binding: VB
    protected lateinit var viewModel: VM
    protected lateinit var dialogViewModel: DialogViewModel
    protected lateinit var confettiDialogViewModel: ConfettiDialogViewModel

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if(context is BaseActivity<*>){
            mActivity = context
        }
    }

    open fun onError(errorModel: ErrorModel) {
        e("BaseFragment","inside onError : $errorModel")
        if(errorModel.statusCode== RESPONSE_CODE_SUCCESS || errorModel.statusCode== CODE_SUCCESS){
            showDialog(
                DialogModel(false, null, errorModel.message, getString(R.string.ok), null),
                object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        viewModel.setErrorOkClicked()
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                    }
                })
        }
        else {
            showDialog(
                DialogModel(false,
                    R.drawable.ic_subscription_error,
                    errorModel.title,
                    "Ok", null,
                    errorModel.message, errorModel.statusCode),
                object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        viewModel.setErrorOkClicked()
                        hideDialog()
                    }

                    override fun onCloseButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                    }
                })
        }
    }

    override fun getContext(): Context? {
        if (super.getContext()==null){
            startHomeScreen(activity)
        }
        return super.getContext()
    }

    open fun onNetworkError(errorMessage: String, isRetry: Boolean) {
//        showDialog(
//            DialogModel(false, R.drawable.ic_internet,
//                getString(R.string.network_title),
//                if(isRetry) getString(R.string.retry) else getString(R.string.ok),
//                null,
//                getString(R.string.network_error_message)),
//            (activity as BaseActivity<*>).getListener(isRetry, viewModel)
//        )
        showToast(context,getString(R.string.network_title), R.drawable.ic_internet_small)
        /*object : CommonDialogEventListener {
            override fun onPrimaryButtonClick() {
                hideDialog()
                if(isRetry) {
                    showProgressLoading()
                    viewModel.retrySubject.onNext(Any())
                }
                else
                    viewModel.setErrorOkClicked()
            }

            override fun onSecondaryButtonClick() {
                viewModel.setErrorOkClicked()
                hideDialog()
            }

            override fun onCloseButtonClick() {
                viewModel.setErrorOkClicked()
                hideDialog()
            }
        })*/
    }

    override fun getTheme(): Int {

        this.context?.let{ctx->
            if(!isTablet(ctx) && !tabSupported)
                return R.style.AppBottomSheetDialogTheme
        }

        return 0
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return super.onCreateDialog(savedInstanceState)
        this.context?.let{ctx->
            if(isTablet(ctx) && tabSupported)
                return Dialog(ctx, theme)
        }
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (::binding.isInitialized) {
            return binding.root

            /* if(::binding.isInitialized){
           if(binding.root.parent != null){
               (binding.root.parent as ViewGroup).endViewTransition(binding.root)
           }
       }
           if (binding.root.parent != null) {
               (binding.root.parent as ViewGroup).removeAllViews()
           }*/
        }
        binding = DataBindingUtil.bind(inflater.inflate(layoutId(), container, false))!!
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    abstract fun getViewModelOwner(): ViewModelStoreOwner

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.setOnShowListener { dialog ->
            (dialog as? BottomSheetDialog?)
                ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.let { bottomSheetInternal ->
                    mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetInternal)
                    if (isExpanded)
                        mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                }
        }
        if (activity != null) {
            (activity as? BaseActivity<*>)?.hideProgress()
        }
        loaderDelayTime = sharedPrefs.getLoaderDelayTime()
        showProgress = Runnable {
            mActivity?.showProgress()
        }
        viewModel = ViewModelProvider(getViewModelOwner(), viewModelFactory)[getViewModelClass()]
        viewModel.setProgressing(false)
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let {
                if (it.statusCode == RESPONSE_CODE_NETWORK_ERROR) {
                    onNetworkError(it.message ?: NETWORK_ERROR_MSG, false)
                }else if(it.statusCode == RESPONSE_CODE_RATE_LIMIT){
                    sharedPrefs.getRateLimit()?.let { it1 ->
                        onError(ErrorModel(statusCode = it.statusCode, message = it1))
                    }
                } else if(it.statusCode == RESPONSE_CODE_TIMEOUT) {
                    showDialog(
                        DialogModel(false, R.drawable.ic_subscription_error, it.message, getString(R.string.ok), null),
                        object : CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                viewModel.setErrorOkClicked()
                                hideDialog()
                            }

                            override fun onCloseButtonClick() {
                                hideDialog()
                            }

                            override fun onSecondaryButtonClick() {
                            }
                        })
                }else {
                    onError(it)
                }
            }
        })
        viewModel.retryError.observe(viewLifecycleOwner, Observer { it ->
            it.getContentIfNotHandled()?.let {
                isRetryOnNetwork = true
                if ( it is SocketTimeoutException){
                    showDialog(
                        DialogModel(false, R.drawable.ic_subscription_error, TIMEOUT_ERROR_MSG, getString(R.string.ok), null),
                        object : CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                viewModel.setErrorOkClicked()
                                hideDialog()
                            }

                            override fun onCloseButtonClick() {
                                hideDialog()
                            }

                            override fun onSecondaryButtonClick() {
                            }
                        })
                }
                else if( it is IOException) {
                    onNetworkError(it.message ?: NETWORK_ERROR_MSG, viewModel._isRetry.value?.getContentIfNotHandled() ?: false)
                } else if (it is HttpException){
                    when( it.code()) {
                        RESPONSE_CODE_NOT_FOUND ->
                            onError(ErrorModel(message = UNREACHABLE_ERROR_MSG,
                                statusCode = it.response()?.code() ?: -1))
                        RESPONSE_CODE_DEACTIVATED,
                        RESPONSE_CODE_500,
                        RESPONSE_CODE_BAD_GATEWAY,
                        RESPONSE_CODE_SERVICE_TEMPORARY_UNAVAILABLE ->
                            try {
                                val response = Gson().fromJson<BaseResponse>(it.response()?.errorBody()?.string(), BaseResponse::class.java)
                                onError(ErrorModel(statusCode = it.response()?.code() ?: -1,
                                    message = response.message?: COMMON_ERROR_MSG,
                                    title = response.title ?: COMMON_ERROR_TITLE))
                            }catch (exception:Exception) {
                                onError(ErrorModel(statusCode = it.response()?.code() ?: -1))
                            }
                        RESPONSE_CODE_UNAUTHORIZED-> {
                            forceLogout()
                            isSessionTimedOut = true
                        }
                        else ->
                            onError(ErrorModel(statusCode = it.response()?.code() ?: -1,
                                message = it.message?: COMMON_ERROR_MSG))
                    }
                }else{
                    onError(ErrorModel(message = it.message))
                }
            }
        })
        viewModel.progressListener.observe(viewLifecycleOwner, Observer {
            e("GuestLoginBottomSheet","inside progressListener $it")
            if (it) {
                mHandler.postDelayed(
                    showProgress, loaderDelayTime
                )
                if(false==mActivity?.allowedTouchWhenLoading()) {
                    activity?.window?.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    view?.closeKeyboard()
                }
            } else {
                mHandler.removeCallbacks(showProgress)
                mActivity?.hideProgress()
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        })

        viewModel.forceLogout.observe(viewLifecycleOwner, Observer {
            forceLogout()
            isSessionTimedOut = true
        })
        viewModel.forceDeviceStatusLogout.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                showDeviceStatusLogout()
            }
        })
        if (!isLoaded) {
            if (activity != null) {
                (activity as? BaseActivity<*>)?.hideDialog()
                (activity as? BaseActivity<*>)?.hideProgress()
            }
            toBeCalledOnce()
            isLoaded = true
        }
        if(!mPreventSetObserver)
            setObserver()
    }
    private var mPreventSetObserver = false
    fun preventSetObserver(){
        mPreventSetObserver = true
    }

    open fun forceLogout() {
        if(sharedPrefs.getLoginStatus())(activity as? BaseActivity<*>)?.showLogoutDialog()
        (activity?.application as MyApp).clearAllData()
    }


    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(showProgress)
        view?.closeKeyboard()
    }

    protected fun hideRetry() {
        hideDialog()
    }

    open fun showDialog(dialogModel: DialogModel, eventListener: CommonDialogEventListener) {
        if(!isSessionTimedOut) {
            if (!::dialogViewModel.isInitialized) {
                dialogViewModel =
                    ViewModelProvider(
                        requireActivity(),
                        viewModelFactory
                    ).get(DialogViewModel::class.java)
            }
            dialogViewModel.setDialogModel(dialogModel)
            dialogViewModel.setEventHandler(eventListener)
            (activity as? BaseActivity<*>)?.showDialog()
        }
    }

    open fun showConfettiDialog(dialogModel: ConfettiDialogModel, eventListener: ConfettiDialogEventListener) {
        if(!isSessionTimedOut) {
            if (!::dialogViewModel.isInitialized) {
                confettiDialogViewModel =
                    ViewModelProvider(
                        requireActivity(),
                        viewModelFactory
                    ).get(ConfettiDialogViewModel::class.java)
            }
            confettiDialogViewModel.setDialogModel(dialogModel)
            confettiDialogViewModel.setEventHandler(eventListener)
            (activity as? BaseActivity<*>)?.showConfettiDialog()
        }
    }

    open fun hideDialog() {
        activity?.let {
            (it as? BaseActivity<*>)?.hideDialog()
        }?:mActivity?.hideDialog()
    }

    override fun onResume() {
        super.onResume()
        view?.findViewById<ViewGroup>(R.id.toolbar_layout)?.findViewById<Toolbar>(R.id.toolbar)?.let {
            (activity as? BaseActivity<*>)?.setSupportActionBar(it)
            val label = findNavController().currentDestination?.label
            if (label != null) {
                // Fill in the data pattern with the args to build a valid URI
                val title = StringBuffer()
                val fillInPattern = Pattern.compile("\\{(.+?)\\}")
                val matcher = fillInPattern.matcher(label)
                while (matcher.find()) {
                    val argName = matcher.group(1)
                    if (arguments != null && requireArguments().containsKey(argName)) {
                        matcher.appendReplacement(title, "")
                        title.append(requireArguments()[argName].toString())
                    } else {
                        throw IllegalArgumentException("Could not find " + argName + " in "
                                + arguments + " to fill label " + label)
                    }
                }
                matcher.appendTail(title)
                it.title = title
            }
            if(navController().currentDestination?.id != navController().graph.startDestination){
                it.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back_key)
                it.setNavigationOnClickListener { findNavController().navigateUp() }
            } else if (navController().currentDestination?.id == R.id.parentalControlSettingsFragment) {
                it.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back_key)
                it.setNavigationOnClickListener { activity?.onBackPressed() }
            } else {
                it.navigationIcon = null
            }
        }
    }

    //use SingleEvent LiveData for Observing Only once, use ViewLifeCycleOwner in most cases.
    abstract fun setObserver()

    abstract fun toBeCalledOnce()

    fun navController() = Navigation.findNavController(requireView())


    override fun onStart() {
        context?.let {
            localBroadcastHelper.registerBroadcast(it, mLogoutListener, localBroadcastHelper.ACTION_LOGOUT)
            localBroadcastHelper.registerBroadcast(it, mLogoutListener, localBroadcastHelper.ACTION_ATV_CANCELLED_SWITCH)
            localBroadcastHelper.registerBroadcast(it, mLogoutListener, localBroadcastHelper.ACTION_DEVICE_STATUS_LOGOUT)
//            localBroadcastHelper.registerBroadcast(it, mLogoutListener, localBroadcastHelper.ACTION_DATA_POSTED)
            localBroadcastHelper.registerBroadcast(it, mLogoutListener, localBroadcastHelper.ACTION_NOTIFICATION_RECEIVED)
            localBroadcastHelper.registerBroadcast(it, mLogoutListener, localBroadcastHelper.ACTION_PLAN_CHANGED)
            localBroadcastHelper.registerBroadcast(it, mLogoutListener, localBroadcastHelper.ACTION_SUBSCRIPTION_UPDATED)
        }
        registerUnregisterNetworkCallback(true)
        super.onStart()
    }

    override fun onStop() {
        registerUnregisterNetworkCallback(false)
        super.onStop()
    }

    open val mLogoutListener: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            e("pubnub", "action " + intent.action)
            when {
                localBroadcastHelper.ACTION_LOGOUT == intent.action -> {
                    forceLogout()
                    isSessionTimedOut = true
                }
                localBroadcastHelper.ACTION_ACCOUNT_DEACTIVE == intent.action -> {
                    //show deactive account alert
//                    handleDeactivateDialog()
                }
                localBroadcastHelper.ACTION_NOTIFICATION_RECEIVED == intent.action -> {
                    refreshNotificationCount()
                }
                localBroadcastHelper.ACTION_DEVICE_STATUS_LOGOUT == intent.action -> {
                    showDeviceStatusLogout()
                }
                localBroadcastHelper.ACTION_ATV_CANCELLED_SWITCH == intent.action -> {
                    showATVCancelledSwitchDialog()
                }
                localBroadcastHelper.ACTION_PLAN_CHANGED == intent.action -> {
                    showCustomUpgradeToast(this@BaseBottomSheetDialogFragment.context, intent.getStringExtra("title")?:"Plan Changed",intent.getStringExtra("message")?:"Your Subscription has been modified successfully" )
                }
                else -> {
                    viewModel.updateInpack()
                }
            }
        }
    }

    open fun refreshNotificationCount(){
    }

    protected fun handleDeactivateDialog(source: String) {
        showDialog(
            DialogModel(
                false,
                R.drawable.ic_subscription_error,
                getString(R.string.subscription_inactive),
                getString(R.string.recharge),
                getString(R.string.skip),
                getString(R.string.recharge_binge_deactive)
            ), object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    hideDialog()
                    activity?.let {
                        startActivity(getSubscriptionActivityIntent(it, false, null, source, initiateRecharge = true, fromDialog = true))
                    }
                    //Need to go to Recharge screen
                }
                override fun onSecondaryButtonClick() {
                    hideDialog()
                }

                override fun onCloseButtonClick() {
                    hideDialog()
                }
            })
    }

    /*Will need to update once verbiages updated*/
    protected fun handleCancelledDeactivateDialog(source: String) {
        showDialog(
            DialogModel(
                false,
                R.drawable.ic_subscription_error,
                getString(R.string.subscription_inactive),
                getString(R.string.recharge),
                getString(R.string.skip),
                getString(R.string.recharge_binge_deactive)
            ), object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    hideDialog()
                    activity?.let {
                        startActivity(getSubscriptionActivityIntent(it, false, null, source, initiateRecharge = true, fromDialog = true))
                    }
                    //Need to go to Recharge screen
                }
                override fun onSecondaryButtonClick() {
                    hideDialog()
                }

                override fun onCloseButtonClick() {
                    hideDialog()
                }
            })
    }


    protected open val networkCallback: ConnectivityManager.NetworkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                activity?.let {
                    val retryView = (activity as? BaseActivity<*>)?.retryView
                    if (retryView != null && retryView.isVisible) {
                        requireActivity().runOnUiThread {
                            hideRetry()
                            hideDialog()
                            showProgressLoading()
                        }
                        viewModel.retrySubject.onNext(Any())
                    } else if (isRetryOnNetwork) {
                        requireActivity().runOnUiThread {
                            //hideRetry()
                            hideDialog()
                            showProgressLoading()
                        }
                        viewModel.retrySubject.onNext(Any())
                    }
                    onNetworkAvailable()
                    isRetryOnNetwork = false
                }
            }

            override fun onLost(network: Network) {
                /* This is a multi-line comment. I'm writing this
                * text to show you that we can write the comments
                * in multiple lines
                */
            }
        }

    open fun onNetworkAvailable() { }

    private fun showProgressLoading() {
        viewModel.setProgressing(true)
    }

    override fun onDestroyView() {
        context?.let {
            localBroadcastHelper.unregisterBroadcast(it, mLogoutListener)
        }
        super.onDestroyView()
    }

    /**
     * This Method is used to register and un-register the network callback
     *
     * @param isRegister
     */
    protected fun registerUnregisterNetworkCallback(isRegister: Boolean) {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val builder = NetworkRequest.Builder()
        if (isRegister) {
            connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
        } else {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    open fun showDeviceStatusLogout() {
        (activity as? BaseActivity<*>)?.showDeviceLogoutDialog(getString(R.string.deviceStatusLogoutDialogTitle), getString(R.string.deviceStatusLogoutDialogText))
    }

    open fun showATVCancelledSwitchDialog(){
        if(sharedPrefs.getSubscribedPack()?.largeScreenEligibility == false){
            (activity as? BaseActivity<*>)?.showATVCancelledDialog(
                context?.getString(R.string.atv_cancel_dialog_msg)
            )
        }
        if(sharedPrefs.getSubscribedPack()?.largeScreenEligibility == true){
            (activity as? BaseActivity<*>)?.showATVCancelledDialog(
                context?.getString(R.string.atv_cancel_dialog_msg1)
            )
        }
    }


}