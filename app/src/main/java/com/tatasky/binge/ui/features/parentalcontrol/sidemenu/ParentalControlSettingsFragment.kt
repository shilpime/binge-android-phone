package com.tatasky.binge.ui.features.parentalcontrol.sidemenu

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentParentalControlSettingsBinding
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.setAllOnClickListener
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.TabletType
import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.*
import com.tatasky.binge.ui.features.sidemenunavdrawer.SideMenuDrawerAnalytics
import com.tatasky.binge.utils.PARENTAL_VIEWING_RESTRICTION_UPDATED
import com.tatasky.binge.utils.PARENTAL_PIN_SETUP_SUCCESS
import com.tatasky.binge.utils.CUSTOM_SNACKBAR_DELAY
import com.tatasky.binge.utils.isTablet
import com.tatasky.binge.utils.navigateSafe
import com.tatasky.binge.utils.showCustomToast
import com.tatasky.binge.utils.getTabletType
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.showToast
import kotlinx.android.synthetic.main.fragment_parental_control_settings.*
import javax.inject.Inject

class ParentalControlSettingsFragment :
    BaseFragment<FragmentParentalControlSettingsBinding, ParentalControlViewModel>() {

    private lateinit var transaction: FragmentTransaction
    private lateinit var childFragment: Fragment
    private lateinit var args: Bundle
    @Inject
    lateinit var sideMenuDrawerAnalytics: SideMenuDrawerAnalytics
    private var isResultHandled = false
    private var isDialogOpen = false
    private var isDeviceTablet = false

    override fun getViewModelClass(): Class<ParentalControlViewModel> =
        ParentalControlViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_parental_control_settings
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
       uiChanges()
    }

    override fun getViewModelOwner(): ViewModelStoreOwner = if (isTablet(requireContext())) {
        this
    } else {
        findNavController().getViewModelStoreOwner(R.id.nav_parental_control_menu)
    }

    override fun setObserver() {
        activity?.let {
            isDeviceTablet = isTablet(it)
        }
        findNavController()
            .currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<ParentalControlBottomSheetResult>(
                KEY_PARENTAL_CONTROL_BOTTOM_DIALOG_RESULT
            )
            ?.observe(viewLifecycleOwner) { result ->
                if (!isResultHandled) {
                    isResultHandled = true
                    when (result.resultStatus) {
                        ParentalControlBottomSheetResultStatus.SUCCESS_DISMISS -> {
                            when (result.actionBeforeOpeningBottomSheet) {
                                ACTION_PIN_CHANGE, ACTION_PIN_FORGOT -> {

                                    if (!(activity as LandingActivity)?.isParentalPinChanged) {
                                        if (isDeviceTablet) {
                                            showParentalPinToastForTabletView(
                                                R.string.toast_msg_pin_changed_successful,
                                                R.drawable.ic_tick_login_success
                                            )
                                        } else {
                                            showParentalPinToastForMobileView(
                                                R.string.toast_msg_pin_changed_successful,
                                                R.drawable.ic_tick_login_success
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        ParentalControlBottomSheetResultStatus.PIN_VERIFIED -> {
                            when (result.actionBeforeOpeningBottomSheet) {
                                ACTION_RATING_CHANGE -> {
                                    viewModel.parentalPinValue = result.pinValue
                                    result.pinValue?.let { sharedPrefs.setParentalPin(it) }
                                    Handler().postDelayed({

                                        context?.let {
                                            if (isTablet(it)) {
                                                if (sharedPrefs.gethandleRatingScreenTablet() == null || sharedPrefs.gethandleRatingScreenTablet() == "no") {
                                                    binding.SidePaneContainer.visibility =
                                                        View.VISIBLE
                                                    binding.clParentLayout.visibility = View.GONE
                                                    childFragment = ParentalControlRatingFragment()
                                                    args = Bundle()
                                                    args.putBoolean("isPCRatingChange", true)
                                                    childFragment.arguments = args
                                                    transaction =
                                                        childFragmentManager.beginTransaction()
                                                    transaction.replace(
                                                        R.id.SidePaneContainer,
                                                        childFragment
                                                    ).commit()
                                                }
                                            } else {
                                                findNavController().navigateSafe(
                                                    ParentalControlSettingsFragmentDirections.actionParentalControlSettingsFragmentToParentalControlRatingFragment(
                                                        isPCRatingChange = true,
                                                    )
                                                )
                                            }
                                        }
                                    }, 500)
                                }
                            }
                        }
                    }
                }
            }
    }

    override fun toBeCalledOnce() {
        checkAndShowCustomToastMessage()
        sideMenuDrawerAnalytics.trackParentalControlSettingView()
        if (!sharedPrefs.isParentalPinExists()) {

            if (context?.let { isTablet(it) } == true) {
                binding.SidePaneContainer.visibility = View.VISIBLE
                binding.clParentLayout.visibility = View.GONE
                childFragment = ParentalControlRatingFragment()
                args = Bundle()
                args.putBoolean("isPCRatingChange", false)
                childFragment.arguments = args
                val transaction =
                    childFragmentManager.beginTransaction()
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                transaction.replace(R.id.SidePaneContainer, childFragment).commit()
            } else {
                findNavController().navigateSafe(
                    ParentalControlSettingsFragmentDirections.actionParentalControlSettingsFragmentToParentalControlRatingFragmentWithPopUpto()
                )
            }
        }
    }

    private fun checkAndShowCustomToastMessage() {
        when {
            arguments?.getBoolean(PARENTAL_VIEWING_RESTRICTION_UPDATED, false) == true -> {
                arguments?.remove(PARENTAL_VIEWING_RESTRICTION_UPDATED)
                showParentalPinToastForTabletView(
                    R.string.toast_msg_rating_change_success,
                    R.drawable.ic_tick_login_success
                )
            }
            arguments?.getBoolean(PARENTAL_PIN_SETUP_SUCCESS, false) == true -> {
                arguments?.remove(PARENTAL_PIN_SETUP_SUCCESS)
                showParentalPinToastForTabletView(
                    R.string.toast_msg_parental_pin_setup_successful,
                    R.drawable.ic_tick_login_success
                )
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (context?.let { isTablet(it) } == true) {
            binding.toolbarLayout.visibility = View.INVISIBLE
            binding.tvTitle?.visibility = View.VISIBLE
            uiChanges()
        }
        binding.apply {
            groupParentalPinItems.setAllOnClickListener {
                isResultHandled = false
                if (!isDialogOpen || isDeviceTablet){
                    (activity as LandingActivity)?.isParentalPinChanged=false
                    findNavController().navigateSafe(
                        ParentalControlSettingsFragmentDirections.actionGlobalParentalControlBottomDialogFragment(
                            actionBeforeOpeningBottomSheet = ACTION_PIN_CHANGE
                        )
                    )
                }
            }

            groupParentalRatingItems.setAllOnClickListener {
                isResultHandled = false
                if (!isDialogOpen || isDeviceTablet){
                    (activity as LandingActivity)?.isParentalPinChanged=false
                    findNavController().navigateSafe(
                        ParentalControlSettingsFragmentDirections.actionGlobalParentalControlBottomDialogFragment(
                            actionBeforeOpeningBottomSheet = ACTION_RATING_CHANGE
                        )
                    )
                }

            }
        }

        findNavController().addOnDestinationChangedListener(object :
            NavController.OnDestinationChangedListener {
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                controller.currentDestination?.navigatorName?.let {
                    isDialogOpen = it.equals("dialog")

                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        binding.tvPcRatingValue.text = sharedPrefs.getParentalRating()?.ageRatingMasterMapping
            ?: getString(R.string.no_restrictions)
    }

    private fun showParentalPinToastForMobileView(toastMsgId: Int, imageResource: Int) {
        val toastView =
            DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                LayoutInflater.from(context),
                R.layout.layout_toast_success_failure,
                null,
                false
            )
        toastView.textLoginSuccessfulToast.text =
            getString(toastMsgId)
        toastView.imageTickLoginSuccessfulToast.setImageResource(
            imageResource
        )
        showCustomToast(
            context,
            toastView?.root,
            Gravity.FILL_HORIZONTAL
        )
    }

    private fun showParentalPinToastForTabletView(toastMsgId: Int, imageResource: Int) {
        context?.let { if (!isTablet(it)) return }
        val layoutParam = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            context?.let {
                when (getTabletType(it)) {
                    TabletType.TABLET_LANDSCAPE ->
                        setMargins(dpToPx(it, 30), 0, dpToPx(it, 30), 0)
                    else -> Unit
                }
            }
        }
        showToast(
            context = context,
            msz = getString(toastMsgId),
            imgResource = imageResource,
            layoutParam = layoutParam,
            viewGroup = binding.toastContainerFl
        )
        (activity as? LandingActivity)?.isParentalPinChanged =true
    }

    private fun uiChanges(){
        activity?.let {
            if (isTablet(it)) {
                val layoutParams = binding.clParentLayout ?.layoutParams as ConstraintLayout.LayoutParams
                binding.clParentLayout?.layoutParams = layoutParams.apply {
                    marginStart = it.resources.getDimension(R.dimen.tab_padding).toInt()
                    marginEnd =it.resources.getDimension(R.dimen.tab_padding_right).toInt()
                }

            }
        }
    }
}
