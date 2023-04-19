package com.tatasky.binge.ui.features.more

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.facebook.FacebookSdk
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.HomeDirections
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_CHATBOT_DEEPLINK
import com.tatasky.binge.analytics.SOURCE_DEEPLINK
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.FaqResponse
import com.tatasky.binge.data.networking.models.response.SubscriberProfileListModel
import com.tatasky.binge.databinding.FragmentFaqBinding
import com.tatasky.binge.helper.DeeplinkHelper.DeeplinkSubscriptionActions.*
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.more.adapter.FaqAdapter
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.sidemenunavdrawer.SideMenuDrawerAnalytics
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.imagepicker.ImagePicker
import java.io.File
import java.util.Date
import javax.inject.Inject

/**
 * Created by Srikant Karnani on 16/1/20.
 */
class FaqFragment : BaseFragment<FragmentFaqBinding, SettingsViewModel>() {


    @Inject
    lateinit var sideMenuDrawerAnalytics: SideMenuDrawerAnalytics


    @Inject
    lateinit var loginAnalytics: LoginAnalytics

    override fun getViewModelClass(): Class<SettingsViewModel> = SettingsViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_faq

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    private val hcDeeplinkUrlData by lazy {
        activity?.intent?.data?.let {
            it.toString().removePrefix("${it.scheme}://${it.authority}/")
        }
    }

    private fun getUriWithBundle(lastPathSegment: String?): Pair<Uri?, Bundle?>? {
        return when (lastPathSegment) {
            KEY_TRANSACTION_HISTORY -> Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_transaction_history,
                        BuildConfig.hostName
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )
            KEY_DEVICE_MANAGEMENT -> Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_device_management,
                        BuildConfig.hostName
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )
            KEY_EDIT_PROFILE -> Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_edit_profile,
                        BuildConfig.hostName
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )
            KEY_SETTING -> Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_settings,
                        BuildConfig.hostName
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )
            //Redirects to MySubscription based on subscription status
            KEY_SUBSCRIPTION -> Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_subscription,
                        BuildConfig.hostName,
                        "",
                        ""
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )
            KEY_LANGUAGE -> Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_content_language,
                        BuildConfig.hostName
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )
            KEY_PARENTAL_CONTROL -> Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_parental_control,
                        BuildConfig.hostName
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )
            KEY_HOME -> Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_home,
                        BuildConfig.hostName
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )
            else -> null
        }
    }

    private fun getEditProfileDataAndNavigate() {
        val savedUserData = viewModel.sharedPrefs.getSelectedProfile()
        val userData = SubscriberProfileListModel.Data()
        userData.email = savedUserData?.emailId
        userData.firstName = savedUserData?.firstName
        userData.lastName = savedUserData?.lastName
        userData.rmn = savedUserData?.rmn
        userData.image = savedUserData?.imageUrl
        findNavController().navigateSafe(
            HomeDirections.actionGlobalEditProfileFragment(
                userData
            )
        )
    }

    private fun moveToSelection(lastPathSegment: String?) {
        when (lastPathSegment) {
            KEY_TRANSACTION_HISTORY -> {
                getDeeplinkIntent(Pair(
                    Uri.parse(
                        getString(
                            R.string.deeplink_transaction_history,
                            BuildConfig.hostName
                        )
                    ),
                    Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                    }
                )).let {
                    findNavController().navigate(
                        R.id.transactionHistoryFragment,
                        it?.extras
                    )
                }

            }
            KEY_DEVICE_MANAGEMENT ->
                getDeeplinkIntent(Pair(
                    Uri.parse(
                        getString(
                            R.string.deeplink_device_management,
                            BuildConfig.hostName
                        )
                    ),
                    Bundle().apply {
                        this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                    }
                )).let {
                    findNavController().navigate(
                        R.id.deviceListFragment,
                        it?.extras
                    )
                }
            KEY_EDIT_PROFILE -> getDeeplinkIntent(Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_edit_profile,
                        BuildConfig.hostName
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )).let {
                getEditProfileDataAndNavigate()
            }
            KEY_SETTING -> getDeeplinkIntent(Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_settings,
                        BuildConfig.hostName
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )).let {
                findNavController().navigateSafe(
                    R.id.action_global_settingsFragment,
                    it?.extras
                )
            }
            //Redirects to MySubscription based on subscription status
            KEY_SUBSCRIPTION -> {
                if (sharedPrefs.getEligibleForFreeTrial()) return //Eligible for Free trial, block deeplinking
                val currentPack = sharedPrefs.getSubscribedPack()
                (activity as LandingActivity).apply {

                    val intent = Intent().apply {
                        data =
                            Uri.parse(
                                getString(
                                    R.string.deeplink_subscription,
                                    BuildConfig.hostName,
                                    "",
                                    ""
                                )
                            )
                        putExtras(Bundle().apply {
                            this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                        })

                    }
                    //Todo check for managedApp
                    startActivity(
                        getSubscriptionActivityIntent(
                            this,
                            selectedAppId = null,
                            fromScreen = getSourceOrFromScreenName(),
                            packName = intent.data?.getQueryParameter(KEY_PACK_NAME),
                            startPackListing =
                            (intent.data?.getQueryParameter(ACTION) == ACTION_PACK_SELECTION.action &&
                                    currentPack?.planCTADetails?.changePlanOption == true)
                        ).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.data?.getQueryParameter(ACTION)?.takeIf { it.isNotBlank() }
                                ?.let {
                                    /*If parameter value is null or unhandled, by default user redirected to My plan or as per the rule else to the screen for which action is configured*/
                                    putExtra(ACTION, it)
                                }
                        })
                }

            }
            KEY_LANGUAGE -> getDeeplinkIntent(Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_content_language,
                        BuildConfig.hostName
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )).let {
                val verbiage = sharedPrefs.getConfigResponse()?.data?.config?.getLanguageVerbiage(
                    CATEGORY_LANGUAGE_SETTING
                )
                val contentLangTitle =
                    verbiage?.data?.header ?: getString(R.string.content_language)
                findNavController().navigate(
                    R.id.contentLanguage,
                    Bundle().apply {
                        putString(KEY_TITLE, contentLangTitle)
                        it?.extras
                    }
                )
            }
            KEY_PARENTAL_CONTROL -> getDeeplinkIntent(Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_parental_control,
                        BuildConfig.hostName
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )).let {
                findNavController().navigateSafe(
                    R.id.action_global_parentalPinMenuFragment,
                    it?.extras
                )
            }
            KEY_HOME -> getDeeplinkIntent(Pair(
                Uri.parse(
                    getString(
                        R.string.deeplink_home,
                        BuildConfig.hostName
                    )
                ),
                Bundle().apply {
                    this.putString(KEY_FROM_SCREEN, SOURCE_CHATBOT_DEEPLINK)
                }
            )).let {
                startActivityWithSafeIntent(
                    getDeeplinkIntent(
                        getUriWithBundle(lastPathSegment)
                    )
                )
            }
        }
    }


    private fun startActivityWithSafeIntent(intent: Intent?) {
        intent?.let {
            startActivity(it)
            //Add transition for smooth navigation
            activity?.overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)
        }
    }

    private fun getDeeplinkIntent(
        deeplinkUriWithBundle: Pair<Uri?, Bundle?>?,
        calledActivity: Class<*> = LandingActivity::class.java
    ): Intent? {
        activity?.let {
            return Intent(it, calledActivity).apply {
                data =
                    deeplinkUriWithBundle?.first
                putExtras(deeplinkUriWithBundle?.second ?: Bundle())
                /*Added flag to fix behavior of bottom nav and redirection from any NavGraph
                It will recreate the task, And then redirect to the screen as per Uri.
                Explicit deeplink has same behaviour so doing manually with Flags
                */
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        return null
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(
        true // default to enabled
    ) {
        override fun handleOnBackPressed() {
            if (binding.wvFaq.canGoBack()) {
                binding.wvFaq.goBack()
            } else if (activity?.intent?.extras?.getString(
                    KEY_FROM_SCREEN,
                    null
                ) == SOURCE_DEEPLINK
            )
                (activity as? AppCompatActivity)?.let { findNavController().navigateUpOrOpenHome(it) }
            else {
                isEnabled = false
                activity?.onBackPressed()
            }
        }
    }

//    Get Help and Support

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            duration = 500
        }
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            duration = 500
        }
        returnTransition = backward
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner, // LifecycleOwner
            onBackPressedCallback
        )
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun setObserver() {
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigateUp()
            }
        })
        viewModel.getFaqResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { faqResponse ->
                faqResponse.data?.let {
                    sharedPrefs.setFaqData(it)
                    val urlToOpen =
                        (viewModel.sharedPrefs.getConfigResponse()?.data?.config?.helpCenterInfo?.helpCenterTokenBaseUrl + it.helpCenterToken)
                            .toUri()
                            .buildUpon()
                            .appendQueryParameter(
                                HELP_CENTER_DEEPLINK_URL_DATA,
                                hcDeeplinkUrlData
                            )
                            .toString()
                    setWebView(urlToOpen)

                } ?: run { onError(ErrorModel()) }
            }
        })
    }

    fun setAdapter(faqResponseList: List<FaqResponse.Question>) {
        val adapter = FaqAdapter()
        adapter.setList(faqResponseList)
        binding.faqRecycler.layoutManager = LinearLayoutManager(context)
        (binding.faqRecycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        binding.faqRecycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        binding.toolbarLayout.findViewById<Toolbar>(R.id.toolbar)?.let {
            it.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_cross)
            it.setNavigationOnClickListener { findNavController().navigateUp() }
        }
    }

    override fun toBeCalledOnce() {
        binding.lifecycleOwner = viewLifecycleOwner
        val title =
            viewModel.sharedPrefs.getConfigResponse()?.data?.config?.helpCenterInfo?.helpCenterHeading
        if (!title.isNullOrBlank()) {
            findNavController().currentDestination?.label = title
            (activity as LandingActivity).supportActionBar?.title = title
        }

        if (viewModel.sharedPrefs.getLoginStatus())
        {

            sharedPrefs.getFaqData()?.let {
                val differenceInTimeStamp = Date().time - it.helpCenterTokenTimeStamp
                val hours = ((differenceInTimeStamp) / (1000 * 60 * 60))
                if (!it.helpCenterToken.isNullOrEmpty() && hours < 24) {
                    val urlToOpen =
                        (viewModel.sharedPrefs.getConfigResponse()?.data?.config?.helpCenterInfo?.helpCenterTokenBaseUrl
                            + it.helpCenterToken)
                            .toUri()
                            .buildUpon()
                            .appendQueryParameter(
                                HELP_CENTER_DEEPLINK_URL_DATA,
                                hcDeeplinkUrlData
                            )
                            .toString()
                    setWebView(urlToOpen)
                } else {
                    viewModel.fetchFaqs()
                }

            } ?: kotlin.run {
                viewModel.fetchFaqs()
            }
        }
        else {
            val urlWithMixpanelId =
                Uri.parse(viewModel.sharedPrefs.getConfigResponse()?.data?.config?.helpCenterInfo?.helpCenterUrl)
                    .buildUpon()
                    .appendQueryParameter("mixpanelId", loginAnalytics.getMixPanelUnifiedId())
            val urlToOpen = urlWithMixpanelId
                .appendQueryParameter(
                    HELP_CENTER_DEEPLINK_URL_DATA,
                    hcDeeplinkUrlData
                )
                .toString()
            setWebView(urlToOpen)
        }
    }

    private fun setWebView(url: String) {
        val decodedUrl = Uri.decode(url)
        if (isNetworkConnected(requireContext())) {
            viewModel.setProgressing(true)
            binding.wvFaq.settings.javaScriptEnabled = true
            binding.wvFaq.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.darkBackground
                )
            )
            binding.wvFaq.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            binding.wvFaq.webChromeClient = MyChrome()
            binding.wvFaq.settings.domStorageEnabled = true
            binding.wvFaq.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    viewModel.setProgressing(true)
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    viewModel.setProgressing(false)
                    super.onPageCommitVisible(view, url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    viewModel.setProgressing(false)
//                    injectCSS()
                    super.onPageFinished(view, url)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: ""
                    e("Help n Support", "url: $url")
                    return try {
                        if (checkUrlEligibilityForHelpCenter(url)) {
                            if (getUriWithBundle(request?.url?.lastPathSegment?.lowercase()) == null)
                                super.shouldOverrideUrlLoading(view, request)
                            else {
//                                startActivityWithSafeIntent(
//                                    getDeeplinkIntent(
//                                        getUriWithBundle(request?.url?.lastPathSegment?.lowercase())
//                                    )
//                                )
                                moveToSelection(request?.url?.lastPathSegment?.lowercase())

                                true
                            }
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(url)
                            startActivity(intent) // view.context.startActivity(intent);
                            true
                        }
                    } catch (e: Exception) {
                        super.shouldOverrideUrlLoading(view, request)
                    }
                }
            }
            binding.wvFaq.loadUrl(decodedUrl)
        } else {
            onNetworkError("", false)
        }
    }

    private fun checkUrlEligibilityForHelpCenter(url: String): Boolean {
        viewModel.sharedPrefs.getConfigResponse()?.data?.config?.helpCenterInfo?.let {
            for (eligibleUrl in it.helpCenterAllowedURL) {
                if (url.contains(eligibleUrl)) {
                    return true
                }
            }
        }
        return false
    }

    var mUploadFile: ValueCallback<Array<Uri>>? = null
    inner class MyChrome internal constructor() : WebChromeClient() {
        private var mCustomView: View? = null
        private var mCustomViewCallback: WebChromeClient.CustomViewCallback? = null
        protected var mFullscreenContainer: FrameLayout? = null
        private var mOriginalOrientation = 0
        private var mOriginalSystemUiVisibility = 0
        override fun getDefaultVideoPoster(): Bitmap? {
            return if (mCustomView == null) {
                null
            } else BitmapFactory.decodeResource(
                FacebookSdk.getApplicationContext().getResources(),
                2130837573
            )
        }

        override fun onHideCustomView() {
            (activity?.window?.decorView as FrameLayout).removeView(
                mCustomView
            )
            mCustomView = null
            activity?.getWindow()?.getDecorView()?.setSystemUiVisibility(
                mOriginalSystemUiVisibility
            )
            activity?.setRequestedOrientation(mOriginalOrientation)
            mCustomViewCallback?.onCustomViewHidden()
            mCustomViewCallback = null
        }

        override fun onShowCustomView(
            paramView: View?,
            paramCustomViewCallback: WebChromeClient.CustomViewCallback?
        ) {
            if (mCustomView != null) {
                onHideCustomView()
                return
            }
            mCustomView = paramView
            mOriginalSystemUiVisibility =
                activity?.window?.decorView?.systemUiVisibility ?: View.SYSTEM_UI_FLAG_FULLSCREEN
            mOriginalOrientation =
                activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            mCustomViewCallback = paramCustomViewCallback
            (activity?.window?.decorView as FrameLayout?)?.addView(
                mCustomView,
                FrameLayout.LayoutParams(-1, -1)
            )
            activity?.window?.decorView?.systemUiVisibility =
                3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }

//        override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
//            Log.d("HelpCenterWebLog", "${message?.message()} -- From line " +
//                    "${message?.lineNumber()} of ${message?.sourceId()}")
//            return true
//
//        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            if (filePathCallback != null) {
                mUploadFile = filePathCallback
            }

            ImagePicker.getGalleryImage(requireActivity(), this@FaqFragment).apply {
                if (this != null) {
                    openGallery(this)
                }
            }
//            selectImage()
            return true
        }


    }

    companion object {
        private const val HELP_CENTER_DEEPLINK_URL_DATA = "hcDeeplinkUrl"
        private const val HELP_CENTER_DEEPLINK_PATH = "help-center"
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (isNetworkConnected(requireContext())) {
                var newData = Intent()
                if (data != null) {
                    newData = data
                }
                ImagePicker.onActivityResult(
                    requireContext(),
                    requestCode,
                    resultCode,
                    newData,
                    object :
                        ImagePicker.OnImagePicked() {
                        override fun onSuccess(imageFile: File?, bm: Bitmap?) {
                            bm?.let {

                                val yourUri = Uri.fromFile(imageFile)
                                val result = arrayOf(yourUri)
                                mUploadFile!!.onReceiveValue(result)
                                mUploadFile = null

                            }
                        }

                        override fun onError(msg: String?) {
                            // Log image Pick failed
                            mUploadFile!!.onReceiveValue(null)
                        }

                    })
            } else {
                onNetworkError("File uploading failed", false)
                mUploadFile!!.onReceiveValue(null)
            }
        } else {
            mUploadFile!!.onReceiveValue(null)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantedResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        when (requestCode) {
            ImagePicker.PERMISSION_REQUEST_CODE ->
                if (grantedResults.isNotEmpty() && grantedResults.get(0) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery(ImagePicker.pickImageGalleryIntent)
                } else {
                    showToast(context, getString(R.string.permission_denied))
                }
        }
    }

    private fun openGallery(intent: Intent) {
        startActivityForResult(
            intent,
            ImagePicker.GALLERY_INTENT
        )
    }


    private fun selectImage() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Choose File")
        builder.setCancelable(false)
        builder.setItems(options, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, item: Int) {
                if (options[item] == "Take Photo") {
                    startActivityForResult(
                        ImagePicker.getPickImageCameraIntent(context!!),
                        ImagePicker.CAMERA_INTENT
                    )
                } else if (options[item] == "Choose from Gallery") {
                    ImagePicker.getGalleryImage(requireActivity(), this@FaqFragment).apply {
                        if (this != null) {
                            openGallery(this)
                        }
                    }
                } else if (options[item] == "Cancel") {
                    mUploadFile!!.onReceiveValue(null)
                    dialog.dismiss()
                }
            }
        })
        builder.show()
    }



}
