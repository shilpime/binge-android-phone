package com.tatasky.binge.ui.features.subscription_freemium

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.analytics.PACK_TYPE_PAID
import com.tatasky.binge.analytics.SOURCE_HOME
import com.tatasky.binge.data.networking.models.requests.PayByDTHBalanceRequest
import com.tatasky.binge.data.networking.models.response.AddPackResponse
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.LayoutTsWalletBalanceBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.recharge.launchRechargeActivity
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.COMMON_ERROR_MSG
import com.tatasky.binge.utils.PaymentUtility.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers
import kotlinx.android.synthetic.main.activity_freemium_subscription.*
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class PaymentJourneyActivity : BaseActivity<CommonSampleViewModel>() {

    private var currentPack: PartnerPacks? = null
    @Inject
    lateinit var localBroadcastHelper: LocalBroadcastHelper
    private var initiatedFromThisActivity: Boolean = false
    private var proratedAmount : String? = null
    private lateinit var mSubscriptionViewModel: FreemiumSubscriptionViewModel
    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun allowedTouchWhenLoading(): Boolean = false

    private fun handleError(status: String) {
        hideProgress()
        // If transaction can be re-tried, then retry it
        //  Use TransactionErrorCode class for specific error handling
        fun showErrorDialog(
            verbiage: String,
            primaryBtnText: String,
            secondaryBtnText: String?,
            commonDialogEventListener: CommonDialogEventListener
        ) {
            showDialog(
                DialogModel(
                    cancelable = false,
                    imageId = R.drawable.ic_subscription_error,
                    title = "Payment Unsuccessful",
                    text = verbiage,
                    primaryButtonText  = primaryBtnText,
                    secondaryButtonText = secondaryBtnText
                ), commonDialogEventListener
            )
        }

        val addModifyResponse = sharedPrefs.getAddModifyResponse()
        val commonDialogEventListener: CommonDialogEventListener?
        //From Payment Journey always and only paid pack can be purchased.
        subscriptionAnalytics.trackSubscribeFailure(
            status,
            this@PaymentJourneyActivity.intent.getStringExtra(KEY_PACK_NAME)
                ?: mSubscriptionViewModel.getAddOrModifyPackResponse().value?.peekContent()?.data?.productName
                ?: "",
            PACK_TYPE_PAID,
            paymentMethod = PG,
            paymentType = "",
            this@PaymentJourneyActivity.intent.getStringExtra(KEY_SELECTED_TENURE_TYPE),
            getSourceOrFromScreenName(),
            currentPack?.productName ?: FREEMIUM,
            currentPack?.let { if (true == currentPack?.freeTrialStatus) PACK_TYPE_FREE else PACK_TYPE_PAID }
                ?: PACK_TYPE_FREE,
            this@PaymentJourneyActivity.intent.getStringExtra(KEY_PACK_PRICE)
                ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount ?: "",
            currentPack?.amountValue ?: FREEMIUM,
            currentPack?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType ?: "",
            this@PaymentJourneyActivity.intent.getBooleanExtra(
                KEY_IS_FIRST_SUBSCRIPTION,
                PaymentUtility.isFirstPaidPack(
                    sharedPrefs.getSubscribedPack(),
                    sharedPrefs.getPreviousSubscribedPack(),
                    addModifyResponse?.data?.firstPaidPackSubscriptionDate
                )
            ),
            PaymentUtility.getModificationType(
                addModifyResponse?.data?.modificationType
            ),
            this@PaymentJourneyActivity.intent.getStringExtra(KEY_SELECTED_TENURE_PACK_PRICE)
                ?: /*Renew case*/
                sharedPrefs.getSubscribedPack()
                    ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.offeredPriceValue
                ?: "",
            this@PaymentJourneyActivity.intent
                .getStringExtra(KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX)
                ?: /*Renew case*/
                sharedPrefs.getSubscribedPack()
                    ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureDurationInDaysWithDSuffix
                ?: "",
            null,
            null,
            false,
            null,
            null
        )
        when (status) {
            TransactionStatus.BACKPRESSED.transactionStatus.lowercase(Locale.getDefault()) -> {
                /**Tracking only Non DTH exit
                 * to avoid triggering when paying with Dth wallet in SDK PG
                 * */
                if (!payByDthBalanceSelected)
                    subscriptionAnalytics.trackPaymentFlowExit(
                        status.uppercase(),
                        NOT_SELECTED,
                        NOT_ATTEMPTED,
                        status.uppercase()
                    )
                finish()
            }
            TransactionStatus.USER_ABORTED.transactionStatus.lowercase(Locale.getDefault()) -> {
                if (!payByDthBalanceSelected)
                    subscriptionAnalytics.trackPaymentFlowExit(
                        TransactionStatus.BACKPRESSED.transactionStatus.uppercase(),
                        PAYMENT_GATEWAY,
                        NOT_ATTEMPTED,
                        status.uppercase()
                    )
                commonDialogEventListener = object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        if (initiatedFromThisActivity)
                            startHomeScreen(this@PaymentJourneyActivity)
                        else
                            finish()
                    }

                    override fun onSecondaryButtonClick() {
                        startHomeScreen(this@PaymentJourneyActivity)
                    }

                    override fun onCloseButtonClick() {
                    }

                }
                showErrorDialog(
                    addModifyResponse?.data?.paymentErrorVerbiages?.userCancelledVerbiage ?: "",
                    "Close",
                    null,
                    commonDialogEventListener
                )
            }
            TransactionStatus.PENDING_VBV.transactionStatus.lowercase(Locale.getDefault()) -> {
                if (!payByDthBalanceSelected)
                    subscriptionAnalytics.trackPaymentFlowExit(
                        PENDING,
                        PAYMENT_GATEWAY,
                        PENDING,
                        status.uppercase()
                    )
                fetchPaymentStatus()
            }
            else -> {
                if (!payByDthBalanceSelected)
                    subscriptionAnalytics.trackPaymentFlowExit(
                        FAILURE,
                        PAYMENT_GATEWAY,
                        FAILURE,
                        status.uppercase()
                    )
                commonDialogEventListener = object : CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        if (initiatedFromThisActivity)
                            startHomeScreen(this@PaymentJourneyActivity)
                        else
                            finish()
                    }

                    override fun onSecondaryButtonClick() {
                        startHomeScreen(this@PaymentJourneyActivity)
                    }

                    override fun onCloseButtonClick() {
                    }

                }
                showErrorDialog(
                    addModifyResponse?.data?.paymentErrorVerbiages?.paymentFailureVerbiage ?: "",
                    "Try Again",
                    "Close",
                    commonDialogEventListener
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.localBroadcastHelper.unregisterBroadcast(this, genericBroadcastReceiver)
        if (initiatedFromThisActivity)
            terminateJuspayService(this)
        hideProgress()
    }

    private fun getSourceOrFromScreenName() =
        intent?.extras?.get(KEY_FROM_SCREEN) as String? ?: viewModel.source.takeIf {
            !it.isNullOrBlank()
        }?.also { viewModel.source = null /*After consumption, make it null again*/ }
        ?: SOURCE_HOME

    private fun fetchPaymentStatus(payByDTH : Boolean = false) {
        hideProgress()
        val addModifyPackResponse = mSubscriptionViewModel.getAddOrModifyPackResponse().value?.peekContent()?.data
        val bundle = Bundle().apply {
            this.putBoolean("addPackCalled",mSubscriptionViewModel.addPackCalled)
            this.putBoolean("modifyPackCalled",mSubscriptionViewModel.modifyPackCalled)
            this.putString("orderId",mSubscriptionViewModel.getAddOrModifyPackResponse().value?.peekContent()?.data?.paymentPayload?.payload?.orderId)
            this.putString("paymentHeader",mSubscriptionViewModel.getAddOrModifyPackResponse().value?.peekContent()?.data?.paymentStatusVerbiage?.header)
            this.putString("paymentMessage",mSubscriptionViewModel.getAddOrModifyPackResponse().value?.peekContent()?.data?.paymentStatusVerbiage?.message)
            this.putString("paymentFooter",mSubscriptionViewModel.getAddOrModifyPackResponse().value?.peekContent()?.data?.paymentStatusVerbiage?.footer)
            this.putBoolean("upFrontMoneyCollected",mSubscriptionViewModel.getAddOrModifyPackResponse().value?.peekContent()?.data?.upFrontMoneyCollected?:false)
            this.putBoolean(KEY_PAY_BY_DTH, payByDTH)
            this.putString(KEY_FROM_SCREEN, getSourceOrFromScreenName())
            PaymentUtility.getModificationType(
                addModifyPackResponse?.modificationType
            )?.let {
                this.putString(KEY_MODIFICATION_TYPE, it)
            }
            this.putString(KEY_PACK_PRICE, addModifyPackResponse?.selectedTenurePackPrice)
            this.putString(KEY_APPSFLYER_SOURCE, this@PaymentJourneyActivity.intent.getStringExtra(KEY_APPSFLYER_SOURCE))
            this.putString(KEY_PACK_NAME, addModifyPackResponse?.productName)
            this.putString(KEY_SELECTED_TENURE_PACK_PRICE,
                addModifyPackResponse?.selectedTenurePackPrice
            )
            this.putString(
                KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX,
                addModifyPackResponse?.selectedTenureInDaysWithDSuffix
            )
            this.putString(
                KEY_SELECTED_TENURE_TYPE,
                addModifyPackResponse?.selectedTenureType
            )
            this.putBoolean(
                KEY_IS_FIRST_SUBSCRIPTION,
                PaymentUtility.isFirstPaidPack(
                    sharedPrefs.getSubscribedPack(),
                    sharedPrefs.getPreviousSubscribedPack(),
                    mSubscriptionViewModel.getAddOrModifyPackResponse().value?.peekContent()?.data?.firstPaidPackSubscriptionDate
                )
            )
            this.putString(
                KEY_PRODUCT_TYPE,
                addModifyPackResponse?.productType
            )
        }
        startHomeScreen(
            this,
            checkPaymentStatus = true,
            bundle = bundle

        )
    }

    private val genericBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when (p1?.action) {
                localBroadcastHelper.ACTION_MAKE_PAYMENT -> {

                    if (sharedPrefs.isManagedAppEnabled()) {
                        actionMakePaymentTickTick()
                    } else {
                        actionMakePayment(p1)
                    }

                }
                localBroadcastHelper.ACTION_PAYMENT_CHARGED -> fetchPaymentStatus() //Fetch latest payment status
                localBroadcastHelper.ACTION_PAYMENT_ERROR -> {
                    d("payment error status","${p1.extras?.getString("status")}")
                    p1.extras?.getString("status")?.let { handleError(it) } ?: run {
                        finish()
                    }
                 }
            }
        }
    }

    fun actionMakePayment(p1: Intent) {
        if (!p1.getBooleanExtra(
                FreemiumSubscriptionActivity.KEY_HAS_LOW_BALANCE_FOR_THIS_TXN, false
            )
        ) {
            //User has sufficient dth wallet balance
            payByDthBalanceSelected = true
            val packDetails =
                mSubscriptionViewModel.getAddOrModifyPackResponse().value?.peekContent()?.data
            showProgress()
            mSubscriptionViewModel.payByDTHBalance(
                PayByDTHBalanceRequest(
                    mSubscriptionViewModel.sharedPref.getOriginalSubscriberId() ?: "",
                    packDetails?.amount.toString() ?: "",
                    packDetails?.productName ?: "",
                    packDetails?.productId ?: "",
                    mSubscriptionViewModel.sharedPref.getBaId(),
                    mSubscriptionViewModel.sharedPref.getAddModifyResponse()?.data?.paymentTransactionId
                )
            )
        }
        else {
            // Open self care
            mSubscriptionViewModel.startRecharge(getSourceOrFromScreenName())
        }

    }

    fun actionMakePaymentTickTick(){
        val addOrModifyPackResponse =
            mSubscriptionViewModel.getAddOrModifyPackResponse().value?.peekContent()?.data
        if (addOrModifyPackResponse?.payByDthWalletData?.hasLowBalanceForThisTxn != true) {
            //User has sufficient dth wallet balance
            payByDthBalanceSelected = true
            showProgress()
            mSubscriptionViewModel.payByDTHBalance(
                PayByDTHBalanceRequest(
                    mSubscriptionViewModel.sharedPref.getOriginalSubscriberId() ?: "",
                    addOrModifyPackResponse?.amount.toString() ?: "",
                    addOrModifyPackResponse?.productName ?: "",
                    addOrModifyPackResponse?.productId ?: "",
                    mSubscriptionViewModel.sharedPref.getBaId(),
                    mSubscriptionViewModel.sharedPref.getAddModifyResponse()?.data?.paymentTransactionId
                )
            )
        }
        else {
            // Open self care
            mSubscriptionViewModel.startRecharge(getSourceOrFromScreenName(),
                addOrModifyPackResponse.amount.toString())
        }
    }

    private fun getDataAndValidatePack() {
        val intent = intent
        val packID = intent.getStringExtra("packID")
        val selectedTenureID = intent.getStringExtra("selectedTenureID")
        val isMigrated = intent.getBooleanExtra("isMigrated", false)
        val migratedVerbiage = intent.getStringExtra("migratedVerbiage")
        proratedAmount = intent.getStringExtra("proratedAmount")
        val selectedTenureAmount = intent.getStringExtra("selectedTenureAmount")
        val newUserDelay = intent.getBooleanExtra("newUserDelay",false)
        var callPackValidate: Boolean
        if(newUserDelay){
            Handler(Looper.getMainLooper()).postDelayed({
                if (selectedTenureID != null) {
                    callPackValidate = mSubscriptionViewModel.sharedPref.getSubscriptionType().equals(
                        subscriptionTypeFreemium, true
                    )
                    if (selectedTenureAmount != null) {
                        mSubscriptionViewModel.packValidate(selectedTenureID, packID, isMigrated, migratedVerbiage,callPackValidate,selectedTenureAmount,proratedAmount)
                    }
                }
            },3000)
        } else {
        if (selectedTenureID != null) {
            callPackValidate = mSubscriptionViewModel.sharedPref.getSubscriptionType().equals(
                subscriptionTypeFreemium, true
            )
            if (selectedTenureAmount != null) {
                mSubscriptionViewModel.packValidate(selectedTenureID, packID, isMigrated, migratedVerbiage,callPackValidate,selectedTenureAmount,proratedAmount)
            }
        }
        }
    }

    override fun bindPayByBalanceView(parent: ViewGroup?): View? {
        payByBalanceBinding = LayoutTsWalletBalanceBinding.inflate(layoutInflater, parent, false)
        var hasLowBalance = false
        val addOrModifyPackResponse = sharedPrefs.getAddModifyResponse()
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
                    proratedAmount ?: sharedPrefs.getSubscribedPack()?.amountValue,
                    intent.getStringExtra("selectedTenureID"),
                    fromScreenName = this.javaClass.simpleName
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
            localBroadcastHelper.sendBroadcast(
                this,
                Intent(localBroadcastHelper.ACTION_MAKE_PAYMENT).apply {
                    Bundle().putBoolean(
                        FreemiumSubscriptionActivity.KEY_HAS_LOW_BALANCE_FOR_THIS_TXN,
                        hasLowBalance
                    )
                })
        }
        return payByBalanceBinding?.root
    }

    override fun processJuspay(juspayProcessPayload: JSONObject?) {
        d(this.javaClass.simpleName, "Juspay: processJuspay Payload: $juspayProcessPayload")
        if (hyperInstance != null && hyperInstance?.isInitialised == true) {
            juspayProcessPayload?.let {
                d(this.javaClass.simpleName, "Juspay: process() called")
                subscriptionAnalytics.trackViewPaymentGatewayScreen()
                hyperInstance?.process(this, it)
            } ?: run {
                handleJusPayError(getString(R.string.unable_to_process))
            }
        } else {
            handleJusPayError(getString(R.string.unable_to_process))
        }
        if (!initiatedFromThisActivity)
            hideProgress()
    }

    private fun setObserver() {
        mSubscriptionViewModel.rechargeResponse().observe(this) {
            it.getContentIfNotHandled()?.data?.let {
                if (it.rechargeUrl.isNullOrBlank()) {
                    showDialog(
                        DialogModel(
                            false,
                            R.drawable.ic_subscription_error,
                            COMMON_ERROR_TITLE,
                            "Ok",
                            null,
                            "Recharge cannot be processed now please try after some time"
                        ),
                        object : CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                hideDialog()
                                finish()
                            }

                            override fun onCloseButtonClick() {
                            }

                            override fun onSecondaryButtonClick() {
                            }
                        })

                }
                else {
                    try {
                        launchRechargeActivity(this, Uri.parse(it.rechargeUrl))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        mSubscriptionViewModel.getAddOrModifyPackResponse().observe(this){
            it.getContentIfNotHandled()?.let {
                trackPaymentInitiate(it)
                PaymentUtility.getModificationType(
                    it.data?.modificationType
                )?.let { modificationType ->
                    subscriptionAnalytics.trackModifyPackInitiate(
                        getSourceOrFromScreenName(),
                        this@PaymentJourneyActivity.intent.getStringExtra(KEY_PACK_NAME)
                            ?: it.data?.productName ?: "",
                        this@PaymentJourneyActivity.intent.getStringExtra(KEY_PACK_PRICE)
                            ?: it.data?.amount ?: "",
                        modificationType,
                        this@PaymentJourneyActivity.intent.getStringExtra(KEY_APPSFLYER_SOURCE) ?: getSourceOrFromScreenName(),
                        this@PaymentJourneyActivity.intent.getStringExtra(KEY_SELECTED_TENURE_PACK_PRICE)
                            ?: it.data?.amount ?:
                            /*Renew case*/
                            sharedPrefs.getSubscribedPack()
                                ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.offeredPriceValue
                            ?: "",
                        this@PaymentJourneyActivity.intent.getStringExtra(
                            KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX
                        ) ?:
                        /*Renew case*/
                        sharedPrefs.getSubscribedPack()
                            ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureDurationInDaysWithDSuffix
                        ?: "",
                        sharedPrefs.getSubscribedPack()?.packType ?: PACK_TYPE_PAID,
                        sharedPrefs.getPreviousSubscribedPack()?.productName,
                        sharedPrefs.getPreviousSubscribedPack()?.packType ?: PACK_TYPE_PAID,
                        sharedPrefs.getPreviousSubscribedPack()?.amountValue,
                        sharedPrefs.getPreviousSubscribedPack()?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType,
                        this@PaymentJourneyActivity.intent.getStringExtra(KEY_SELECTED_TENURE_TYPE)
                            ?:
                            /*Renew case*/
                            sharedPrefs.getPreviousSubscribedPack()
                                ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType,
                        this@PaymentJourneyActivity.intent.getBooleanExtra(
                            KEY_IS_FIRST_SUBSCRIPTION,
                            PaymentUtility.isFirstPaidPack(
                                sharedPrefs.getSubscribedPack(),
                                sharedPrefs.getPreviousSubscribedPack(),
                                it.data?.firstPaidPackSubscriptionDate
                            )
                        )
                    )
                }
                sharedPrefs.saveAddModifyResponse(
                    Gson().toJson(
                        it
                    )
                )

                if(it.data?.upFrontMoneyCollected == true){
                    fetchPaymentStatus()
                }
                else if (it.data?.DTH == true) {
                    val intent = Intent(this, WalletPaymentActivity::class.java)
                    intent.putExtra(
                        "packID",
                        this.intent.getStringExtra("packID") ?: it.data?.productId
                    )
                    intent.putExtra("selectedTenureID", this.intent.getStringExtra("selectedTenureID"))
                    intent.putExtra("selectedTenureAmount", this.intent.getStringExtra("selectedTenureAmount"))
                    intent.putExtra("isMigrated", this.intent.getBooleanExtra("isMigrated", false))
                    intent.putExtra("migratedVerbiage", this.intent.getStringExtra("migratedVerbiage"))
                    intent.putExtra("proratedAmount",
                        it.data?.amount ?: proratedAmount
                        ?: sharedPrefs.getSubscribedPack()?.amountValue
                    )
                    intent.putExtra(KEY_PACK_PRICE, this.intent.getStringExtra(KEY_PACK_PRICE))
                    intent.putExtra(KEY_ADD_PACK_CALLED, mSubscriptionViewModel.addPackCalled)
                    intent.putExtra(KEY_MODIFY_PACK_CALLED, mSubscriptionViewModel.modifyPackCalled)
                    intent.putExtra(KEY_DTH, true)
                    intent.putExtra(KEY_APPSFLYER_SOURCE,this.intent.getStringExtra(KEY_APPSFLYER_SOURCE) ?: getSourceOrFromScreenName())
                    intent.putExtra(KEY_PACK_NAME, this.intent.getStringExtra(KEY_PACK_NAME))
                    intent.putExtra(
                        KEY_SELECTED_TENURE_PACK_PRICE,
                        this.intent.getStringExtra(KEY_SELECTED_TENURE_PACK_PRICE)
                            ?:
                            /*Renew case*/
                            sharedPrefs.getSubscribedPack()
                                ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.offeredPriceValue
                            ?: "",
                        )
                    intent.putExtra(
                        KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX,
                        this.intent.getStringExtra(
                            KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX
                        ) ?:
                        /*Renew case*/
                        sharedPrefs.getSubscribedPack()
                            ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureDurationInDaysWithDSuffix
                        ?: ""
                    )
                    intent.putExtra(
                        KEY_SELECTED_TENURE_TYPE,
                        this.intent.getStringExtra(
                            KEY_SELECTED_TENURE_TYPE
                        ) ?:
                        /*Renew case*/
                        sharedPrefs.getSubscribedPack()
                            ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType
                        ?: ""
                    )
                    intent.putExtra(
                        KEY_IS_FIRST_SUBSCRIPTION,
                        this@PaymentJourneyActivity.intent.getBooleanExtra(
                            KEY_IS_FIRST_SUBSCRIPTION,
                            PaymentUtility.isFirstPaidPack(
                                sharedPrefs.getSubscribedPack(),
                                sharedPrefs.getPreviousSubscribedPack(),
                                it.data?.firstPaidPackSubscriptionDate
                            )
                        )
                    )
                    this.startActivity(intent)
                    this.finish()
                }
            }
        }

        mSubscriptionViewModel.getErrorResponse().observe(this) {
            it.getContentIfNotHandled()?.let { it1 ->
                hideProgress()
                if (it1.code != CODE_SUCCESS) {
                    subscriptionAnalytics.trackSubscribeFailure(
                        it1.message ?: COMMON_ERROR_MSG,
                        this@PaymentJourneyActivity.intent.getStringExtra(KEY_PACK_NAME)
                            ?: mSubscriptionViewModel.getAddOrModifyPackResponse().value?.peekContent()?.data?.productName
                            ?: "",
                        PACK_TYPE_PAID.lowercase(Locale.getDefault())
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        PG,
                        "",
                        this@PaymentJourneyActivity.intent.getStringExtra(KEY_SELECTED_TENURE_TYPE),
                        getSourceOrFromScreenName(),
                        currentPack?.productName ?: FREEMIUM,
                        currentPack?.let { if (true == currentPack?.freeTrialStatus) PACK_TYPE_FREE else PACK_TYPE_PAID }
                            ?: PACK_TYPE_FREE,
                        this@PaymentJourneyActivity.intent.getStringExtra(KEY_PACK_PRICE)
                            ?: viewModel.sharedPrefs.getAddModifyResponse()?.data?.amount ?: "",
                        currentPack?.amountValue ?: FREEMIUM,
                        currentPack?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType ?: "",
                        this@PaymentJourneyActivity.intent.getBooleanExtra(
                            KEY_IS_FIRST_SUBSCRIPTION,
                            PaymentUtility.isFirstPaidPack(
                                sharedPrefs.getSubscribedPack(),
                                sharedPrefs.getPreviousSubscribedPack(),
                                sharedPrefs.getAddModifyResponse()?.data?.firstPaidPackSubscriptionDate
                            )
                        ),
                        PaymentUtility.getModificationType(
                            mSubscriptionViewModel.getAddOrModifyPackResponse().value?.peekContent()?.data?.modificationType
                        ),
                        this@PaymentJourneyActivity.intent.getStringExtra(KEY_SELECTED_TENURE_PACK_PRICE)
                            ?: /*Renew case*/
                            sharedPrefs.getSubscribedPack()
                                ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.offeredPriceValue
                            ?: "",
                        this@PaymentJourneyActivity.intent
                            .getStringExtra(KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX)
                            ?: /*Renew case*/
                            sharedPrefs.getSubscribedPack()
                                ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureDurationInDaysWithDSuffix
                            ?: "",
                        null,
                        null,
                        false,
                        null,
                        null
                    )
                    showDialog(
                        DialogModel(
                            false,
                            R.drawable.ic_subscription_error,
                            COMMON_ERROR_TITLE,
                            "Ok",
                            null,
                            it1.message,
                            statusCode = it1.code
                        ),
                        object : CommonDialogEventListener {
                            override fun onPrimaryButtonClick() {
                                hideDialog()
                                finish()
                            }

                            override fun onCloseButtonClick() {
                            }

                            override fun onSecondaryButtonClick() {
                            }
                        })

                }
            }
        }

        mSubscriptionViewModel.getJuspayProcessPayload().observe(this) {
            it.getContentIfNotHandled()?.let { juspayProcessPayload ->
                this.juspayProcessPayload = juspayProcessPayload
                if (hyperInstance?.isInitialised == false) {
                    // Start initiating Juspay SDK
                    mSubscriptionViewModel.fetchJuspayInitiatePayload(
                        HashMap<String, String>().apply {
                            put(PaymentGatewayConstants.KEY_CUSTOMER_ID, mSubscriptionViewModel.sharedPref.getOriginalSubscriberId())
                            put(PaymentGatewayConstants.KEY_CUSTOMER_EMAIL, mSubscriptionViewModel.sharedPref.getSelectedProfile()?.emailId ?: "")
                            put("rmn", mSubscriptionViewModel.sharedPref.getClearRMN() ?: "")
                        }
                    )
                }
                else {
                    // Juspay SDK already initialised from another activity
                    processJuspay(this.juspayProcessPayload)
                }
            }
        }

        mSubscriptionViewModel.getPayByDTHBalanceResponse().observe(this) {
            it.getContentIfNotHandled()?.let {
                fetchPaymentStatus(payByDTH = true)
            }
        }

        mSubscriptionViewModel.getJuspayInitiatePayload().observe(this) {
            it.getContentIfNotHandled()?.let { juspayInitiatePayload ->
                initiatedFromThisActivity = true
                initiateJusPay(
                    this,
                    juspayInitiatePayload,
                    mSubscriptionViewModel.sharedPref.getDthStatusFreemium(),
                    shouldShowInfoOnUI = true,
                    alsoCallProcess = true
                )
            }
        }
    }

    private fun trackPaymentInitiate(it: AddPackResponse) {
        subscriptionAnalytics.trackPaymentInitiate(
            getSourceOrFromScreenName(),
            this@PaymentJourneyActivity.intent.getStringExtra(KEY_PACK_NAME)
                ?: it.data?.productName ?: "",
            this@PaymentJourneyActivity.intent.getStringExtra(KEY_PACK_PRICE)
                ?: it.data?.amount ?: "",
            PaymentUtility.getModificationType(
                it.data?.modificationType
            ) ?: "",
            sharedPrefs.getSubscribedPack()?.packType ?: PACK_TYPE_PAID,
            currentPack?.productName ?: FREEMIUM,
            currentPack?.let { if (true == currentPack?.freeTrialStatus) PACK_TYPE_FREE else PACK_TYPE_PAID }
                ?: PACK_TYPE_FREE,
            currentPack?.amountValue ?: FREEMIUM,
            currentPack?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType ?: "",
            this@PaymentJourneyActivity.intent.getStringExtra(KEY_SELECTED_TENURE_TYPE)
                ?:
                /*Renew case*/
                sharedPrefs.getPreviousSubscribedPack()
                    ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType ?: "",
            this@PaymentJourneyActivity.intent.getBooleanExtra(
                KEY_IS_FIRST_SUBSCRIPTION,
                PaymentUtility.isFirstPaidPack(
                    sharedPrefs.getSubscribedPack(),
                    sharedPrefs.getPreviousSubscribedPack(),
                    it.data?.firstPaidPackSubscriptionDate
                )
            )
        )
    }

    override fun getContentViewId(): Int = R.layout.activity_payment_journey

    override fun getRootLayoutContainer(): View = splash_container

    override fun getViewModelClass(): Class<CommonSampleViewModel> =
        CommonSampleViewModel::class.java

    override fun init(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out)
        if (!viewModel.isLoggedIn()) {
            logoutApplication(this)
        }
        // Payment journey from Juspay SDK should start
        toBeCalledOnce()
    }

    private fun toBeCalledOnce() {
//        if (BuildConfig.DEBUG)
//            WebView.setWebContentsDebuggingEnabled(true)
//        subscriptionAnalytics.trackViewPaymentGatewayScreen()
        viewModel.checkForManagedAppEligibility { eligible ->
        }
        showProgress()
        registerBroadcast()
        mSubscriptionViewModel = ViewModelProvider(
            this,
            mViewModelFactory
        )[FreemiumSubscriptionViewModel::class.java]
        currentPack = sharedPrefs.getSubscribedPack()
        mSubscriptionViewModel.sharedPref.savePreviousSubscribedPack(currentPack)
        setupPaymentSDK(
            viewModel.sharedPrefs.getConfigResponse()?.data?.config?.paymentGatewayInfo?.paymentClientId
                ?: "",
            viewModel.sharedPrefs.getConfigResponse()?.data?.config?.paymentGatewayInfo?.paymentServiceId
                ?: "",
            viewModel.sharedPrefs.getConfigResponse()?.data?.config?.paymentGatewayInfo?.paymentBetaAssets
                ?: false
        )
        setObserver()
        if (!sharedPrefs.isManagedAppEnabled())
            getDataAndValidatePack()
        else
            mSubscriptionViewModel.callAddOrModifyPack(
                this.intent?.getStringExtra(CART_ID),
                this.intent?.getBooleanExtra(
                    KEY_IS_RENEW,
                    false
                ) == true
            )
    }

    // Register broadcast to listen for any broadcast and based on that do action
    private fun registerBroadcast() {
        this.localBroadcastHelper.registerBroadcast(this, genericBroadcastReceiver, localBroadcastHelper.ACTION_MAKE_PAYMENT)
        this.localBroadcastHelper.registerBroadcast(this, genericBroadcastReceiver, localBroadcastHelper.ACTION_PAYMENT_CHARGED)
        this.localBroadcastHelper.registerBroadcast(this, genericBroadcastReceiver, localBroadcastHelper.ACTION_PAYMENT_ERROR)
    }

    override fun onSupportNavigateUp(): Boolean {
//        setResult(Activity.RESULT_CANCELED)
        return findNavController(R.id.fragment_container).navigateUpOrOpenHome(this)
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            // using finish() is optional, use it if you do not want to keep currentActivity in stack
            finish()
        } else {
            val backPressHandled = if (hyperInstance != null)
                hyperInstance?.onBackPressed()
            else
                false
            if (!backPressHandled!!) {
                super.onBackPressed()
            }
        }
    }
}
