package com.tatasky.binge.ui.features.parentalcontrol.sidemenu

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentParentalControlSettingsBinding
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.setAllOnClickListener
import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.*
import com.tatasky.binge.ui.features.sidemenunavdrawer.SideMenuDrawerAnalytics
import com.tatasky.binge.utils.navigateSafe
import com.tatasky.binge.utils.showCustomToast
import javax.inject.Inject

class ParentalControlSettingsFragment :
    BaseFragment<FragmentParentalControlSettingsBinding, ParentalControlViewModel>() {

    @Inject
    lateinit var sideMenuDrawerAnalytics: SideMenuDrawerAnalytics
    private var isResultHandled = false
    private var isDialogOpen = false

    override fun getViewModelClass(): Class<ParentalControlViewModel> =
        ParentalControlViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_parental_control_settings

    override fun getViewModelOwner(): ViewModelStoreOwner =
        findNavController().getViewModelStoreOwner(R.id.nav_parental_control_menu)

    override fun setObserver() {
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
                                    val toastView =
                                        DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                                            LayoutInflater.from(context),
                                            R.layout.layout_toast_success_failure,
                                            null,
                                            false
                                        )
                                    toastView.textLoginSuccessfulToast.text =
                                        getString(R.string.toast_msg_pin_changed_successful)
                                    toastView.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
                                    showCustomToast(
                                        context,
                                        toastView?.root,
                                        Gravity.FILL_HORIZONTAL
                                    )
                                }
                            }
                        }
                        ParentalControlBottomSheetResultStatus.PIN_VERIFIED -> {
                            when (result.actionBeforeOpeningBottomSheet) {
                                ACTION_RATING_CHANGE -> {
                                    viewModel.parentalPinValue = result.pinValue
                                    Handler().postDelayed({
                                        findNavController().navigateSafe(
                                            ParentalControlSettingsFragmentDirections.actionParentalControlSettingsFragmentToParentalControlRatingFragment(
                                                isPCRatingChange = true,
                                            )
                                        )
                                    }, 500)
                                }
                            }
                        }
                    }
                }
            }
    }

    override fun toBeCalledOnce() {
        sideMenuDrawerAnalytics.trackParentalControlSettingView()
        if (!sharedPrefs.isParentalPinExists()) {
            findNavController().navigateSafe(
                ParentalControlSettingsFragmentDirections.actionParentalControlSettingsFragmentToParentalControlRatingFragmentWithPopUpto()
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            groupParentalPinItems.setAllOnClickListener {
                isResultHandled = false
                if(!isDialogOpen)
                findNavController().navigateSafe(
                    ParentalControlSettingsFragmentDirections.actionGlobalParentalControlBottomDialogFragment(
                        actionBeforeOpeningBottomSheet = ACTION_PIN_CHANGE
                    )
                )
            }

            groupParentalRatingItems.setAllOnClickListener {
                isResultHandled = false
                if(!isDialogOpen)
                findNavController().navigateSafe(
                    ParentalControlSettingsFragmentDirections.actionGlobalParentalControlBottomDialogFragment(
                        actionBeforeOpeningBottomSheet = ACTION_RATING_CHANGE
                    )
                )
            }
        }

        findNavController().addOnDestinationChangedListener(object :
            NavController.OnDestinationChangedListener{
            override fun onDestinationChanged(
                controller: NavController,
                destination: NavDestination,
                arguments: Bundle?
            ) {
                controller.currentDestination?.navigatorName?.let {
                    if(it.equals("dialog"))
                        isDialogOpen=true
                    else
                        isDialogOpen=false

                }
            }

        })
    }

    override fun onResume() {
        super.onResume()
        binding.tvPcRatingValue.text = sharedPrefs.getParentalRating()?.ageRatingMasterMapping
            ?: getString(R.string.no_restrictions)
    }
}