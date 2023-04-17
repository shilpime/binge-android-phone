package com.tatasky.binge.ui.features.splash

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import com.facebook.applinks.AppLinkData
import com.tatasky.binge.R
import com.tatasky.binge.analytics.ANALYTICS_TIME_FORMAT
import com.tatasky.binge.analytics.PLATFORM_ANDROID
import com.tatasky.binge.analytics.SID
import com.tatasky.binge.analytics.appsflyer.AppsFlyerHelper
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.GetAppRatingRequest
import com.tatasky.binge.data.networking.models.response.ConfigResponse
import com.tatasky.binge.databinding.FragmentAppSplashBinding
import com.tatasky.binge.helper.DeeplinkHelper
import com.tatasky.binge.helper.DeeplinkHelper.GOOGLE_ANALYTICS_DEFERRED_DEEPLINK_PREF
import com.tatasky.binge.helper.DeeplinkHelper.KEY_DEEPLINK
import com.tatasky.binge.helper.DeeplinkHelper.KEY_TIMESTAMP
import com.tatasky.binge.ui.base.MyApp
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.utils.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class AppSplashFragment : BaseFragment<FragmentAppSplashBinding, SplashViewModel>(),
    MediaPlayer.OnPreparedListener, SurfaceHolder.Callback,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val TAG = this.javaClass.simpleName
    private var preferences: SharedPreferences? = null

    private var isMigratingUser: Boolean = false

    @Inject
    lateinit var appsFlyerHelper: AppsFlyerHelper

    private var isVideoPlaying: Boolean = true
    private var isVideoPrepared: Boolean = false
    private var configResponse: ConfigResponse? = null
    var mMediaPlayer: MediaPlayer? = null
    var triedSecondSplash = false

    private var commonViewModel: CommonSampleViewModel? = null

    @Inject
    lateinit var splashAnalytics: SplashAnalytics

    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics

    override fun getViewModelClass(): Class<SplashViewModel> = SplashViewModel::class.java

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    override fun layoutId(): Int = R.layout.fragment_app_splash

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            KEY_DEEPLINK -> {
                lifecycleScope.launch {
                    val deferredDeeplinkUsingGoogleAnalytics = preferences?.getString(key, null)
                    val capturedTimeInMicroSeconds = preferences?.getLong(KEY_TIMESTAMP, 0L)
                    /**Due to async in nature, Using datastore for storing Deferred deeplink
                     * Will be retrieved later for collection using Flow<T>
                     */
                    dataStorePrefs.saveGoogleOrFacebookDeferredDeeplinkUriInString(
                        deferredDeeplinkUsingGoogleAnalytics
                    )
                    d(
                        TAG,
                        "Google Deferred deeplink: $deferredDeeplinkUsingGoogleAnalytics, Captured time: $capturedTimeInMicroSeconds"
                    )
                }
            }
            else -> Unit
        }
    }

    override fun onStop() {
        super.onStop()
        preferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onStart() {
        super.onStart()
        preferences?.registerOnSharedPreferenceChangeListener(this)

    }

    override fun toBeCalledOnce() {
        preferences = activity?.getSharedPreferences(
            GOOGLE_ANALYTICS_DEFERRED_DEEPLINK_PREF,
            Activity.MODE_PRIVATE
        )
        binding.vm = viewModel
        sharedPrefs.setStartLaunchCount(true)
        commonViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[CommonSampleViewModel::class.java]
        startAnalyticsTracking()
        if (viewModel.isLoggedIn()) {
            var lastLoggedInAppLaunchCount = sharedPrefs.getLoggedInAppLaunchCountForRegionalAppNudge()
            sharedPrefs.saveLoggedInAppLaunchCountForRegionalAppNudge(++lastLoggedInAppLaunchCount)
            if(sharedPrefs.getDthStatusFreemium().isEmpty()
                && sharedPrefs.getAnonymousId() == null) {
                isMigratingUser = true
                commonViewModel?.migrateOldBingeDTHUser(false)
            }
            else {
                updateAnalytics()
            }
        }
        else
            splashAnalytics.registerGuestSuperProperty()
        sharedPrefs.setFirstTimeLandingOpen(true)
        sharedPrefs.removeHierarchyData()
        sharedPrefs.setGameNudgeShown(false)
        e("Splash","sharedPrefs.getMixPanelId() : ${sharedPrefs.getMixPanelId()}")
        init()
        viewModel.fetchConfigResponse()

        if (sharedPrefs.getLoginStatus() && sharedPrefs.getStartLaunchCount()) {
            var lastLaunchValueLoggedIn = viewModel.sharedPrefs.getAppLaunchValueLoggedIn()
            viewModel.sharedPrefs.setAppLaunchValueLoggedIn(++lastLaunchValueLoggedIn)
        }
        if (!sharedPrefs.getLoginStatus() && sharedPrefs.getStartLaunchCount()){
            var lastLaunchValueGuest = viewModel.sharedPrefs.getAppLaunchValueGuest()
            viewModel.sharedPrefs.setAppLaunchValueGuest(++lastLaunchValueGuest)
        }

        var lastLaunchValue = viewModel.sharedPrefs.getAppLaunchValue()
        viewModel.sharedPrefs.setAppLaunchValue(++lastLaunchValue)

        if (sharedPrefs.isEligibleForAppRating()) {
            //Fetch latest eligibility
            val appRatingRequest =
                GetAppRatingRequest(DeviceInfoUtils.getDeviceId(requireContext()))
            viewModel.getAppRatingEligibility(appRatingRequest)
        }
    }

    private fun startAnalyticsTracking() {
        splashAnalytics.registerOrUpdateCommonSuperProperties()
        if (sharedPrefs.getFirstAppLaunchTimeInUTC() == null) {
            val currentTimeInUTC = getTimeInUTC(
                System.currentTimeMillis(),
                ANALYTICS_TIME_FORMAT
            )
            sharedPrefs.setFirstAppLaunchTimeInUTC(currentTimeInUTC)
            splashAnalytics.trackAppLaunch(
                !sharedPrefs.getFirstInvention(),
                currentTimeInUTC
            )
        }
        else {
            splashAnalytics.trackAppLaunch(
                !sharedPrefs.getFirstInvention(),
                sharedPrefs.getFirstAppLaunchTimeInUTC() ?: ""
            )
        }
        if(!sharedPrefs.getFirstInvention()){
            // First app launch, Do something
            splashAnalytics.trackFirstInvention()
            // Set first invention in the last after triggering invention related event
            sharedPrefs.saveFirstInvention()
        }
    }

    private fun updateAnalytics() {
        splashAnalytics.setUserIdentity(sharedPrefs.getMixPanelId())
        sharedPrefs.getLoginResponse()?.let {
            val profile = sharedPrefs.getSelectedProfile()
            it.firstName = profile?.firstName
            it.lastName = profile?.lastName
            it.emailId = profile?.emailId
            it.rmn = sharedPrefs.getClearRMN()
            splashAnalytics.upgradeUserProperty(
                sharedPrefs.getOriginalSubscriberId(),
                it,
                sharedPrefs.getFirstAppLaunchTimeInUTC() ?: "",
                sharedPrefs.getSubscribedPack()?.burnRateType ?: ""
            )
            splashAnalytics.setGroup(
                SID,
                sharedPrefs.getOriginalSubscriberId(),
                it,
                sharedPrefs.getFirstAppLaunchTimeInUTC() ?: "",
                sharedPrefs.getSubscribedPack()?.burnRateType ?: ""
            )
        }
    }

    private fun onCompletedCheck(configResponse: ConfigResponse?) {
        if (configResponse == null || isVideoPlaying) return
        viewModel.sharedPrefs.saveCoachMarkLaunchFrequency(configResponse.data?.config?.coachMark?.androidLaunchFrequency)
        viewModel.sharedPrefs.saveGenericAppLaunchCount(viewModel.sharedPrefs.getGenericAppLaunchCount().inc())
        viewModel.sharedPrefs.setConfigured(true)
        viewModel.sharedPrefs.addConfigResponse(configResponse)
        configResponse.data?.config?.optDuration?.let { viewModel.sharedPrefs.saveOtpDuration(it) }
        configResponse.data?.config?.otpResentCount?.let {
            viewModel.sharedPrefs.saveOtpResentCount(
                it
            )
        }
        viewModel.sharedPrefs.setConfigured(true)
        viewModel.sharedPrefs.saveCloudenieryUrl(configResponse.data?.config?.url?.image?.cloudAccountUrl)
        configResponse.data?.app?.appUpgrade?.android?.let {
            viewModel.sharedPrefs.setConfigAppVersion(it)
        }
        if(configResponse.data?.config?.termConditionPrivacy != null){
            configResponse.data?.config?.termConditionPrivacy?.privacyPolicyUrl?.let {
                viewModel.sharedPrefs.savePrivacyPolicyUrl(it)
            }
            configResponse.data?.config?.termConditionPrivacy?.termConditionsUrl?.let {
                viewModel.sharedPrefs.saveTnCUrl(it)
            }
        }
        else {
            configResponse.data?.config?.url?.privacyPolicyUrlHybrid?.let {
                viewModel.sharedPrefs.savePrivacyPolicyUrl(it)
            }
            configResponse.data?.config?.url?.termsConditionsUrlHybrid?.let {
                viewModel.sharedPrefs.saveTnCUrl(it)
            }
        }

        configResponse.data?.config?.licenseAgreement?.let{
            it.url?.let { it1 -> viewModel.sharedPrefs.saveEulaUrl(it1) }
            it.title?.let { it1 -> viewModel.sharedPrefs.saveEulaTitle(it1) }
            it.subTitle?.let{ it1 -> viewModel.sharedPrefs.saveEulaSubTitle(it1)}
        }
//        configResponse.data?.config?.url?.eulaUrlHybrid?.let {
//            viewModel.sharedPrefs.saveEulaUrl(it)
//        }
        configResponse.data?.config?.maxRechargeAmount?.let {
            viewModel.sharedPrefs.setMaxRechargeAmount(it)
        }
        configResponse.data?.config?.passwordRedirectionTimeInSecs?.let {
            viewModel.sharedPrefs.setPasswordRedirectionTime(it)
        }
        configResponse.data?.config?.connectTimout?.let {
            viewModel.sharedPrefs.saveConnectionTimeout(it)
        }

        configResponse.data?.config?.loaderDelayTime?.let {
            viewModel.sharedPrefs.saveLoaderDelayTime(it)
        }

        configResponse.data?.config?.showMarketingScreen?.let{
            viewModel.sharedPrefs.saveShowMarketingScreen(it)
        }


        configResponse.data?.config?.buttonsEligibility?.let {
            viewModel.sharedPrefs.saveBingeButtonsEligibility(it)
        }
        configResponse.data?.config?.rateLimit?.let{
            viewModel.sharedPrefs.saveRateLimit(it)
        }
        configResponse.data?.config?.primeRedirection?.let {
            viewModel.sharedPrefs.setPrimeRedirectionEnabled(it.enableRedirection)
            viewModel.sharedPrefs.setPrimeRedirectionDelay(it.redirectionDelay?.toIntOrNull()?:0)
        }
        saveHotstarPopupData(configResponse)
        saveFSPopupData(configResponse)
        configResponse.data?.config?.primePopUpFrequency?.primeAndroid?.let {
            viewModel.sharedPrefs.setPrimePopupFrequency(it.primeLaunchFrequency)
            viewModel.sharedPrefs.setPrimePopupInterval(it.primePeriodicFrequency)
        }
        configResponse.data?.config?.primeExistingPopUpFrequency?.primeAndroid?.let {
            viewModel.sharedPrefs.setExistingPrimeInterstitialFrequency(it.primeLaunchFrequency)
            viewModel.sharedPrefs.setExistingPrimeInterstitialInterval(it.primePeriodicFrequency)
        }
        saveAvailableProvider(configResponse.data?.config?.availableProviders)
        configResponse.data?.config?.managedAppEnabled?.let {
            viewModel.sharedPrefs.setManagedAppEnabled(it)
        }
        saveNudgesData(configResponse)
        checkAndMoveToNext()
    }

    private fun saveAvailableProvider(availableProviders: List<ConfigResponse.AvailableProviders>?) {
        val map : HashMap<String, ConfigResponse.AvailableProviders> = HashMap()
        val list = ArrayList<String>()
        availableProviders?.let{
            for (i in it) {
                if(i.platform.contains(PLATFORM_ANDROID)){
                    i.providerName?.let { it1 ->
                        val providerName = it1.toLowerCase()
                        map[providerName] = i
                        list.add(providerName)
                    }
                }
            }
            sharedPrefs.saveAllowedProviders(list)
            sharedPrefs.saveAvailableProviders(map)
        }
    }

    private fun saveFSPopupData(configResponse: ConfigResponse?) {
        configResponse?.data?.app?.ftvPopup?.ftvAndroid?.let {
            if (viewModel.sharedPrefs.getFirestickDialogTimeFrequency() != it.ftvTimeFrequency
                || viewModel.sharedPrefs.getFirestickDialogLaunchFrequency() != it.ftvLaunchFrequency
                || viewModel.sharedPrefs.getFirestickDialogVisibilityType()
                    .isNullOrEmpty() || viewModel.sharedPrefs.getFirestickDialogVisibilityType() != it.ftvPopupType
            ) {
                resetFSPopupData()
            }
            viewModel.sharedPrefs.setFirestickDialogVisibilityType(it.ftvPopupType)
            viewModel.sharedPrefs.setFirestickDialogTimeFrequency(it.ftvTimeFrequency)
            viewModel.sharedPrefs.setFirestickDialogLaunchFrequency(it.ftvLaunchFrequency)
            if (viewModel.sharedPrefs.getFirestickDialogVisibilityType() == it.ftvPopupType
                && viewModel.sharedPrefs.getFirestickDialogVisibilityType() == KEY_DIALOG_VISIBILITY_TYPE_TIME_EVENT
            ) {
                val diff =
                    Calendar.getInstance().timeInMillis - viewModel.sharedPrefs.getFirestickDialogFirstVisbileTime()
                if ((diff / (1000 * 60 * 60) % 24) >= viewModel.sharedPrefs.getFirestickDialogTimeFrequency()) {
                    viewModel.sharedPrefs.setAppLaunchValue(0)
                    viewModel.sharedPrefs.setFirestickDialogFirstVisbileTime(0)
                }
            }
        }
    }

    private fun saveHotstarPopupData(configResponse: ConfigResponse?) {
        configResponse?.data?.config?.hotstarPopUp?.hotstarAndroid?.let {
            if (viewModel.sharedPrefs.getHotstarDialogLaunchFrequency() != it.hotstarLaunchFrequency
                || viewModel.sharedPrefs.getHotstarDialogPeriodicFrequency() != it.hotstarPeriodicFrequency
            ) {
                resetHotstarPopupData()
            }
            viewModel.sharedPrefs.setHotstarDialogLaunchFrequency(it.hotstarLaunchFrequency)
            viewModel.sharedPrefs.setHotstarDialogPeriodicFrequency(it.hotstarPeriodicFrequency)
        }
        val currentDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diff =
            currentDate.timeInMillis - viewModel.sharedPrefs.getHotstarLastFinalPopupShownTime()
        e("launchHotstar","time diff: $diff")
        if (diff / (1000 * 60 * 60 * 24) > viewModel.sharedPrefs.getHotstarDialogPeriodicFrequency()) {
            viewModel.sharedPrefs.resetHotstarPopupCount()
        }
    }

    private fun resetFSPopupData() {
        viewModel.sharedPrefs.resetFSDialog()
        viewModel.sharedPrefs.setFirestickDialogVisibilityType(null)
        viewModel.sharedPrefs.setFirestickDialogTimeFrequency(0)
        viewModel.sharedPrefs.setFirestickDialogLaunchFrequency(0)
    }

    private fun resetHotstarPopupData() {
        viewModel.sharedPrefs.resetHotstarPopupCount()
        viewModel.sharedPrefs.setHotstarLastFinalPopupShownTime(0)
        viewModel.sharedPrefs.setHotstarPopupFirstCycleCompleted(false)
    }

    private fun saveNudgesData(configResponse: ConfigResponse?) {
        configResponse?.data?.app?.nudges?.let {
            it.androidNudge?.freeTrialStartupNudge?.let { freeTrialStartupNudge ->
                viewModel.sharedPrefs.saveFreeTrialStartupNudgeData(freeTrialStartupNudge)
            }
        }
    }

    override fun onError(errorModel: ErrorModel) {
        if (errorModel.statusCode == RESPONSE_CODE_NETWORK_ERROR) {
            mMediaPlayer?.pause()
            binding.btnRetry.setOnClickListener {
                try {
                    if (isNetworkConnected(requireContext())) {
                        binding.clNoNetwork.hide()
                        binding.videoView.show()
                        mMediaPlayer?.start()
                        viewModel.retrySubject.onNext(Any())
                    }
                } catch (e: Exception) {

                }
            }
            binding.videoView.hide()
            binding.clNoNetwork.show()
        }
        else{
            mMediaPlayer?.pause()
            showToast(context, errorModel.message?: GENERIC_ERROR_MSG)
            activity?.onBackPressed()
        }
    }


    override fun onNetworkError(errorMessage: String, isRetry: Boolean) {
        mMediaPlayer?.pause()
        binding.btnRetry.setOnClickListener {
            try {
                if (isNetworkConnected(requireContext())) {
                    binding.clNoNetwork.hide()
                    binding.videoView.show()
                    mMediaPlayer?.start()
                    viewModel.retrySubject.onNext(Any())
                }
            } catch (e: Exception) {

            }
        }
        binding.videoView.hide()
        binding.clNoNetwork.show()
    }

    override fun setObserver() {
        commonViewModel?.migrateUserResponse?.observe(viewLifecycleOwner, {
            it.getContentIfNotHandled().let {
                isMigratingUser = false
                if(sharedPrefs.getLoginStatus())
                    updateAnalytics()
                else
                    context?.let { it1 -> logoutApplication(it1) }
                if(!isVideoPlaying)checkAndMoveToNext()
            }
        })//handling of videoPlayback too with migrate key
        viewModel.retryError.observe(viewLifecycleOwner, Observer {
            if (viewModel.sharedPrefs.isConfigured())
                checkAndMoveToNext()
        })
        viewModel.errorMessage.observe(viewLifecycleOwner, Observer {
            if (viewModel.sharedPrefs.isConfigured()) {
                configResponse = viewModel.sharedPrefs.getConfigResponse()
                if (!isVideoPlaying)
                    checkAndMoveToNext()
            }
        })
        viewModel.configResponse.observe(viewLifecycleOwner, Observer {
            if (it?.data != null) {
                this.configResponse = it
                onCompletedCheck(it)
            } else {
                if (viewModel.sharedPrefs.isConfigured()) {
                    checkAndMoveToNext()
                }
            }
        })
    }

    private fun checkAndMoveToNext() {
        if(isMigratingUser) return
        val appVersion = viewModel.sharedPrefs.getConfigAppVersion()
        //if (appVersion == null || isVersionUpdated(appVersion)) {
            setupDeeplinksAndMoveToNext()
        //}

    }

    override fun dismissUpgradePopup() {
        super.dismissUpgradePopup()
        setupDeeplinksAndMoveToNext()
    }

    private fun tempMoveToLandingActivity() {
        activity?.let {
            val intent = Intent(it, LandingActivity::class.java).apply {
                this.data = it.intent.data
                it.intent.extras?.let { bundle ->
                    this.putExtras(bundle)
                }
                this.putExtra("fromSplash", true)
            }

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            it.startActivity(intent)
            it.finish()
        }
    }

    private fun setupDeeplinksAndMoveToNext() {
        // Sets the Onelink redirection for screens where login is not required
        // Configuration needed after login to redirect the user to specific screen for authenticated screens
        activity?.let { activity ->
            // Onelink redirection handling using Deeplink
            appsFlyerHelper.setupOnelinkRedirection(activity)?.let {
                // Setup the intent data (URI) and any extra if needed
                activity.intent.data = it.first
                activity.intent.putExtras(it.second ?: Bundle())
                appsFlyerHelper.deepLinkDestination =
                    null // After consumption making it null to avoid redirection again
            }
            //Deeplink intent handling using URI
            DeeplinkHelper.createApplinkFromCustomDeeplinkURI(
                activity.intent.data?.toString()
            )?.let { deeplinkBundle ->
                activity.intent.data = deeplinkBundle.first
                activity.intent.putExtras(deeplinkBundle.second)
            }
            //Deferred deeplink using Facebook app links
            AppLinkData.fetchDeferredAppLinkData(requireContext()) { applinksData ->
                applinksData?.targetUri?.let {
                    lifecycleScope.launch {
                        d(TAG, "FB Deferred deeplink: $it")
                        /**Due to async in nature, Using datastore for storing Deferred deeplink
                         * Will be retrieved later for collection using Flow<T>
                         */
                        dataStorePrefs.saveGoogleOrFacebookDeferredDeeplinkUriInString(it.toString())
                    }
                }
            }
        }
        tempMoveToLandingActivity()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (!isVideoPrepared) {
            binding.videoView.alpha = 0f
        }
        if (MyApp.initSucceed && isVideoPrepared) {
            if (mMediaPlayer?.isPlaying == false)
                mMediaPlayer?.start()
        }
        mMediaPlayer?.setDisplay(holder)
        mMediaPlayer?.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        // Set the volume of media player
        mMediaPlayer?.setVolume(0f, 0f) // 0f for mute
        mMediaPlayer?.setOnPreparedListener(this)
        if (!isVideoPrepared)
            try {
                val path = "android.resource://" + requireActivity().packageName
                    .toString() + "/" + R.raw.splash_video_portrait
                mMediaPlayer?.setDataSource(requireContext(), Uri.parse(path))
                mMediaPlayer?.prepare()
            } catch (e: Exception) {
                try {
                    val path = "android.resource://" + requireActivity().packageName
                        .toString() + "/" + R.raw.splash_video_portrait2
                    mMediaPlayer?.setDataSource(requireContext(), Uri.parse(path))
                    mMediaPlayer?.prepare()
                } catch (e:Exception){
                    isVideoPlaying = false
                    onCompletedCheck(configResponse)
                }
            }
        mMediaPlayer?.setOnCompletionListener {
            isVideoPlaying = false
            onCompletedCheck(configResponse)
        }
        mMediaPlayer?.setOnInfoListener { mp, what, extra ->
            if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                binding.videoView.alpha = 1F // to remove black screen before vid starts
                return@setOnInfoListener true
            }
            return@setOnInfoListener false
        }
        mMediaPlayer?.setOnErrorListener { mp, what, extra ->
            if(triedSecondSplash)
                return@setOnErrorListener false
            else {
                triedSecondSplash = true
                try {
                    val path = "android.resource://" + requireActivity().packageName
                        .toString() + "/" + R.raw.splash_video_portrait2
                    mMediaPlayer?.setDataSource(requireContext(), Uri.parse(path))
                    mMediaPlayer?.prepare()
                }catch (e:Exception){
                    isVideoPlaying = false
                    onCompletedCheck(configResponse)
                }
            }
            return@setOnErrorListener true
        }

        if (mMediaPlayer != null) {
            val videoWidth = mMediaPlayer!!.videoWidth
            val videoHeight = mMediaPlayer!!.videoHeight
            //Get the width of the screen
            val screenWidth: Int = requireActivity().windowManager.defaultDisplay.width
            //Get the SurfaceView layout parameters
            val lp: ViewGroup.LayoutParams = binding.videoView.layoutParams
            //Set the width of the SurfaceView to the width of the screen
            lp.width = screenWidth
            //Set the height of the SurfaceView to match the aspect ratio of the video
            //be sure to cast these as floats otherwise the calculation will likely be 0
            lp.height =
                (videoHeight.toFloat() / videoWidth.toFloat() * screenWidth.toFloat()).toInt()

            binding.videoView.layoutParams = lp

//                mMediaPlayer?.start()
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        mMediaPlayer?.pause()
    }


    override fun onPrepared(p0: MediaPlayer?) {
        isVideoPrepared = true
        mMediaPlayer?.start()
    }

    override fun onPause() {
        super.onPause()
        mMediaPlayer?.pause()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
    }

    fun init() {
        mMediaPlayer = MediaPlayer()
        if(binding.videoView != null) {
            binding.videoView.holder.addCallback(this)
            binding.videoView.holder.setFormat(PixelFormat.TRANSPARENT)
            binding.videoView.holder.setFormat(PixelFormat.OPAQUE)
        }
    }
}