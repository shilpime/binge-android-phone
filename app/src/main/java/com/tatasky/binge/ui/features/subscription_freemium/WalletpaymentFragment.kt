package com.tatasky.binge.ui.features.subscription_freemium

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.analytics.*
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.requests.PayByDTHBalanceRequest
import com.tatasky.binge.data.networking.models.response.AddPackResponse
import com.tatasky.binge.data.networking.models.response.PartnerPacks
import com.tatasky.binge.databinding.FragmentWalletPaymentBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.recharge.launchRechargeActivity
import com.tatasky.binge.ui.features.subscription.SubscriptionAnalytics
import com.tatasky.binge.ui.features.subscription_freemium.viewmodel.FreemiumSubscriptionViewModel
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.COMMON_ERROR_MSG
import com.tatasky.binge.utils.PaymentUtility.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers
import java.lang.Exception
import javax.inject.Inject

class WalletpaymentFragment : BaseFragment<FragmentWalletPaymentBinding, FreemiumSubscriptionViewModel>(){

    private var currentPack: PartnerPacks? = null
    @Inject
    lateinit var subscriptionAnalytics: SubscriptionAnalytics
    private var dthBalance: String? = null
    private var message: String? = null
    private var proratedAmount : String? = null

    override fun getViewModelClass(): Class<FreemiumSubscriptionViewModel> =
        FreemiumSubscriptionViewModel::class.java


    override fun layoutId(): Int = R.layout.fragment_wallet_payment

    override fun getViewModelOwner(): ViewModelStoreOwner = requireActivity()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            it.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        subscriptionAnalytics.trackPaymentFlowExit(
                            TransactionStatus.BACKPRESSED.transactionStatus.uppercase(),
                            NOT_SELECTED,
                            NOT_ATTEMPTED,
                            ""
                        )
                        findNavController().navigateUpOrFinish(it as AppCompatActivity)
                    }
                })
        }
    }

    private fun getSourceOrFromScreenName() =
        activity?.intent?.extras?.get(KEY_FROM_SCREEN) as String? ?: viewModel.source.takeIf {
            !it.isNullOrBlank()
        }?.also { viewModel.source = null /*After consumption, make it null again*/ }
        ?: SOURCE_HOME

    private fun trackPaymentInitiate(it: AddPackResponse) {
        subscriptionAnalytics.trackPaymentInitiate(
            getSourceOrFromScreenName(),
            activity?.intent?.getStringExtra(KEY_PACK_NAME)
                ?: it.data?.productName ?: "",
            activity?.intent?.getStringExtra(KEY_PACK_PRICE)
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
            activity?.intent?.getStringExtra(KEY_SELECTED_TENURE_TYPE)
                ?:
                /*Renew case*/
                sharedPrefs.getPreviousSubscribedPack()
                    ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType ?: "",
            activity?.intent?.getBooleanExtra(
                KEY_IS_FIRST_SUBSCRIPTION,
                PaymentUtility.isFirstPaidPack(
                    sharedPrefs.getSubscribedPack(),
                    sharedPrefs.getPreviousSubscribedPack(),
                    it.data?.firstPaidPackSubscriptionDate
                )
            ) ?:
            PaymentUtility.isFirstPaidPack(
                sharedPrefs.getSubscribedPack(),
                sharedPrefs.getPreviousSubscribedPack(),
                it.data?.firstPaidPackSubscriptionDate
            )
        )
    }

    override fun onError(errorModel: ErrorModel) {
        subscriptionAnalytics.trackSubscribeFailure(
            errorModel.message ?: COMMON_ERROR_MSG,
            activity?.intent?.getStringExtra(KEY_PACK_NAME) ?: sharedPrefs.getAddModifyResponse()?.data?.productName ?: "",
            PACK_TYPE_PAID,
            paymentMethod = TSWALLET,
            paymentType = "",
            activity?.intent?.getStringExtra(KEY_SELECTED_TENURE_TYPE),
            getSourceOrFromScreenName(),
            currentPack?.productName ?: FREEMIUM,
            currentPack?.let { if (true == currentPack?.freeTrialStatus) PACK_TYPE_FREE else PACK_TYPE_PAID }
                ?: PACK_TYPE_FREE,
            activity?.intent?.getStringExtra(KEY_PACK_PRICE)
                ?: sharedPrefs.getAddModifyResponse()?.data?.amount ?: "",
            currentPack?.amountValue ?: FREEMIUM,
            currentPack?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType ?: "",
            activity?.intent?.getBooleanExtra(
                KEY_IS_FIRST_SUBSCRIPTION,
                PaymentUtility.isFirstPaidPack(
                    sharedPrefs.getSubscribedPack(),
                    sharedPrefs.getPreviousSubscribedPack(),
                    sharedPrefs.getAddModifyResponse()?.data?.firstPaidPackSubscriptionDate
                )
            ) ?:
            PaymentUtility.isFirstPaidPack(
                sharedPrefs.getSubscribedPack(),
                sharedPrefs.getPreviousSubscribedPack(),
                sharedPrefs.getAddModifyResponse()?.data?.firstPaidPackSubscriptionDate
            ),
            PaymentUtility.getModificationType(
                sharedPrefs.getAddModifyResponse()?.data?.modificationType
            ),
            activity?.intent?.getStringExtra(KEY_SELECTED_TENURE_PACK_PRICE)
                ?: /*Renew case*/
                sharedPrefs.getSubscribedPack()
                    ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.offeredPriceValue
                ?: "",
            activity?.intent?.getStringExtra(
                KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX
            )
                ?: /*Renew case*/sharedPrefs.getSubscribedPack()
                    ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureDurationInDaysWithDSuffix
                ?: "",
            null,
            null,
            false,
            null,
            null
        )
        super.onError(errorModel)
    }

    override fun setObserver() {
        viewModel.errorOkClicked.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                activity?.let { fragmentActivity ->
                    findNavController().navigateUpOrFinish(fragmentActivity as AppCompatActivity)
                }
            }
        }

        viewModel.rechargeResponse().observe(this) {
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
                                activity?.finish()
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


        viewModel.getWalletBalance().observe(this) {
            it.getContentIfNotHandled()?.let { walletBalance ->
                message = walletBalance.data?.walletPaymentVerbiage
                walletBalance.data?.balanceQueryRespDTO?.let { rechargeResponse ->
                    dthBalance = rechargeResponse.balance
                    binding.dthBalance = dthBalance ?: "0"
                    binding.message = message ?:""
                }
            }
        }

        viewModel.getPayByDTHBalanceResponse().observe(this) {
            it.getContentIfNotHandled()?.let {
                fetchPaymentStatus(true)
            }
        }
        viewModel.getAddOrModifyPackResponse().observe(this){
            it.getContentIfNotHandled()?.let{
                trackPaymentInitiate(it)
                PaymentUtility.getModificationType(
                    it.data?.modificationType
                )?.let { modificationType ->
                    subscriptionAnalytics.trackModifyPackInitiate(
                        getSourceOrFromScreenName(),
                        activity?.intent?.getStringExtra(KEY_PACK_NAME)
                            ?: it.data?.productName ?: "",
                        activity?.intent?.getStringExtra(KEY_PACK_PRICE)
                            ?: it.data?.amount ?: "",
                        modificationType,
                        activity?.intent?.getStringExtra(KEY_APPSFLYER_SOURCE)
                            ?: getSourceOrFromScreenName(),
                        activity?.intent?.getStringExtra(KEY_SELECTED_TENURE_PACK_PRICE)
                            ?: it.data?.amount ?:
                            /*Renew case*/
                            sharedPrefs.getSubscribedPack()
                                ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.offeredPriceValue
                            ?: "",
                        activity?.intent?.getStringExtra(
                            KEY_SELECTED_TENURE_DURATION_IN_DAYS_WITH_D_SUFFIX
                        )
                            ?: /*Renew case*/sharedPrefs.getSubscribedPack()
                                ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureDurationInDaysWithDSuffix
                            ?: "",
                        sharedPrefs.getSubscribedPack()?.packType ?: PACK_TYPE_PAID,
                        sharedPrefs.getPreviousSubscribedPack()?.productName,
                        sharedPrefs.getPreviousSubscribedPack()?.packType ?: PACK_TYPE_PAID,
                        sharedPrefs.getPreviousSubscribedPack()?.amountValue,
                        sharedPrefs.getPreviousSubscribedPack()?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType,
                        activity?.intent?.getStringExtra(KEY_SELECTED_TENURE_TYPE) ?:
                        /*Renew case*/
                        sharedPrefs.getSubscribedPack()
                            ?.getCurrentOrLastActiveTenureDetailsForActiveOrInactiveUsers()?.tenureType,
                        activity?.intent?.getBooleanExtra(
                            KEY_IS_FIRST_SUBSCRIPTION,
                            PaymentUtility.isFirstPaidPack(
                                sharedPrefs.getSubscribedPack(),
                                sharedPrefs.getPreviousSubscribedPack(),
                                it.data?.firstPaidPackSubscriptionDate
                            )
                        ) ?:
                        PaymentUtility.isFirstPaidPack(
                            sharedPrefs.getSubscribedPack(),
                            sharedPrefs.getPreviousSubscribedPack(),
                           it.data?.firstPaidPackSubscriptionDate
                        )
                    )
                }
                fetchPaymentStatus(true)
            }
        }
    }

    private fun fetchPaymentStatus(payByDTH: Boolean = false) {
        var modifyPackCalled: Boolean
        val addModifyPackResponse = viewModel.getAddOrModifyPackResponse().value?.peekContent()?.data
        val bundle = Bundle().apply {
            if (activity?.intent?.getBooleanExtra(KEY_DTH, false) == true) {
                modifyPackCalled = (activity?.intent?.getBooleanExtra(KEY_MODIFY_PACK_CALLED, false) == true)
                this.putBoolean(
                    KEY_ADD_PACK_CALLED,
                    activity?.intent?.getBooleanExtra(KEY_ADD_PACK_CALLED, false) == true
                )
                this.putBoolean(
                    KEY_MODIFY_PACK_CALLED,
                    modifyPackCalled
                )
            } else {
                modifyPackCalled = viewModel.modifyPackCalled
                this.putBoolean(KEY_ADD_PACK_CALLED, viewModel.addPackCalled)
                this.putBoolean(KEY_MODIFY_PACK_CALLED, modifyPackCalled)
            }
            this.putString(
                "orderId",
                sharedPrefs.getAddModifyResponse()?.data?.paymentTransactionId
            )
            this.putString(
                "paymentHeader",
                sharedPrefs.getAddModifyResponse()?.data?.paymentStatusVerbiage?.header
            )
            this.putString(
                "paymentMessage",
                sharedPrefs.getAddModifyResponse()?.data?.paymentStatusVerbiage?.message
            )
            this.putString(
                "paymentFooter",
                sharedPrefs.getAddModifyResponse()?.data?.paymentStatusVerbiage?.footer
            )
            this.putBoolean(
                "upFrontMoneyCollected",
                sharedPrefs.getAddModifyResponse()?.data?.upFrontMoneyCollected
                    ?: false
            )
            this.putBoolean("payByDTH", payByDTH)
            this.putString(KEY_FROM_SCREEN, getSourceOrFromScreenName())
            PaymentUtility.getModificationType(
                sharedPrefs.getAddModifyResponse()?.data?.modificationType
            )?.let {
                this.putString(KEY_MODIFICATION_TYPE, it)
            }
            this.putString(KEY_ACTUAL_PRORATED_AMOUNT_FROM_API, activity?.intent?.getStringExtra("proratedAmount"))
            this.putString(KEY_PACK_PRICE, addModifyPackResponse?.selectedTenurePackPrice)
            this.putString(KEY_APPSFLYER_SOURCE, activity?.intent?.getStringExtra(KEY_APPSFLYER_SOURCE) ?: getSourceOrFromScreenName())
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
                activity?.intent?.getBooleanExtra(
                    KEY_IS_FIRST_SUBSCRIPTION,
                    PaymentUtility.isFirstPaidPack(
                        sharedPrefs.getSubscribedPack(),
                        sharedPrefs.getPreviousSubscribedPack(),
                        sharedPrefs.getAddModifyResponse()?.data?.firstPaidPackSubscriptionDate
                    )
                ) == true
            )
            this.putString(
                KEY_PRODUCT_TYPE,
                addModifyPackResponse?.productType
            )
        }
        subscriptionAnalytics.trackPaymentFlowExit(
            "",
            TP_WALLET,
            "",
            ""
        )
        startHomeScreen(
            activity,
            checkPaymentStatus = true,
            bundle = bundle
        )
    }

    private fun setWalletDetailsForFreemium(packDetails: AddPackResponse.Data?) {
        binding.amountValue.text = "₹"+packDetails?.amount
        viewModel.fetchBalance(
            activity?.intent?.getStringExtra("proratedAmount"),
            activity?.intent?.getStringExtra("selectedTenureID") ?: activity?.intent?.getStringExtra("packID") /*For change tenure use selectedTenureID, For Renew use packId*/
        )

        binding.payByTSBalCB.setOnCheckedChangeListener { _, isChecked ->
            binding.viewShouldShow = isChecked
        }
        binding.dthBalance = dthBalance ?: "0"

        binding.message = message ?:""

        binding.subIDTV.text =
            String.format(getString(R.string.subscriber_id_template),
                viewModel.sharedPref.getOriginalSubscriberId()
            )
        binding.makePaymentBtn.setOnClickListener {

            if (viewModel.getWalletBalance().value?.peekContent()?.data?.hasLowBalanceForThisTxn == false) {
                (activity as? BaseActivity<*>)?.payByDthBalanceSelected = true
                val selectedPackDetails = sharedPrefs.getAddModifyResponse()?.data
                viewModel.setProgressing(true)
                viewModel.payByDTHBalance(
                    PayByDTHBalanceRequest(
                        viewModel.sharedPref.getOriginalSubscriberId() ?: "",
                        selectedPackDetails?.amount.toString() ?: "",
                        selectedPackDetails?.productName ?: "",
                        selectedPackDetails?.productId ?: "",
                        sharedPrefs.getBaId(),
                        sharedPrefs.getAddModifyResponse()?.data?.paymentTransactionId
                    )
                )
            }
            else {
                // Open self care
                viewModel.startRecharge(getSourceOrFromScreenName())
            }
        }

    }

    //function for subscriptionType != FREEMIUM i.e. ATV, ANYWHERE, FS etc.
    private fun setWalletDetailsForNonFreemium() {
        val packId = activity?.intent?.getStringExtra("packID")
        val selectedTenureId = activity?.intent?.getStringExtra("selectedTenureID")
        val selectedTenureAmount = activity?.intent?.getStringExtra("selectedTenureAmount")
        val isMigrated = activity?.intent?.getBooleanExtra("isMigrated", false)?:false
        val migratedVerbiage = activity?.intent?.getStringExtra("migratedVerbiage" )
        val proratedAmount = activity?.intent?.getStringExtra("proratedAmount" )

        binding.amountValue.text = proratedAmount

        viewModel.fetchBalance(
            activity?.intent?.getStringExtra("proratedAmount"),
            activity?.intent?.getStringExtra("selectedTenureID") ?: activity?.intent?.getStringExtra("packID") /*For change tenure use selectedTenureID, For Renew use packId*/
        )

        binding.payByTSBalCB.setOnCheckedChangeListener { _, isChecked ->
            binding.viewShouldShow = isChecked
        }
        binding.dthBalance = dthBalance ?: "0"

        binding.message = message ?:""

        binding.subIDTV.text =
            String.format(getString(R.string.subscriber_id_template),
                viewModel.sharedPref.getOriginalSubscriberId()
            )
        binding.makePaymentBtn.setOnClickListener {
            if (viewModel.getWalletBalance().value?.peekContent()?.data?.hasLowBalanceForThisTxn == false) {
                (activity as? BaseActivity<*>)?.payByDthBalanceSelected = true
                if (!sharedPrefs.isManagedAppEnabled()) {
                    if (selectedTenureId != null) {
                        if (selectedTenureAmount != null) {
                            viewModel.packValidate(
                                selectedTenureId,
                                packId,
                                isMigrated,
                                migratedVerbiage,
                                callPackValidate = false,
                                selectedTenureAmount,
                                proratedAmount
                            )
                        }
                    }
                } else {
                    viewModel.callAddOrModifyPack(
                        activity?.intent?.getStringExtra(CART_ID),
                        activity?.intent?.getBooleanExtra(
                            KEY_IS_RENEW,
                            false
                        ) == true
                    )
                }
            }
            else {
                // Open self care
                viewModel.startRecharge(getSourceOrFromScreenName())
            }
        }

    }


    override fun toBeCalledOnce() {
        subscriptionAnalytics.trackViewPaymentGatewayScreen()
        currentPack = sharedPrefs.getSubscribedPack()
        viewModel.sharedPref.savePreviousSubscribedPack(currentPack)
        /*As of now some other individual item binding is not working
       *So binding complete viewmodel. Don't set lifecycle owner of this binding
       */
        binding.viewModel = viewModel

        viewModel.checkForManagedAppEligibility { eligible ->
        }
        if(!NON_DTH_USER.equals(sharedPrefs.getDthStatusFreemium(), true) && !sharedPrefs.getSubscriptionType().equals(
                subscriptionTypeFreemium,true)) {
            setWalletDetailsForNonFreemium()
        } else {
            setWalletDetailsForFreemium(sharedPrefs.getAddModifyResponse()?.data)
        }

        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        recharged = true
        viewModel.fetchBalance(
            activity?.intent?.getStringExtra("proratedAmount"),
            activity?.intent?.getStringExtra("selectedTenureID") ?: activity?.intent?.getStringExtra("packID") /*For change tenure use selectedTenureID, For Renew use packId*/
        )
        if (requestCode == RechargeActivity.RECHARGE_REQUEST_CODE) {
            if (1 == data?.getIntExtra("rechargeStatus", 0))
                showDialog(DialogModel(false, R.drawable.ic_success_tick, getString(R.string.payment_success), getString(R.string.proceed), null, getString(R.string.subscription_payment_success_msg)), object :
                    CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            else {
                showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.payment_failure), getString(R.string.ok), null, null), object :
                    CommonDialogEventListener {
                    override fun onPrimaryButtonClick() {
                        hideDialog()
                    }

                    override fun onSecondaryButtonClick() {
                    }

                    override fun onCloseButtonClick() {
                    }
                })
            }
        }
    }
}