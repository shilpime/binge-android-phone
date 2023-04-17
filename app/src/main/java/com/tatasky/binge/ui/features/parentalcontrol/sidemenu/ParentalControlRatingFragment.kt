package com.tatasky.binge.ui.features.parentalcontrol.sidemenu

import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentParentalControlRatingBinding
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.ACTION_PIN_CREATE
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.KEY_PARENTAL_CONTROL_BOTTOM_DIALOG_RESULT
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.ParentalControlBottomSheetResult
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.ParentalControlBottomSheetResultStatus
import com.tatasky.binge.utils.navigateSafe
import com.tatasky.binge.utils.showCustomToast
import javax.inject.Inject

class ParentalControlRatingFragment :
    BaseFragment<FragmentParentalControlRatingBinding, ParentalControlViewModel>() {
    private var isResultHandled = false

    private val parentalControlRatingFragmentArgs by navArgs<ParentalControlRatingFragmentArgs>()

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var loginAnalytics: LoginAnalytics

    override fun getViewModelClass(): Class<ParentalControlViewModel> =
        ParentalControlViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_parental_control_rating

    override fun getViewModelOwner(): ViewModelStoreOwner =
        findNavController().getViewModelStoreOwner(R.id.nav_parental_control_menu)

    override fun setObserver() {
        viewModel.ageRatingsResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { response ->
                val selPosition = response.data?.indexOfFirst { data ->
                    data.ageRatingName == sharedPrefs.getParentalRating()?.ageRatingName
                            ?: getString(R.string.no_restrictions)
                }
                viewModel.setParentalControlRatingAdapter(
                    response.data,
                    if (sharedPrefs.isParentalPinExists()) selPosition
                    else null
                )
            }
        }

        viewModel.selectedAgeRatingValue.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { selectedRating ->
                binding.btnProceedFragmentParentalControlRating.isEnabled = true
            }
        }

        viewModel.updateAgeRatingResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let {
                viewModel.selectedAgeRatingValue.value?.peekContent()
                    ?.let { rating -> sharedPrefs.setParentalRating(rating) }
                val toastView =
                    DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                        LayoutInflater.from(context),
                        R.layout.layout_toast_success_failure,
                        null,
                        false
                    )
                toastView.textLoginSuccessfulToast.text =
                    getString(R.string.toast_msg_rating_change_success)
                toastView.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
                showCustomToast(
                    context,
                    toastView?.root,
                    Gravity.FILL_HORIZONTAL
                )

                Handler().postDelayed({
                    findNavController().navigateSafe(
                        ParentalControlRatingFragmentDirections.actionParentalControlRatingFragmentToParentalControlSettingsFragment()
                    )
                }, 500)
            }
        }

        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<ParentalControlBottomSheetResult>(
                KEY_PARENTAL_CONTROL_BOTTOM_DIALOG_RESULT
            )
            ?.observe(viewLifecycleOwner) { result ->
                if (!isResultHandled) {
                    isResultHandled = true
                    when (result.resultStatus) {
                        ParentalControlBottomSheetResultStatus.SUCCESS_DISMISS -> {
                            sharedPrefs.setParentalPinExists(true)
                            viewModel.selectedAgeRatingValue.value?.peekContent()
                                ?.let { sharedPrefs.setParentalRating(it) }

                            val toastView =
                                DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                                    LayoutInflater.from(context),
                                    R.layout.layout_toast_success_failure,
                                    null,
                                    false
                                )
                            toastView.textLoginSuccessfulToast.text =
                                getString(R.string.toast_msg_parental_pin_setup_successful)
                            toastView.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
                            showCustomToast(
                                context,
                                toastView?.root,
                                Gravity.FILL_HORIZONTAL
                            )

                            Handler().postDelayed({
                                findNavController().navigateSafe(
                                    ParentalControlRatingFragmentDirections.actionParentalControlRatingFragmentToParentalControlSettingsFragmentWithPopUpTo()
                                )
                            }, 500)
                        }
                    }
                }
            }
    }

    override fun toBeCalledOnce() {
        binding.viewModel = viewModel
        viewModel.getAgeRatings()
        if(!parentalControlRatingFragmentArgs.isPCRatingChange)
            binding.btnProceedFragmentParentalControlRating.isEnabled = false
        binding.btnProceedFragmentParentalControlRating.text =
            getString(
                if (parentalControlRatingFragmentArgs.isPCRatingChange) R.string.btn_proceed_parental_control_rating
                else R.string.proceed
            )
        binding.btnProceedFragmentParentalControlRating.setOnClickListener {
            loginAnalytics.trackSetRegistrationInitiate(
                viewModel.selectedAgeRatingValue.value?.peekContent()?.ageRatingName ?: ""
            )
            loginAnalytics.trackSetRestrictionProceed(
                viewModel.selectedAgeRatingValue.value?.peekContent()?.ageRatingName ?: ""
            )
            if (viewModel.selectedAgeRatingValue.value?.peekContent()?.ageRatingName == getString(R.string.no_restrictions) && !parentalControlRatingFragmentArgs.isPCRatingChange) {
                viewModel.selectedAgeRatingValue.value?.peekContent()
                    ?.let { rating -> sharedPrefs.setParentalRating(rating) }
                val toastView =
                    DataBindingUtil.inflate<LayoutToastSuccessFailureBinding>(
                        LayoutInflater.from(context),
                        R.layout.layout_toast_success_failure,
                        null,
                        false
                    )
                toastView.textLoginSuccessfulToast.text =
                    getString(R.string.toast_msg_rating_no_restriction)
                toastView.imageTickLoginSuccessfulToast.setImageResource(R.drawable.ic_tick_login_success)
                showCustomToast(
                    context,
                    toastView?.root,
                    Gravity.FILL_HORIZONTAL
                )

                activity?.onBackPressed()
            } else if (parentalControlRatingFragmentArgs.isPCRatingChange) {
                viewModel.updateAgeRating(
                    viewModel.parentalPinValue,
                    viewModel.selectedAgeRatingValue.value?.peekContent()?.ageRatingName
                )
            } else {
                isResultHandled = false
                findNavController().navigateSafe(
                    ParentalControlRatingFragmentDirections.actionGlobalParentalControlBottomDialogFragment(
                        actionBeforeOpeningBottomSheet = ACTION_PIN_CREATE,
                        ageRatingValue = viewModel.selectedAgeRatingValue.value?.peekContent()?.ageRatingName
                    )
                )
            }
        }
    }
}