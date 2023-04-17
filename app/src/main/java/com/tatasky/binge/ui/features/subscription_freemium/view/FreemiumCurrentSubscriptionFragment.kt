package com.tatasky.binge.ui.features.subscription_freemium.view

import android.content.res.Configuration
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.Visibility
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.Gson

import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.MYPLAN_CHANGE
import com.tatasky.binge.analytics.MYPLAN_TENURE
import com.tatasky.binge.analytics.SOURCE_ACCOUNT
import com.tatasky.binge.analytics.SOURCE_HOME
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.FragmentFreemiumCurrentSubscriptionBinding
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.helper.DeeplinkHelper.DeeplinkSubscriptionActions.*
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.subscription.ChannelsCountAdapter
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription_freemium.FreemiumSubscriptionActivity
import com.tatasky.binge.ui.features.subscription_freemium.FreemiumSubscriptionActivity.Companion.PAYMENT_ACTIVITY_REQUEST_CODE
import com.tatasky.binge.ui.features.subscription_freemium.adapter.FreemiumProviderAdapter
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.ManagedAppViewModel
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.BindingAdapters.Companion.setHtmlText
import com.tatasky.binge.utils.PaymentUtility.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers
import java.util.*
import javax.inject.Inject

class FreemiumCurrentSubscriptionFragment :
    BaseFragment<FragmentFreemiumCurrentSubscriptionBinding, FreemiumSubscriptionViewModel>() {

    private lateinit var managedAppViewModel: ManagedAppViewModel

    private var currentJourneyRef : String =""
    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics

    override fun getViewModelClass(): Class<FreemiumSubscriptionViewModel> =
        FreemiumSubscriptionViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_freemium_current_subscription

    override fun getViewModelOwner(): ViewModelStoreOwner =
        requireActivity()
    var cardWidth:Int = 0

    private fun getSourceOrFromScreenName() =
        activity?.intent?.extras?.get(KEY_FROM_SCREEN) as String? ?: viewModel.source.takeIf {
            !it.isNullOrBlank()
        }?.also { viewModel.source = null /*After consumption, make it null again*/ }
        ?: SOURCE_HOME

    override fun setObserver() {
        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                if (it) {
                    //refresh Account page for subscription detail
                    setPackInformation()
                }
            }
        })

        viewModel.getCancellationResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
//                viewModel.fetchFreemiumCurrentSubscription()
                showDialog(
                    DialogModel(
                        false,
                        R.drawable.ic_success_tick,
                        it.data?.deactivateMessage?.header ?: "Plan cancellation scheduled",
                        "Done",
                        null,
                        it.data?.deactivateMessage?.footer,
                        subText = it.data?.deactivateMessage?.message
                    ), object :
                        CommonDialogEventListener {
                        override fun onPrimaryButtonClick() {
                            hideDialog()
                        }

                        override fun onSecondaryButtonClick() {
                            hideDialog()
                        }

                        override fun onCloseButtonClick() {
                            hideDialog()
                        }
                    })
            }
        })
        viewModel.getCancellationRevokeResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                viewModel.subscriptionAnalytics.trackSubscriptionRevoke(
                    activity?.intent?.extras?.getString(
                        "fromScreen"
                    ) ?: SOURCE_ACCOUNT
                )
                val toastView =
                    DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                        LayoutInflater.from(context),
                        R.layout.layout_toast_success_failure,
                        null,
                        false
                    )
                toastView.textLoginSuccessfulToast.text =
                    getString(R.string.cancellation_revoked)
                toastView.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
                showCustomToast(
                    context,
                    toastView?.root,
                    Gravity.FILL_HORIZONTAL
                )
            }
        })
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (false == activity?.isTaskRoot) {
                    activity?.finish()
                } else {
                    startHomeScreen(activity)
                }
            }
        })
        managedAppViewModel.getManagedAppResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { managedAppResponse ->
                managedAppResponse.data?.href?.let {
                    findNavController().navigateSafe(
                        FreemiumCurrentSubscriptionFragmentDirections.actionMyPlanOtherOptionsBottomSheetToManagedAppFragment(
                            journeySource =  currentJourneyRef,
                            accessToken = managedAppResponse.data?.accessToken?:"",
                            pageUrl = it
                        )
                    )
                }?:run{onError(ErrorModel())}
            }
        })

    }

    override fun toBeCalledOnce() {

        managedAppViewModel =
            ViewModelProvider(
                requireActivity(),
                viewModelFactory
            )[ManagedAppViewModel::class.java]

        // Start initiating Juspay SDK
//        (activity as? FreemiumSubscriptionActivity)?.fetchPayloadAndInitiateJuspay() //Moved to ManagedAppFragment
//        val layoutManagerExtra = FlexboxLayoutManager(binding.selectedPackCard.recyclerPacks.context)
//        layoutManagerExtra.flexDirection = FlexDirection.ROW
//        layoutManagerExtra.justifyContent = JustifyContent.CENTER
//        layoutManagerExtra.alignItems = AlignItems.CENTER
//        val gridLayoutManagerExtra=
//            GridLayoutManager(binding.selectedPackCard.recyclerPacks.context,5)
//        binding.selectedPackCard.recyclerPacks.layoutManager = gridLayoutManagerExtra
//        val layoutManager = FlexboxLayoutManager(binding.selectedPackCard.recyclerPacks.context)
//        layoutManager.flexDirection = FlexDirection.ROW
//        layoutManager.justifyContent = JustifyContent.CENTER
//        layoutManager.alignItems = AlignItems.CENTER
//        val gridLayoutManager=
//            GridLayoutManager(binding.selectedPackCard.recyclerPacks.context,5)
//        binding.comboSelectedPackCard.recyclerPacks.layoutManager = gridLayoutManager
        viewModel.fetchFreemiumCurrentSubscription()
        binding.ivBack.setOnClickListener {
            findNavController().navigateUpOrOpenHome(activity as AppCompatActivity)
        }
        binding.selectedPackCard.btnRegionalApps.setOnClickListener {
            val actionFromDeeplink = activity?.intent?.getStringExtra(ACTION)
            val journeySourceRefIdFromDeeplink = activity?.intent?.getStringExtra(
                KEY_MANAGED_APP_JOURNEY_SOURCE_REFID
            )
            currentJourneyRef= MYPLAN_REGIONAL
            managedAppViewModel.fetchManagedAppsUrl(
                actionFromDeeplink ?: currentJourneyRef,
                journeySourceRefIdFromDeeplink ?: ""
            )
        }
        binding.selectedPackCard.btnCheckOtherOptions.setOnClickListener {
            if (viewModel.getCurrentSubscription()?.isInactive == true) {
                viewModel.subscriptionAnalytics.trackMyPlanRenewOtherOptions(
                    sharedPrefs.getSubscribedPack()?.productName ?: "",
                    if (sharedPrefs.getSubscribedPack()?.isInactive == true) "0"
                    else getDifferenceBetweenTwoDates(
                        sharedPrefs.getSubscribedPack()?.expirationDate ?: "",
                        getCurrentDateInFormat(SERVER_DATE_TIME_FORMAT),
                        SERVER_DATE_TIME_FORMAT
                    ),
                    viewModel.getCurrentSubscription()
                        ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                        ?: ""
                )

                if (sharedPrefs.isManagedAppEnabled()) {
                    currentJourneyRef = MYPLAN_CHANGE
                    managedAppViewModel.fetchManagedAppsUrl(currentJourneyRef, "")

                } else {
                    findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionToFreemiumSubscriptionFragment())
                }
            } else {
                findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionFragmentToMyPlanOtherOptionsBottomSheet())
            }
        }
        binding.selectedPackCard.btnGetPlan.setOnClickListener {

            if (sharedPrefs.isManagedAppEnabled()) {
                currentJourneyRef = MYPLAN_CHANGE
                managedAppViewModel.fetchManagedAppsUrl(currentJourneyRef, "")

            } else {
                findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionToFreemiumSubscriptionFragment())

            }


        }
        binding.selectedPackCard.btnRevokeCancellation.setOnClickListener {
            /*revoke cancellation api*/
            subscriptionAnalytics.trackMyPlanDontCancelPlan(sharedPrefs.getSubscribedPack()?.productName?:"","BINGE")
            viewModel.requestSubscriptionRevokeCancellation()
        }
        viewModel.checkForManagedAppEligibility { eligible ->
        }

    }

    private fun setPackInformation() {
        val currentPack = viewModel.getCurrentSubscription()

        transparentImageLoad(binding.selectedPackCard.ivDeviceImage,currentPack?.deviceDetails?.iconUrl?:"")


        if (sharedPrefs.isManagedAppEnabled())
            binding.regionalAppInfo = currentPack?.regionalAppInfo


        subscriptionAnalytics.trackViewMyPlanScreen(
            packActive = if (currentPack?.isInactive == false) YES else NO,
            primePackActive = if (currentPack?.primePackDetails?.isActive == true) YES else NO,
            packName = currentPack?.productName ?: "",
            tenureType = currentPack?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType ?: ""
        )

        if(currentPack?.getSelectedComponentAppList?.size ?: 0 !=0) {
            cardWidth = getDisplayMatics().widthPixels
        }
        if(currentPack?.freemiumCombo == true){
            handleComboPack(currentPack)
            return
        } else{
            binding.clComboPlanRoot.hide()
            binding.clNormalPlanRoot.show()
        }

        if(currentPack?.segment?.equals(FIBER_USER) == true){
            binding.tvFiberVerbiage.visibility=View.VISIBLE
            binding.tvFiberVerbiage.text=Html.fromHtml(currentPack.fiberVerbiage)
            binding.tvFiberVerbiage.movementMethod= LinkMovementMethod.getInstance()

        }else{
            binding.tvFiberVerbiage.visibility=View.GONE
        }
//        if (currentPack?.highlightedPack == true) {
//            paintPremiumGradient(
//                binding.selectedPackCard.tvPackName,
//                currentPack?.productName?.let {
//                    binding.selectedPackCard.tvPackName.paint.measureText(it)
//                } ?: 0f
//            )
//            paintPremiumGradient(
//                binding.selectedPackCard.tvPackPrice,
//                currentPack?.amount?.let {
//                    binding.selectedPackCard.tvPackName.paint.measureText(it)
//                } ?: 0f
//            )

//      }


        if (currentPack == null) {

            if(sharedPrefs.isManagedAppEnabled()) {
                currentJourneyRef=MYPLAN_CHANGE
                managedAppViewModel.fetchManagedAppsUrl(currentJourneyRef,"")
            }else{
                findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionToFreemiumSubscriptionFragment())
            }

                    } else {

            if (currentPack.isInactive) {
                subscriptionExpiredMode(currentPack)
            } else if (currentPack.isCancelled) {
                subscriptionCancelledMode(currentPack)
            } else {
                subscriptionActiveMode(currentPack)
            }
            currentPack.primePackDetails?.let {
                handlePrimeSubscription(currentPack)
            }


            binding.tvMsg.text = currentPack.verbiage?.footerVerbiage?.message
            binding.btnCancelSubscription.setOnClickListener {
                viewModel.subscriptionAnalytics.trackMyPlanCancelPlan(
                    currentPack.productName?:"",
                    if (sharedPrefs.getSubscribedPack()?.isInactive == true) "0"
                    else getDifferenceBetweenTwoDates(
                        sharedPrefs.getSubscribedPack()?.expirationDate ?: "",
                        getCurrentDateInFormat(SERVER_DATE_TIME_FORMAT),
                        SERVER_DATE_TIME_FORMAT
                    )
                )
                beginCancellationProcess()
            }

            //Deeplink/Onelink handling for redirecting user to specific screen on my subscription
            activity?.intent?.getStringExtra(ACTION)?.let {
                when {
                    it.equals(ACTION_RENEW.action, true) -> {
                        if (currentPack.planCTADetails?.renewPlanOption == true) {
                            viewModel.setProgressing(true)
                            Handler(Looper.getMainLooper()).postDelayed({
                                viewModel.setProgressing(false)
                                binding.selectedPackCard.btnRenew.performClick()
                            }, 5000L) //Added delay to get everything updated on UI and let Juspay initiate properly
                        }
                    }
                }
            }.also {
                /*Remove extra from intent to avoid again redirection*/
                activity?.intent?.removeExtra(ACTION)
                activity?.intent?.removeExtra(KEY_PACK_NAME)
            }
        }
        if (currentPack?.planCTADetails?.renewPlanOption == true)
            (activity as? FreemiumSubscriptionActivity)?.fetchPayloadAndInitiateJuspay(false)
    }

    private fun handleComboPack(currentPack: PartnerPacks) {
        binding.selectedPack = currentPack
        binding.clNormalPlanRoot.hide()
        binding.clComboPlanRoot.show()
        binding.comboSelectedPackCard.recyclerPacks.adapter =
            FreemiumProviderAdapter(currentPack.getSelectedComponentAppList,cardWidth)

        currentPack.comboPackChannelsInfo?.channelsBreakdownInfo?.let {
            binding.comboSelectedPackCard.recyclerChannels.adapter = ChannelsCountAdapter(it)
        }

        currentPack.primePackDetails?.let {
            handlePrimeSubscription(currentPack)
        }

        val comboConfig: Configuration = binding.clComboPlanRoot.context.resources.configuration
        if(comboConfig.smallestScreenWidthDp<=370){
            binding.comboSelectedPackCard.tvComboAppCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F)
            binding.comboSelectedPackCard.tvComboCountVerbiage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F)
            binding.comboSelectedPackCard.tvComboDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F)

        }

    }

    private fun subscriptionCancelledMode(currentPack: PartnerPacks) {
        binding.selectedPack = currentPack
        binding.selectedPackCard.recyclerPacks.adapter =
            FreemiumProviderAdapter(currentPack.getSelectedComponentAppList, cardWidth)
        binding.selectedPackCard.btnRenew.setOnClickListener {
            viewModel.subscriptionAnalytics.trackMyPlanRenewPlan(
                sharedPrefs?.getSubscribedPack()?.productName?:"",
                if (!currentPack.isInactive) YES else NO,
                if (sharedPrefs.getSubscribedPack()?.isInactive == true) "0"
                else getDifferenceBetweenTwoDates(
                    sharedPrefs.getSubscribedPack()?.expirationDate ?: "",
                    getCurrentDateInFormat(SERVER_DATE_TIME_FORMAT),
                    SERVER_DATE_TIME_FORMAT
                ),
                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                    ?: ""
            )
            currentPack.tenure?.find{
                it.currentTenure == true
            }?.let {

                activity?.startActivityForResult(
                    getPaymentActivityIntent(
                        requireContext(),
                        packId = currentPack.productId!!,
                        selectedTenureId = it.tenureId!!,
                        selectedTenureAmount = it.offeredPriceValue,
                        isMigrated = currentPack.migrated!!,
                        migratedVerbiage = currentPack.migratedVerbiage?:"",
                         "",
                        fromScreen = getSourceOrFromScreenName(),
                        sharedPrefs = sharedPrefs
                    ).apply {
                        putExtra(KEY_IS_RENEW, true)
                        putExtra(KEY_PACK_PRICE, currentPack.amountValue)
                        putExtra(KEY_APPSFLYER_SOURCE, getSourceOrFromScreenName())
                        putExtra(KEY_PACK_NAME, currentPack.productName)
                        putExtra(
                            KEY_SELECTED_TENURE_PACK_PRICE,
                            /*Renew case*/
                            currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.offeredPriceValue
                        )
                        putExtra(
                            KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX,
                            /*Renew case*/
                            currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureDurationInDaysWithDSuffix
                        )
                        putExtra(
                            KEY_SELECTED_TENURE_TYPE,
                            /*Renew case*/
                            currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                        )
                        putExtra(
                            KEY_IS_FIRST_SUBSCRIPTION,
                            false
                        )
                    }, PAYMENT_ACTIVITY_REQUEST_CODE
                )

            }?:run{

                activity?.startActivityForResult(
                    getPaymentActivityIntent(
                        requireContext(),
                        packId = currentPack.productId!!,
                        selectedTenureId = currentPack.productId!!, //if no current tenure found then send the current pack product id
                        selectedTenureAmount = currentPack.amountValue ,
                        isMigrated = currentPack.migrated!!,
                        migratedVerbiage = currentPack.migratedVerbiage?:"",
                        "",
                        fromScreen = getSourceOrFromScreenName(),
                        sharedPrefs = sharedPrefs
                    ).apply {
                        putExtra(KEY_IS_RENEW, true)
                        putExtra(KEY_PACK_PRICE, currentPack.amountValue)
                        putExtra(KEY_APPSFLYER_SOURCE, getSourceOrFromScreenName())
                        putExtra(KEY_PACK_NAME, currentPack.productName)
                        putExtra(
                            KEY_SELECTED_TENURE_PACK_PRICE,
                            /*Renew case*/
                            currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.offeredPriceValue
                        )
                        putExtra(
                            KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX,
                            /*Renew case*/
                            currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureDurationInDaysWithDSuffix
                        )
                        putExtra(
                            KEY_SELECTED_TENURE_TYPE,
                            /*Renew case*/
                            currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                        )
                        putExtra(
                            KEY_IS_FIRST_SUBSCRIPTION,
                            false
                        )
                    }, PAYMENT_ACTIVITY_REQUEST_CODE
                )
        }}

        binding.btnCancelSubscription.hide()
        binding.selectedPackCard.tvRenewalCycle.setText(
            currentPack.packValidity,
            TextView.BufferType.SPANNABLE
        )


        if (currentPack.planCTADetails?.renewPlanOption == true) {
            binding.selectedPackCard.btnRenew.show()
            binding.selectedPackCard.btnCheckOtherOptions.show()
        } else {
            binding.selectedPackCard.btnRenew.hide()
            binding.selectedPackCard.btnCheckOtherOptions.hide()
        }
        if (currentPack.planCTADetails?.let { it.changePlanOption && !it.renewPlanOption } == true) {
            binding.cardChangePlan.show()
        } else {
            binding.cardChangePlan.hide()
        }
        if (currentPack.planCTADetails?.let { it.changeTenureOption && !it.renewPlanOption } == true) {
            binding.cardChangeTenure.show()
        } else {
            binding.cardChangeTenure.hide()
        }
        binding.cardChangePlan.setOnClickListener {
            viewModel.subscriptionAnalytics.trackMyPlanChangePlan(
                currentPack.productName ?: "",
                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                    ?: ""
            )
            if(sharedPrefs.isManagedAppEnabled()) {
                currentJourneyRef=MYPLAN_CHANGE
                managedAppViewModel.fetchManagedAppsUrl(currentJourneyRef,"")
            }else{
                findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionToFreemiumSubscriptionFragment())
            }


        }
        binding.cardChangeTenure.setOnClickListener {
            viewModel.subscriptionAnalytics.trackMyPlanChangetenure(
                currentPack.productName ?: "",
                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                    ?: ""
            )
            if(sharedPrefs.isManagedAppEnabled()){
                currentJourneyRef= MYPLAN_TENURE
                managedAppViewModel.fetchManagedAppsUrl(currentJourneyRef,"")

            }else{
                findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionFragmentToTenureBottomSheetDialog(currentPack))
            }

        }
        if (viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if (!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()) {
            binding.selectedPackCard.tvEligibility.text =
                currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCard.tvEligibility.show()
        } else if (viewModel.getSubscriptionType().equals(
                subscriptionTypeFtv,
                true
            ) || (currentPack.eligibleFirestick && currentPack.fsTaken)
        ) {
            binding.selectedPackCard.tvEligibility.show()
        } else if (currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCard.tvEligibility.show()
        } else {
            binding.selectedPackCard.tvEligibility.hide()
        }
    }

    private fun handlePrimeSubscription(currentPack: PartnerPacks) {
        binding.amazonPacks.lifecycleOwner = viewLifecycleOwner
        if (true == currentPack.primePackDetails?.isActive || true == currentPack.primePackDetails?.isSuspended) {
            binding.primeVisible = true
            val primePack = currentPack.primePackDetails!!
            if (primePack.imageUrl.isNullOrBlank())
                updateProviderSeeAll(
                    binding.amazonPacks.packIcon,
                    PROVIDER_PRIME,
                    sharedPrefs.getProviderLogo(),
                    sharedPrefs.getCloudenieryUrl()
                )
            else
                transparentImageLoad(binding.amazonPacks.packIcon, primePack.squareImageUrl ?: "")
//            var color = ContextCompat.getColor(requireContext(), R.color.pink_50)
//            binding.amazonPacks.tvExpiryMsg.setTextColor(color)
            binding.amazonPacks.tvPackName.text = primePack.title
            binding.amazonPacks.tvExpiryMsg.text = primePack.expiryDateToDisplay
            binding.amazonPacks.tvPrice.text=String.format(
                getString(R.string.amount_with_rupee),
                "${primePack.getFormattedPrice()}")
            binding.amazonPacks.tvDuration.text="/${primePack.renewalCycle}"

            binding.amazonPacks.tvPackDisclaimer.show()
            if(primePack.let {
                (it.isActive || it.isSuspended) && it.platform.equals(
                    "mobile",
                    true
                ) && !it.primeCancellationRaised
            }) {
                binding.amazonPacks.divider.show()
                binding.amazonPacks.btnCancelPrime.show()
                binding.amazonPacks.btnCancelPrime.setOnClickListener{
                    showOnlyPrimeCancellationDialog()
                }
            }
        } else {
            binding.primeVisible = false
        }
    }

    private fun subscriptionActiveMode(currentPack: PartnerPacks) {
        binding.selectedPack = currentPack
        binding.selectedPackCard.recyclerPacks.adapter =
            FreemiumProviderAdapter(currentPack.getSelectedComponentAppList, cardWidth)
        binding.selectedPackCard.btnRenew.setOnClickListener {
            viewModel.subscriptionAnalytics.trackMyPlanRenewPlan(
                sharedPrefs?.getSubscribedPack()?.productName?:"",
                if (!currentPack.isInactive)YES else NO,
                if (sharedPrefs.getSubscribedPack()?.isInactive == true) "0"
                else getDifferenceBetweenTwoDates(
                    sharedPrefs.getSubscribedPack()?.expirationDate ?: "",
                    getCurrentDateInFormat(SERVER_DATE_TIME_FORMAT),
                    SERVER_DATE_TIME_FORMAT
                ),
                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                    ?: ""
            )
            if(currentPack.migrated){
                onError(ErrorModel(message = currentPack.migratedVerbiage))
            } else {
                currentPack.tenure?.find{
                    it.currentTenure == true
                }?.let {

                    activity?.startActivityForResult(
                        getPaymentActivityIntent(
                            requireContext(),
                            packId = currentPack.productId!!,
                            selectedTenureId = it.tenureId!!,  //if no current tenure found then send the current pack product id
                            selectedTenureAmount = it.offeredPriceValue ,
                            isMigrated = currentPack.migrated!!,
                            migratedVerbiage = currentPack.migratedVerbiage?:"",
                             "",
                            fromScreen = getSourceOrFromScreenName(),
                            sharedPrefs = sharedPrefs
                        ).apply {
                            putExtra(KEY_IS_RENEW, true)
                            putExtra(KEY_PACK_PRICE, currentPack.amountValue)
                            putExtra(KEY_APPSFLYER_SOURCE, getSourceOrFromScreenName())
                            putExtra(KEY_PACK_NAME, currentPack.productName)
                            putExtra(
                                KEY_SELECTED_TENURE_PACK_PRICE,
                                /*Renew case*/
                                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.offeredPriceValue
                            )
                            putExtra(
                                KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX,
                                /*Renew case*/
                                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureDurationInDaysWithDSuffix
                            )
                            putExtra(
                                KEY_SELECTED_TENURE_TYPE,
                                /*Renew case*/
                                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                            )
                            putExtra(
                                KEY_IS_FIRST_SUBSCRIPTION,
                                false
                            )
                        }, PAYMENT_ACTIVITY_REQUEST_CODE
                    )

                }?:run{
                    activity?.startActivityForResult(
                        getPaymentActivityIntent(
                            requireContext(),
                            packId = currentPack.productId!!,
                            selectedTenureId = currentPack.productId!!,  //if no current tenure found then send the current pack product id
                            selectedTenureAmount = currentPack.amountValue ,
                            isMigrated = currentPack.migrated!!,
                            migratedVerbiage = currentPack.migratedVerbiage?:"",
                             "",
                            fromScreen = getSourceOrFromScreenName(),
                            sharedPrefs = sharedPrefs
                        ).apply {
                            putExtra(KEY_IS_RENEW, true)
                            putExtra(KEY_PACK_PRICE, currentPack.amountValue)
                            putExtra(KEY_APPSFLYER_SOURCE, getSourceOrFromScreenName())
                            putExtra(KEY_PACK_NAME, currentPack.productName)
                            putExtra(
                                KEY_SELECTED_TENURE_PACK_PRICE,
                                /*Renew case*/
                                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.offeredPriceValue
                            )
                            putExtra(
                                KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX,
                                /*Renew case*/
                                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureDurationInDaysWithDSuffix
                            )
                            putExtra(
                                KEY_SELECTED_TENURE_TYPE,
                                /*Renew case*/
                                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                            )
                            putExtra(
                                KEY_IS_FIRST_SUBSCRIPTION,
                                false
                            )
                        }, PAYMENT_ACTIVITY_REQUEST_CODE
                    )
                }
            }
        }
        if (currentPack.planCTADetails?.cancellationOptions?.cancelPlanOption == true) {
            binding.btnCancelSubscription.show()
        } else {
            binding.btnCancelSubscription.hide()
        }
        if (currentPack.planCTADetails?.let { it.changePlanOption && !it.renewPlanOption } == true) {
            binding.cardChangePlan.show()
        } else {
            binding.cardChangePlan.hide()
        }
        if (currentPack.planCTADetails?.let { it.changeTenureOption && !it.renewPlanOption } == true) {
            binding.cardChangeTenure.show()
        } else {
            binding.cardChangeTenure.hide()
        }
        if (currentPack.planCTADetails?.renewPlanOption == true) {
            binding.selectedPackCard.btnRenew.show()
            binding.selectedPackCard.btnCheckOtherOptions.show()
        } else {
            binding.selectedPackCard.btnRenew.hide()
            binding.selectedPackCard.btnCheckOtherOptions.hide()
        }
        binding.cardChangePlan.setOnClickListener {
            viewModel.subscriptionAnalytics.trackMyPlanChangePlan(
                currentPack.productName ?: "",
                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                    ?: ""
            )
            if(sharedPrefs.isManagedAppEnabled()) {
                currentJourneyRef = MYPLAN_CHANGE
                managedAppViewModel.fetchManagedAppsUrl(currentJourneyRef, "")
                //Todo confirm with shubham
            }else{
                findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionToFreemiumSubscriptionFragment())

            }
        }
        binding.cardChangeTenure.setOnClickListener {
            viewModel.subscriptionAnalytics.trackMyPlanChangetenure(
                currentPack.productName ?: "",
                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                    ?: ""
            )

            if(sharedPrefs.isManagedAppEnabled()){
                currentJourneyRef= MYPLAN_TENURE
                managedAppViewModel.fetchManagedAppsUrl(currentJourneyRef,"")

            }else{
                findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionFragmentToTenureBottomSheetDialog(currentPack,"PackListing"))
            }


        }
        binding.selectedPackCard.tvRenewalCycle.setText(
            currentPack.packValidity,
            TextView.BufferType.SPANNABLE
        )

        if (viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if (!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()) {
            binding.selectedPackCard.tvEligibility.text =
                currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCard.tvEligibility.show()
        } else if (viewModel.getSubscriptionType().equals(
                subscriptionTypeFtv,
                true
            ) || (currentPack.eligibleFirestick && currentPack.fsTaken)
        ) {
            binding.selectedPackCard.tvEligibility.show()
        } else if (currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCard.tvEligibility.show()
        } else {
            binding.selectedPackCard.tvEligibility.hide()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            if(binding.selectedPackCard.btnRevokeCancellation.visibility == View.VISIBLE) {
                binding.selectedPackCard.tvExpiryReminder.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.darkError
                    )
                )
            }
        },100)

        val config : Configuration = binding.clNormalPlanRoot.context.resources.configuration
        if(config.smallestScreenWidthDp <=370){
            binding.selectedPackCard.tvTotalApps.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19F)
            binding.selectedPackCard.tvApps.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F)
            binding.selectedPackCard.tvPlatform.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F)
            binding.selectedPackCard.tvRenewalCycle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10F)
        }
    }

    private fun subscriptionExpiredMode(currentPack: PartnerPacks) {
        binding.selectedPack = currentPack
        binding.selectedPackCard.recyclerPacks.adapter =
            FreemiumProviderAdapter(currentPack.getSelectedComponentAppList, cardWidth)
        binding.selectedPackCard.btnRenew.setOnClickListener {
            viewModel.subscriptionAnalytics.trackMyPlanRenewPlan(
                sharedPrefs?.getSubscribedPack()?.productName?:"",
                if (!currentPack.isInactive) YES else NO,
                if (sharedPrefs.getSubscribedPack()?.isInactive == true) "0"
                else getDifferenceBetweenTwoDates(
                    sharedPrefs.getSubscribedPack()?.expirationDate ?: "",
                    getCurrentDateInFormat(SERVER_DATE_TIME_FORMAT),
                    SERVER_DATE_TIME_FORMAT
                ),
                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                    ?: ""
            )
            if(currentPack.migrated){
                onError(ErrorModel(message = currentPack.migratedVerbiage))
            } else {
                currentPack.tenure?.find{
                    it.currentTenure == true
                }?.let {

                    activity?.startActivityForResult(
                        getPaymentActivityIntent(
                            requireContext(),
                            packId = currentPack.productId!!,
                            selectedTenureId = it.tenureId!!,  //if no current tenure found then send the current pack product id
                            selectedTenureAmount = it.offeredPriceValue ,
                            isMigrated = currentPack.migrated!!,
                            migratedVerbiage = currentPack.migratedVerbiage?:"",
                             "",
                            fromScreen = getSourceOrFromScreenName(),
                            sharedPrefs = sharedPrefs
                        ).apply {
                            putExtra(KEY_IS_RENEW, true)
                            putExtra(KEY_PACK_PRICE, currentPack.amountValue)
                            putExtra(KEY_APPSFLYER_SOURCE, getSourceOrFromScreenName())
                            putExtra(KEY_PACK_NAME, currentPack.productName)
                            putExtra(
                                KEY_SELECTED_TENURE_PACK_PRICE,
                                /*Renew case*/
                                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.offeredPriceValue
                            )
                            putExtra(
                                KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX,
                                /*Renew case*/
                                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureDurationInDaysWithDSuffix
                            )
                            putExtra(
                                KEY_SELECTED_TENURE_TYPE,
                                /*Renew case*/
                                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                            )
                            putExtra(
                                KEY_IS_FIRST_SUBSCRIPTION,
                                false
                            )
                        }, PAYMENT_ACTIVITY_REQUEST_CODE
                    )

                }?:run{

                    activity?.startActivityForResult(
                        getPaymentActivityIntent(
                            requireContext(),
                            packId = currentPack.productId!!,
                            selectedTenureId = currentPack.productId!!,  //if no current tenure found then send the current pack product id
                            selectedTenureAmount = currentPack.amountValue ,
                            isMigrated = currentPack.migrated!!,
                            migratedVerbiage = currentPack.migratedVerbiage?:"",
                             "",
                            fromScreen = getSourceOrFromScreenName(),
                            sharedPrefs = sharedPrefs
                        ).apply {
                            putExtra(KEY_IS_RENEW, true)
                            putExtra(KEY_PACK_PRICE, currentPack.amountValue)
                            putExtra(KEY_APPSFLYER_SOURCE, getSourceOrFromScreenName())
                            putExtra(KEY_PACK_NAME, currentPack.productName)
                            putExtra(
                                KEY_SELECTED_TENURE_PACK_PRICE,
                                /*Renew case*/
                                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.offeredPriceValue
                            )
                            putExtra(
                                KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX,
                                /*Renew case*/
                                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureDurationInDaysWithDSuffix
                            )
                            putExtra(
                                KEY_SELECTED_TENURE_TYPE,
                                /*Renew case*/
                                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                            )
                            putExtra(
                                KEY_IS_FIRST_SUBSCRIPTION,
                                false
                            )
                        }, PAYMENT_ACTIVITY_REQUEST_CODE
                    )

                }
            }
        }
        binding.selectedPackCard.cardView.strokeColor =
            ResourcesCompat.getColor(resources, R.color.darkError, null)
        binding.selectedPackCard.cardView.strokeWidth = 2
        if (currentPack.planCTADetails?.cancellationOptions?.cancelPlanOption == true) {
            binding.btnCancelSubscription.show()
        } else {
            binding.btnCancelSubscription.hide()
        }
        if (currentPack.planCTADetails?.let { it.changePlanOption && !it.renewPlanOption } == true) {
            binding.cardChangePlan.show()
        } else {
            binding.cardChangePlan.hide()
        }
        if (currentPack.planCTADetails?.let { it.changeTenureOption && !it.renewPlanOption } == true) {
            binding.cardChangeTenure.show()
        } else {
            binding.cardChangeTenure.hide()
        }
        binding.cardChangePlan.setOnClickListener {
            viewModel.subscriptionAnalytics.trackMyPlanChangePlan(
                currentPack.productName ?: "",
                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                    ?: ""
            )
            if(sharedPrefs.isManagedAppEnabled()){
                currentJourneyRef=MYPLAN_CHANGE
                managedAppViewModel.fetchManagedAppsUrl(currentJourneyRef,"")
            }else{
                findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionToFreemiumSubscriptionFragment())         }
            }

        binding.cardChangeTenure.setOnClickListener {
            viewModel.subscriptionAnalytics.trackMyPlanChangetenure(
                currentPack.productName ?: "",
                currentPack.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                    ?: ""
            )
            if(sharedPrefs.isManagedAppEnabled()) {
                currentJourneyRef = MYPLAN_TENURE
                managedAppViewModel.fetchManagedAppsUrl(currentJourneyRef, "")
            }else{
                findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionFragmentToTenureBottomSheetDialog(currentPack,"PackListing"))
            }
        }
        if (currentPack.planCTADetails?.renewPlanOption == true) {
            binding.selectedPackCard.btnRenew.show()
            binding.selectedPackCard.btnCheckOtherOptions.show()
        } else {
            binding.selectedPackCard.btnRenew.hide()
            binding.selectedPackCard.btnCheckOtherOptions.hide()
        }
        binding.selectedPackCard.tvRenewalCycle.setText(
            currentPack.packValidity,
            TextView.BufferType.SPANNABLE
        )

        if (viewModel.getSubscriptionType().equals(subscriptionTypeAtv, true)) {
            binding.selectedPackCard.tvEligibility.text = getString(R.string.atv_availed_msg)
            binding.selectedPackCard.tvEligibility.show()
        } else if (!currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()) {
            binding.selectedPackCard.tvEligibility.text =
                currentPack.subscriptionDetailInfo?.complementaryPlanLinkedMessage
            binding.selectedPackCard.tvEligibility.show()
        } else if (viewModel.getSubscriptionType().equals(subscriptionTypeFtv, true)) {
            binding.selectedPackCard.tvEligibility.show()
        } else if (currentPack.eligibleFirestick && !currentPack.fsTaken) {
            binding.selectedPackCard.tvEligibility.show()
        } else {
            binding.selectedPackCard.tvEligibility.hide()
        }
    }

    protected fun beginCancellationProcess() {
        if (true == viewModel.getCurrentSubscription()?.primePackDetails?.let {
                (it.isActive || it.isSuspended) && it.platform.equals(
                    "mobile",
                    true
                ) && !it.primeCancellationRaised
            }) {
                //with prime
            showCancellationDialog()
        } else {
            //only binge
            showOnlyBingeCancellationDialog()
        }
    }

    private fun showOnlyBingeCancellationDialog() {
      findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionFragmentToCancellationConfirmationBottomSheet(false))
    }

    private fun showOnlyPrimeCancellationDialog() {
        findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionFragmentToCancellationConfirmationBottomSheet(true))
    }

    private fun showCancellationDialog() {
        findNavController().navigateSafe(FreemiumCurrentSubscriptionFragmentDirections.actionFreemiumCurrentSubscriptionFragmentToAddOnCancellationBottomSheet())
    }

    protected fun handleModifyBlockedDialog(accountSubStatus: String?, msg: String) {
        val dthStatus = viewModel.getDthStatus()
        when (dthStatus.toLowerCase(Locale.getDefault())) {
            AccountStatusEnum.DEACTIVATED.status.toLowerCase(Locale.getDefault()) -> {
                showDialog(
                    DialogModel(
                        false,
                        R.drawable.ic_subscription_error,
                        getString(R.string.alert),
                        getString(R.string.recharge),
                        getString(R.string.skip),
                        msg
                    ), object :
                        CommonDialogEventListener {
                        override fun onPrimaryButtonClick() {
                            viewModel.startRecharge(getSourceOrFromScreenName())
                            hideDialog()
                        }

                        override fun onSecondaryButtonClick() {
                            hideDialog()
                        }

                        override fun onCloseButtonClick() {
                        }
                    })
            }
            AccountStatusEnum.TEMP_SUSPENSION.status.toLowerCase(Locale.getDefault()) -> {
                showDialog(
                    DialogModel(
                        false,
                        R.drawable.ic_subscription_error,
                        getString(R.string.alert),
                        getString(R.string.ok),
                        null,
                        msg
                    ), object :
                        CommonDialogEventListener {
                        override fun onPrimaryButtonClick() {
                            hideDialog()
                        }

                        override fun onSecondaryButtonClick() {
                            hideDialog()
                        }

                        override fun onCloseButtonClick() {
                        }
                    })
            }
            AccountStatusEnum.ACTIVE.status.toLowerCase(Locale.getDefault()) -> {
                if (accountSubStatus.equals(
                        AccountStatusEnum.PARTIALLY_DUNNED.status.toLowerCase(
                            Locale.getDefault()
                        ), true
                    )
                ) {
                    showDialog(
                        DialogModel(
                            false,
                            R.drawable.ic_subscription_error,
                            getString(R.string.alert),
                            getString(R.string.recharge),
                            getString(R.string.skip),
                            msg
                        ), object :
                            CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                viewModel.startRecharge(getSourceOrFromScreenName())
                                hideDialog()
                            }

                            override fun onSecondaryButtonClick() {
                                hideDialog()
                            }

                            override fun onCloseButtonClick() {
                            }
                        })
                } else {
                    onError(ErrorModel())
                }
            }
            else -> {
                onError(ErrorModel())
            }
        }
    }

    protected fun beginRevokeProcess() {
        showDialog(
            DialogModel(
                false,
                R.drawable.ic_my_subscription,
                viewModel.getCurrentSubscription()?.verbiage?.revokeSubs?.title
                    ?: getString(R.string.resume_subscription_title),
                "Yes",
                getString(R.string.cancel)
            ), object : CommonDialogEventListener {
                override fun onPrimaryButtonClick() {
                    viewModel.requestSubscriptionRevokeCancellation()
                    hideDialog()
                }

                override fun onSecondaryButtonClick() {
                    hideDialog()
                    viewModel.subscriptionAnalytics.trackSubscriptionRevokeSkip()
                }

                override fun onCloseButtonClick() {
                    hideDialog()
                }
            })
    }

}