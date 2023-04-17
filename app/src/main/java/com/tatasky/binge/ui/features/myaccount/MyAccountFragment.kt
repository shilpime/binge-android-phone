package com.tatasky.binge.ui.features.myaccount

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.robinhood.ticker.TickerUtils
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_ACCOUNT
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.data.networking.models.response.SubscriberProfileListModel
import com.tatasky.binge.databinding.FragmentMyAccountBinding
import com.tatasky.binge.helper.circularImageLoadWithoutCache
import com.tatasky.binge.ui.base.frameworks.base.CancellationBaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.ui.features.home.HomeAnalytics
import com.tatasky.binge.utils.*
import javax.inject.Inject


class MyAccountFragment : CancellationBaseFragment<FragmentMyAccountBinding, MyAccountViewModel>() {
    @Inject
    lateinit var homeAnalytics: HomeAnalytics


    private var selectedProfile: SubscriberProfileListModel? = null
    private var mainViewModel : CommonSampleViewModel?=null
    private var shouldAnimateBalance = false
    private var rotationFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            this.duration = 500
        }
        reenterTransition = backward

        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            this.duration = 500
        }
        exitTransition = forward
    }

    override fun getViewModelClass(): Class<MyAccountViewModel> {
        return MyAccountViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_my_account
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return this
    }

    override fun setObserver() {
        super.setObserver()
        viewModel.unreadNotificationCount().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let{unreadNotificationCount ->
                binding.notification.count = unreadNotificationCount
            }
        })

        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                if (it) {
                    //refresh Account page for subscription detail
                    setSubscriptionView()
                }
            }
        })
        viewModel.getProfileInfo().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { hr ->
                selectedProfile = hr
                inflateSubscriptionView()
            }
        })
        viewModel.getNumberOfBingeAccounts().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                sharedPrefs.saveNumberOfBingeAccount(it.data?.listOfBaIds?.size ?: 1)
                updateSwitchAccountCTA()
            }
        })
        viewModel.getEditAliasResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { response ->
                showToast(context, getString(R.string.alias_updated))
            }
        })
        viewModel.getWalletBalance().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { walletBalance ->
                walletBalance.data?.balanceQueryRespDTO?.let { rechargeResponse ->
                    binding.tickerBalance.setText( "₹ "+ getDefaultNumber(binding.tickerBalance.text?.toString()?.replace("₹ ", "")), false)
                    binding.tickerBalance.setText("₹ "+ rechargeResponse.balance,shouldAnimateBalance)
                    binding.tvRechargeDue.text = getString(R.string.recharge_due, rechargeResponse.endDate)
                    shouldAnimateBalance = false
                }
            }
        })
        viewModel.getRefreshFlag().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let{
                if(!it)
                    rotationFlag = false
            }
        })
    }

    private fun updateSwitchAccountCTA() {
        if(sharedPrefs.getNumberOfBingeAccount() > 1){
            binding.switchAccount.root.show()
        }
        else
            binding.switchAccount.root.hide()
    }

    private fun inflateSubscriptionView() {
        setSubscriptionView()

        binding.model = selectedProfile
        binding.largeScreen = viewModel.sharedPrefs.getSubscriptionType().let{ it == subscriptionTypeAtv || it == subscriptionTypeFtv} || !viewModel.sharedPrefs.getSubscribedPack()?.subscriptionDetailInfo?.complementaryPlanLinkedMessage.isNullOrBlank()
        var selectedProfileSaved = viewModel.sharedPrefs.getSelectedProfile()
        val config = viewModel.sharedPrefs.getConfigResponse()

        val w = dpToPx(requireContext(), 75)
        if (selectedProfileSaved == null) {
            selectedProfileSaved = LoginResponse.BingeSubscription()
            selectedProfileSaved.baId = selectedProfile?.userData?.id
            selectedProfileSaved.aliasName = selectedProfile?.userData?.aliasName
            selectedProfileSaved.profileId = selectedProfile?.userData?.profileId
            selectedProfileSaved.imageUrl = selectedProfile?.userData?.image
            selectedProfileSaved.aliasName = selectedProfile?.userData?.aliasName
        }

        binding.profileLayout.tvLetter.text = ((selectedProfile?.userData?.firstName?.substring(0, 1)) ?: "A").toUpperCase()

        if (selectedProfile?.userData?.image.isNullOrEmpty() &&
            selectedProfile?.userData?.firstName?.isNotEmpty() == true
        ) {
            binding.profileLayout.tvLetter.show()
            binding.profileLayout.profileImage.hide()
        } else {
            binding.profileLayout.tvLetter.show()
            binding.profileLayout.profileImage.hide()
            selectedProfile?.userData?.image?.let {
                val imgUrl = getCloudinaryUrl(
                    viewModel.sharedPrefs.getCloudenieryUrl(),
                    w, w,
                    config?.data?.config?.subscriberImage?.imageBaseUrl + it
                )
                circularImageLoadWithoutCache(binding.profileLayout.profileImage, imgUrl, R.drawable.circle_with_gradient_border)
                binding.profileLayout.tvLetter.hide()
                binding.profileLayout.profileImage.show()
            }
        }
        selectedProfileSaved.emailId = selectedProfile?.userData?.email
        selectedProfileSaved.aliasName = selectedProfile?.userData?.aliasName
        selectedProfileSaved.imageUrl = selectedProfile?.userData?.image
        viewModel.sharedPrefs.setSelectedProfile(selectedProfileSaved)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun toBeCalledOnce() {
        viewModel.fetchBalance()
        viewModel.fetchBaIdList(viewModel.sharedPrefs.getOriginalSubscriberId(), false)
        val rotationAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.rotation)
        rotationAnim.repeatCount = Animation.INFINITE
        updateSwitchAccountCTA()
        binding.ivRefresh.setOnClickListener {
            binding.ivRefresh.startAnimation(rotationAnim)
            shouldAnimateBalance = true
            rotationFlag = true
            viewModel.fetchBalance()
        }
        rotationAnim.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {
                if (!rotationFlag)
                    rotationAnim.cancel()
            }
            override fun onAnimationEnd(p0: Animation?) {
            }
            override fun onAnimationStart(p0: Animation?) {
            }
        })

        val tf: Typeface = Typeface.createFromAsset(resources.assets, "fonts/Sky_Med.ttf")
        binding.tickerBalance.typeface = tf
        binding.tickerBalance.setCharacterLists(TickerUtils.provideNumberList())
        binding.tickerBalance.setText("₹ --.--", false)
        binding.sid = viewModel.sharedPrefs.getOriginalSubscriberId()
        binding.tvID.setOnLongClickListener {
            try {
                (activity?.getSystemService(Activity.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("SID", binding.sid))
            } catch (e:Exception){}
            true
        }
        binding.profileLayout.rlAlias.setOnTouchListener { v, event ->
            binding.profileLayout.etAddAlias.isFocusable = true
            binding.profileLayout.etAddAlias.isCursorVisible = true
            binding.profileLayout.etAddAlias.setSelection(binding.profileLayout.etAddAlias.text.toString().length)
            binding.profileLayout.etAddAlias.requestFocus()
            binding.profileLayout.etAddAlias.setFocusableInTouchMode(true)
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.profileLayout.etAddAlias, InputMethodManager.SHOW_IMPLICIT)
            true
        }

        onItemClickListeners()

        binding.profileLayout.etAddAlias.setOnEditorActionListener(object : OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (event != null && event!!.getKeyCode() === KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (TextUtils.isEmpty(binding.profileLayout.etAddAlias.text.toString().trim())) {
                        showToast(context, getString(R.string.alias_error))
                        return true
                    }
                    binding.profileLayout.etAddAlias.isCursorVisible = false
                    viewModel.editAliasName(
                        viewModel.sharedPrefs.getBaId(),
                        binding.profileLayout.etAddAlias.text.toString().trim()
                    )
                }
                return false
            }
        })
        binding.btnRecharge.setOnClickListener {
            if(shouldStartCancellationTrigger(true)){
                viewModel.fetchBaIdList(sharedPrefs.getOriginalSubscriberId())
            }
            else {
                findNavController()
                    .navigateSafe(
                        MyAccountFragmentDirections.actionActionAccountLandingToSubscription(
                            fromLogin = false,
                            selectedAppId = null,
                            fromScreen = SOURCE_ACCOUNT,
                            initiateRecharge = true
                        )
                    )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            this.duration = 500
        }
        reenterTransition = backward

        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            this.duration = 500
        }
        exitTransition = forward
    }

    private fun setSubscriptionView() {
        binding.profileLayout.llCurrentPlan.hide()
        binding.profileLayout.llPrime.hide()
        val selectedPack = viewModel.sharedPrefs.getSubscribedPack()?.takeIf { !it.doNotConsiderThePack }
        selectedPack?.let {
            binding.profileLayout.llCurrentPlan.show()

            selectedProfile?.userData?.planName = it.packName
            //2020-07-01T05:38:30
//            selectedProfile?.userData?.expireDate = convertPackDate(it.expirationDate,
//                SERVER_DATE_TIME_FORMAT, "dd/MM/yy")
            binding.profileLayout.llPrime.hide()
            if(true == it.subscriptionDetailInfo?.migrated) {
                binding.profileLayout.tvPlanExpire.show()
            } else {
                binding.profileLayout.tvPlanExpire.hide()
            }
            val primePackDetails = it.primePackDetails?:sharedPrefs.getPrimePackDetails()
            primePackDetails?.let {
                if (it.isActive || it.isSuspended) {
                    binding.profileLayout.llPrime.show()
                    binding.profileLayout.tvPrimeName.text = it.title?.takeIf { it.isNotBlank() } ?: "Amazon Prime"
                    binding.profileLayout.tvPrimeExpire.text = it.expiryDateToDisplay
                }
            }
            var color = ContextCompat.getColor(requireContext(), R.color.pink_50)
            if (!viewModel.sharedPrefs.isActivePack()) {
                color = ContextCompat.getColor(requireContext(), R.color.darkError)
            }
            binding.profileLayout.tvPlanExpire.text = it.accountScreenExpiryMessage
            binding.profileLayout.tvPlanExpire.setTextColor(color)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        homeAnalytics.trackAccountInitiate()
        mainViewModel = ViewModelProvider(requireActivity(), viewModelFactory)[CommonSampleViewModel::class.java]
        binding.profileLayout.etAddAlias.setOnEditTextImeBackListener(object : EditTextImeBackListener {
            override fun onImeBack(text: String) {
                binding.profileLayout.etAddAlias.isCursorVisible = false
                selectedProfile?.let {
                    binding.profileLayout.etAddAlias.setText(it.userData?.aliasName?:"")
                }

            }
        })
    }

    fun onItemClickListeners() {

        binding.editProfile.clRoot.setOnClickListener {
            selectedProfile?.userData?.let {
                findNavController().navigateSafe(
                    MyAccountFragmentDirections.actionActionAccountLandingToEditProfileFragment(
                        it
                    )
                )
            }
        }

        binding.transactionHistory.clRoot.setOnClickListener {
            homeAnalytics.trackTransactionHistoryInitiate()
            val id = selectedProfile?.userData?.aliasName ?:""
            findNavController().navigateSafe(
                MyAccountFragmentDirections.actionActionAccountLandingToTransactionHistoryFragment(
                    id
                )
            )

        }
        binding.link.clRoot.setOnClickListener {

            findNavController()
                .navigateSafe(MyAccountFragmentDirections.actionActionAccountLandingToNavLinkAccount())

        }
        binding.management.clRoot.setOnClickListener {

            findNavController()
                .navigateSafe(MyAccountFragmentDirections.actionActionAccountLandingToDeviceManagementFragment())
        }
        binding.mySubscription.clRoot.setOnClickListener {
            findNavController()
                .navigateSafe(
                    MyAccountFragmentDirections.actionActionAccountLandingToSubscription(
                        false,
                        null,
                        fromScreen = SOURCE_ACCOUNT
                    )
                )
        }
        binding.notification.clRoot.setOnClickListener {

            findNavController().navigateSafe(MyAccountFragmentDirections.actionActionAccountLandingToNotificationFragment())

        }
        binding.switchAccount.clRoot.setOnClickListener {

            findNavController()
                .navigateSafe(MyAccountFragmentDirections.actionMyAccountSwitchAccount())
        }
    }

    override fun onStart() {
        super.onStart()
        refreshNotificationCount()
        viewModel.fetchProfileInfo()
    }

    override fun refreshNotificationCount() {
        viewModel.getUnReadCleverTapNotificationCount(requireActivity())
    }

    private fun getDefaultNumber(balance:String?) : String {
        try {
            if (!TextUtils.isEmpty(balance)) {
                val digitCount = balance?.length?:0
                val stringBuilder = StringBuilder(digitCount)
                for (i in 0 until digitCount-1) {
                    stringBuilder.append("" + i)
                }
                stringBuilder.insert(digitCount - 3, ".");
                return stringBuilder.toString()
            }
        } catch (e:Exception) {
        }
        return "0";
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if(requestCode==RechargeActivity.RECHARGE_REQUEST_CODE){
//            viewModel.fetchBalance()
//        }
//    }

}