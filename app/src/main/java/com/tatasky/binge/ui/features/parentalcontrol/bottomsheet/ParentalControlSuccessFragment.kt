package com.tatasky.binge.ui.features.parentalcontrol.bottomsheet

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentParentalControlSuccessBinding
import com.tatasky.binge.ui.base.frameworks.SingleEvent
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
import javax.inject.Inject

class ParentalControlSuccessFragment :
    BaseFragment<FragmentParentalControlSuccessBinding, ParentalControlViewModel>() {

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory

    override fun getViewModelClass(): Class<ParentalControlViewModel> =
        ParentalControlViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_parental_control_success

    override fun getViewModelOwner(): ViewModelStoreOwner =
        requireParentFragment().requireParentFragment()

    override fun setObserver() {

    }

    override fun toBeCalledOnce() {

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
                sharedPrefs.setParentalControlEnabled(true)
                viewModel.parentalControlBottomDialogResult.postValue(
                    SingleEvent(
                        ParentalControlBottomSheetResultStatus.SUCCESS_DISMISS
                    )
                )
            }

            btnCancelParentalPinSuccess.setOnClickListener {
                viewModel.parentalControlBottomDialogResult.postValue(
                    SingleEvent(
                        ParentalControlBottomSheetResultStatus.SUCCESS_DISMISS
                    )
                )
            }
        }
    }
}