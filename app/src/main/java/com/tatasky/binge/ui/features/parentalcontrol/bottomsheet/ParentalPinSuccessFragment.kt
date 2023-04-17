package com.tatasky.binge.ui.features.parentalcontrol.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentParentalPinSuccessBinding
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.GuestLoginViewModel
import com.tatasky.binge.ui.features.onboarding.login.bottomsheet.temp.GuestLoginBottomSheetResult
import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
import javax.inject.Inject

class ParentalPinSuccessFragment :
    BaseFragment<FragmentParentalPinSuccessBinding, GuestLoginViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    private lateinit var parentalControlViewModel: ParentalControlViewModel

    override fun getViewModelClass(): Class<GuestLoginViewModel> =
        GuestLoginViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_parental_pin_success

    override fun getViewModelOwner(): ViewModelStoreOwner =
        requireParentFragment().requireParentFragment()

    override fun setObserver() {
        parentalControlViewModel.progressListener.observe(viewLifecycleOwner) {
            viewModel.setProgressing(it)
        }
    }

    override fun toBeCalledOnce() {
        parentalControlViewModel =
            ViewModelProvider(
                requireParentFragment().requireParentFragment(),
                mViewModelFactory
            )[ParentalControlViewModel::class.java]
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        /*overriding loader set on activity via BaseFragment*/
        showProgress = Runnable { }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            btnProceedParentalPinSuccess.setOnClickListener {
                //turn on parental pin and dismiss
                sharedPrefs.setParentalControlEnabled(true)
                viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.SUCCESS))
            }

            btnCancelParentalPinSuccess.setOnClickListener {
                //dismiss without turning on parental pin
                viewModel.guestLoginResult.postValue(SingleEvent(GuestLoginBottomSheetResult.SUCCESS))
            }
        }
    }
}