package com.tatasky.binge.ui.features.subscription_freemium

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.tatasky.binge.R
import com.tatasky.binge.analytics.DRAWER_CYOP
import com.tatasky.binge.analytics.KEY_IS_FIRST_SUBSCRIPTION
import com.tatasky.binge.analytics.SOURCE_HOME
import com.tatasky.binge.databinding.LayoutTsWalletBalanceBinding
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.PaymentUtility.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers
import kotlinx.android.synthetic.main.activity_freemium_subscription.*
import java.util.*
import javax.inject.Inject
private const val TAG = "FreemiumSubscriptionActivity"

class FreemiumSubscriptionActivity : BaseActivity<CommonSampleViewModel>() {

    @Inject
    lateinit var localBroadcastHelper: LocalBroadcastHelper

    private lateinit var mSubscriptionViewModel: FreemiumSubscriptionViewModel
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun allowedTouchWhenLoading(): Boolean = false

    private fun getSourceOrFromScreenName() =
        intent?.extras?.get(KEY_FROM_SCREEN) as String? ?: viewModel.source.takeIf {
            !it.isNullOrBlank()
        }?.also { viewModel.source = null /*After consumption, make it null again*/ }
        ?: SOURCE_HOME

    /*Sometimes initiate gets failed so calling it from multiple fragments
    * So that if Juspay wasn't initiated previously it gets initiated
    */
    override fun fetchPayloadAndInitiateJuspay(showLoader: Boolean) {
        if (hyperInstance?.isInitialised == false)
            mSubscriptionViewModel.fetchJuspayInitiatePayload(
                HashMap<String, String>().apply {
                    put(
                        PaymentGatewayConstants.KEY_CUSTOMER_ID,
                        mSubscriptionViewModel.sharedPref.getOriginalSubscriberId()
                    )
                    put(
                        PaymentGatewayConstants.KEY_CUSTOMER_EMAIL,
                        mSubscriptionViewModel.sharedPref.getSelectedProfile()?.emailId ?: ""
                    )
                    put("rmn", mSubscriptionViewModel.sharedPref.getClearRMN() ?: "")
                },
                showLoader
            )
    }

    override fun bindPayByBalanceView(parent: ViewGroup?): View? {
        payByBalanceBinding = LayoutTsWalletBalanceBinding.inflate(layoutInflater, parent, false)
        val addOrModifyPackResponse = sharedPrefs.getAddModifyResponse()
        var hasLowBalance = false
        if (viewModel.sharedPrefs.isManagedAppEnabled()) {
            addOrModifyPackResponse?.data?.payByDthWalletData?.let { balanceResponse ->
                payByBalanceBinding?.balance =
                    balanceResponse.balance
                payByBalanceBinding?.walletPaymentVerbiage =
                    balanceResponse.walletPaymentVerbiage
                payByBalanceBinding?.hasLowBalanceForThisTxn =
                    balanceResponse.hasLowBalanceForThisTxn
                hasLowBalance = balanceResponse.hasLowBalanceForThisTxn == true
            }
        } else {
            addOrModifyPackResponse?.data?.let {
                mSubscriptionViewModel.fetchBalance(
                    it.amount ?: sharedPrefs.getSubscribedPack()?.amountValue,
                    it.productId
                ) { walletBalanceResponse ->
                    walletBalanceResponse?.data?.let { balanceResponse ->
                        payByBalanceBinding?.balance =
                            balanceResponse.balanceQueryRespDTO?.balance
                        payByBalanceBinding?.walletPaymentVerbiage =
                            balanceResponse.walletPaymentVerbiage
                        payByBalanceBinding?.hasLowBalanceForThisTxn =
                            balanceResponse.hasLowBalanceForThisTxn
                        hasLowBalance = balanceResponse.hasLowBalanceForThisTxn == true
                    }
                }
            }
        }
        payByBalanceBinding?.payByTSBalCB?.setOnCheckedChangeListener { _, isChecked ->
            payByBalanceBinding?.viewShouldShow = isChecked
        }
        payByBalanceBinding?.subIDTV?.text =
            String.format(getString(R.string.subscriber_id_template),
                mSubscriptionViewModel.sharedPref.getOriginalSubscriberId()
            )
        payByBalanceBinding?.makePaymentBtn?.setOnClickListener {
            /**
             * Using broadcast receiver to send click event to another acitvity
             * As if any API gets called from this activity then we will be unable to observe the
             * response/error when the activity is not visible. So sending click event to another
             * activity using Broadcast
             */
            localBroadcastHelper.sendBroadcast(
                this,
                Intent(localBroadcastHelper.ACTION_MAKE_PAYMENT).apply {
                    Bundle().putBoolean(
                        KEY_HAS_LOW_BALANCE_FOR_THIS_TXN,
                        hasLowBalance
                    )
                }
            )
        }
        return payByBalanceBinding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        terminateJuspayService(this)
    }

    private fun setObserver() {
        mSubscriptionViewModel.getJuspayInitiatePayload().observe(this) {
            it.getContentIfNotHandled()?.let { juspayInitiatePayload ->
                initiateJusPay(
                    this,
                    juspayInitiatePayload,
                    mSubscriptionViewModel.sharedPref.getDthStatusFreemium()
                )
            }
        }
    }

    override fun getContentViewId(): Int = R.layout.activity_freemium_subscription

    override fun getRootLayoutContainer(): View = splash_container

    override fun getViewModelClass(): Class<CommonSampleViewModel> =
        CommonSampleViewModel::class.java

    override fun init(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)
        toBeCalledOnce()
//        WebView.setWebContentsDebuggingEnabled(true) // TODO: Make sure to remove this code to prevent sensitive logs printing
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.nav_freemium_subscription)
        val setOfTopLevelDestinations = hashSetOf<Int>()
        val currentPack = sharedPrefs.getSubscribedPack()

        if (currentPack == null) {
            checkRedirectionToSubscriptionSource(navGraph, setOfTopLevelDestinations, navController)
        }
        else if(currentPack.fdoRequested == true){
            navGraph.startDestination = R.id.freemiumCurrentSubscriptionFragment
            setOfTopLevelDestinations.add(R.id.freemiumCurrentSubscriptionFragment)
            navController.setGraph(navGraph, intent.extras)
        } else if (intent.extras?.getBoolean("startPackListing") == false){
            navGraph.startDestination = R.id.freemiumCurrentSubscriptionFragment
            setOfTopLevelDestinations.add(R.id.freemiumCurrentSubscriptionFragment)
            navController.setGraph(navGraph, intent.extras)
        }
        else if (intent.extras?.getBoolean("startPackListing") == true) {

            checkRedirectionToSubscriptionSource(navGraph, setOfTopLevelDestinations, navController)
        }
        else if (intent.extras?.getBoolean("startComparePlan") == true){
            navGraph.startDestination = R.id.comparePlanFragment
            setOfTopLevelDestinations.add(R.id.comparePlanFragment)
            navController.setGraph(navGraph, intent.extras)
        }
        else if (currentPack.isInactive && currentPack.freeTrialStatus == true){
            checkRedirectionToSubscriptionSource(navGraph, setOfTopLevelDestinations, navController)
        }else {
            if (currentPack == null) {
                checkRedirectionToSubscriptionSource(
                    navGraph,
                    setOfTopLevelDestinations,
                    navController
                )
            } else {
                navGraph.startDestination = R.id.freemiumCurrentSubscriptionFragment
                setOfTopLevelDestinations.add(R.id.freemiumCurrentSubscriptionFragment)
                navController.setGraph(navGraph, intent.extras)
            }
        }

    }
    private fun checkRedirectionToSubscriptionSource(
        navGraph: NavGraph,
        setOfTopLevelDestinations: HashSet<Int>,
        navController: NavController
    ) {
        viewModel.checkForManagedAppEligibility { eligible ->
            if (eligible) {
                intent.putExtra("journeySource", DRAWER_CYOP)
                currentJourneyRef = DRAWER_CYOP
                navGraph.startDestination = R.id.managedAppFragment
                setOfTopLevelDestinations.add(R.id.managedAppFragment)
            } else {
                navGraph.startDestination = R.id.freemiumSubscriptionFragment
                setOfTopLevelDestinations.add(R.id.freemiumSubscriptionFragment)
            }
            navController.setGraph(navGraph, intent.extras)
        }
    }


    private fun toBeCalledOnce() {
        mSubscriptionViewModel = ViewModelProvider(
            this,
            mViewModelFactory
        )[FreemiumSubscriptionViewModel::class.java]
        setObserver()
        setupPaymentSDK(
            viewModel.sharedPrefs.getConfigResponse()?.data?.config?.paymentGatewayInfo?.paymentClientId
                ?: "",
            viewModel.sharedPrefs.getConfigResponse()?.data?.config?.paymentGatewayInfo?.paymentServiceId
                ?: "",
            viewModel.sharedPrefs.getConfigResponse()?.data?.config?.paymentGatewayInfo?.paymentBetaAssets
                ?: false
        )

    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(Activity.RESULT_CANCELED)
        return findNavController(R.id.fragment_container).navigateUpOrOpenHome(this)
    }

    override fun onBackPressed() {
        if (!findNavController(R.id.fragment_container).navigateUp())
            if (isTaskRoot) {
                startActivity(Intent(this, LandingActivity::class.java))
                // using finish() is optional, use it if you do not want to keep currentActivity in stack
                finish()
            } else {
                super.onBackPressed()
                overridePendingTransition(R.anim.slide_right_out, R.anim.slide_left_in)
            }
    }

    companion object {
        // Using request code to finish the activity based on condition
        const val PAYMENT_ACTIVITY_REQUEST_CODE = 1132
        const val KEY_HAS_LOW_BALANCE_FOR_THIS_TXN = "hasLowBalanceForThisTxn"
    }


    @SuppressLint("CheckResult")
    override fun userLoggedIn(loginSource : String, isNewUser : Boolean) {
        e("CalledFromLogin","inside userLoggedIn of FreemiumActivity")
        super.userLoggedIn(loginSource, isNewUser)
    }
}
