package com.tatasky.binge.ui.features.onboarding.login.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.tatasky.binge.R
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.UsedMobileNumber
import com.tatasky.binge.databinding.FragmentGuestLoginPreviouslyUsedMobileBinding
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.temp.GuestLoginBottomSheetResult
import com.tatasky.binge.utils.navigateSafe
import javax.inject.Inject

class UsedRMNListingFragment :
    BaseFragment<FragmentGuestLoginPreviouslyUsedMobileBinding, GuestLoginViewModel>() {

    private var selectedPreviousUsedMobileDetail: UsedMobileNumber? = null
    @Inject
    lateinit var loginAnalytics: LoginAnalytics

    private fun validate() {
        if (viewModel.rmn.length == 10) {
            loginAnalytics.trackOtpInvoked(
                viewModel.loginType,
                viewModel.loginAuth,
                viewModel.rmn,
                viewModel.loginSource
            )
            viewModel.generateOtp()
        }
    }

    override fun getViewModelClass(): Class<GuestLoginViewModel> =
        GuestLoginViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_guest_login_previously_used_mobile

    override fun getViewModelOwner(): ViewModelStoreOwner =
        requireParentFragment().requireParentFragment()

    override fun setObserver() {
        viewModel.generateOtpResponseError.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { errorModel ->

                // Remove the value from key if there is any error
                // So that when user clicks on New login
                // It will show RMN field instead of prefilled entry
                findNavController().previousBackStackEntry
                    ?.savedStateHandle
                    ?.remove<UsedMobileNumber>(KEY_PREVIOUSLY_SELECTED_MOBILE)

                when (errorModel.code) {
                    20090 -> onError(errorModel)
                    else -> onError(errorModel)
                }
            }
        }

        viewModel.generateOtpResponse.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { generateOTPResponse ->
                loginAnalytics.trackGetOtp()
                selectedPreviousUsedMobileDetail?.let { selectedRmnDetails ->
                    findNavController().navigateSafe(
                        UsedRMNListingFragmentDirections
                            .actionGuestLoginPreviouslyUsedMobileFragmentToGuestLoginFragment(selectedRmnDetails)
                    )
                } ?: run {
                    onError(ErrorModel(message = getString(R.string.select_one_bmid)))
                }
            }
        }

        viewModel.previouslyUsedMobileNumberLiveData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { previouslyUsedMobileNumber ->
                selectedPreviousUsedMobileDetail = previouslyUsedMobileNumber
            }
        }
    }

    override fun toBeCalledOnce() {
        if (viewModel.previouslyUsedMobileNumberList.isNotEmpty())
            viewModel.guestLoginPreviouslyUsedMobileAdapter.updateList(viewModel.previouslyUsedMobileNumberList)
        else
            findNavController().navigateSafe(
                UsedRMNListingFragmentDirections
                    .actionGuestLoginPreviouslyUsedMobileFragmentToGuestLoginFragment(null),
                NavOptions.Builder()
                    .setPopUpTo(R.id.guestLoginFragment, false)
                    .build()
            )

        binding.viewModel = viewModel

        if (viewModel.isParentalPinSetupRequested) {
            binding.header.tvHeaderTitle.text = getString(R.string.header_title_parental_pin_setup)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*overriding loader set on activity via BaseFragment*/
        showProgress = Runnable { }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnProceedGuestLoginPreviouslyUsedMobile.setOnClickListener {
            selectedPreviousUsedMobileDetail?.let {
                viewModel.rmn = it.mobileNumber ?: ""
                validate()
            } ?: run {
                onError(ErrorModel(message = getString(R.string.select_one_bmid)))
            }
        }

        binding.newLoginBtn.setOnClickListener {
            loginAnalytics.trackLoginPageNewLogin()
            findNavController().navigateSafe(
                UsedRMNListingFragmentDirections.actionGuestLoginPreviouslyUsedMobileFragmentToGuestLoginFragment()
            )
        }
    }
}