package com.tatasky.binge.ui.features.parentalcontrol.sidemenu

import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentParentalControlRatingBinding
import com.tatasky.binge.databinding.LayoutToastSuccessFailureBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.onboarding.login.LoginAnalytics
import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.ACTION_PIN_CREATE
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.KEY_PARENTAL_CONTROL_BOTTOM_DIALOG_RESULT
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.ParentalControlBottomSheetResult
import com.tatasky.binge.ui.features.parentalcontrol.bottomsheet.ParentalControlBottomSheetResultStatus
import com.tatasky.binge.utils.*
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
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        uiChanges()
    }

    private fun uiChanges() {
        val layoutParams= binding.clParentLayout?.layoutParams as ConstraintLayout.LayoutParams
        activity?.let {
            binding.clParentLayout?.layoutParams=  layoutParams.apply {
               layoutParams.marginStart = it.resources.getDimensionPixelSize(R.dimen.tab_padding)
               layoutParams.marginEnd  = it.resources.getDimensionPixelSize(R.dimen.tab_padding_right)
           }
        }
    }

    override fun getViewModelOwner(): ViewModelStoreOwner =if(isTablet(requireContext()))
    {
      activity as LandingActivity
    }else
    {
        findNavController().getViewModelStoreOwner(R.id.nav_parental_control_menu)
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
                context?.let { ctx ->
                    if (!isTablet(ctx)) {
                        showParentalPinToastForMobileView(
                            R.string.toast_msg_rating_change_success,
                            R.drawable.ic_tick_login_success
                        )
                    }
                }

                Handler().postDelayed({

                    context?.let {
                        if (isTablet(it))
                        {
                            sharedPrefs.sethandleRatingScreenTablet("yes")
                            binding.SidePaneContainer.visibility =View.VISIBLE
                            binding.clParentLayout.visibility =View.GONE
                            var childFragment = ParentalControlSettingsFragment()
                            childFragment.arguments = Bundle().apply {
                                putBoolean(
                                    PARENTAL_VIEWING_RESTRICTION_UPDATED,
                                    true
                                )
                            }
                            var transaction =
                                childFragmentManager.beginTransaction()
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            transaction.replace(R.id.SidePaneContainer, childFragment).commit()
                        }
                        else
                        {
                            findNavController().navigateSafe(
                                ParentalControlRatingFragmentDirections.actionParentalControlRatingFragmentToParentalControlSettingsFragment()
                            )
                        }
                    }

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
                            context?.let { ctx ->
                                if (!isTablet(ctx)) {
                                    showParentalPinToastForMobileView(
                                        R.string.toast_msg_parental_pin_setup_successful,
                                        R.drawable.ic_tick_login_success
                                    )
                                }
                            }
                            Handler().postDelayed({

                                context?.let {
                                    if (isTablet(it))
                                    {
                                        sharedPrefs.sethandleRatingScreenTablet("yes")
                                        binding.SidePaneContainer.visibility =View.VISIBLE
                                        binding.clParentLayout.visibility =View.GONE
                                        val childFragment = ParentalControlSettingsFragment()
                                        childFragment.arguments = Bundle().apply {
                                            putBoolean(
                                                PARENTAL_PIN_SETUP_SUCCESS,
                                                true
                                            )
                                        }
                                        val transaction =
                                            childFragmentManager.beginTransaction()
                                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                        transaction.replace(R.id.SidePaneContainer, childFragment).commit()
                                    }
                                    else
                                    {
                                        findNavController().navigateSafe(
                                            ParentalControlRatingFragmentDirections.actionParentalControlRatingFragmentToParentalControlSettingsFragmentWithPopUpTo()
                                        )
                                    }
                                }


                            }, 500)
                        }
                    }
                }
            }
    }

    override fun toBeCalledOnce() {
        if (context?.let { isTablet(it) } == true)
        {
            binding.toolbarLayout.visibility = View.INVISIBLE
        }
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
                showParentalPinToastForMobileView(
                    R.string.toast_msg_rating_no_restriction,
                    R.drawable.ic_tick_login_success
                )
                activity?.onBackPressed()
            } else if (parentalControlRatingFragmentArgs.isPCRatingChange) {

                context?.let {
                    if (isTablet(it))
                    {
                        viewModel.updateAgeRating(
                            sharedPrefs.getParentalPin(),
                            viewModel.selectedAgeRatingValue.value?.peekContent()?.ageRatingName
                        )
                    }
                    else
                    {
                        viewModel.updateAgeRating(
                            viewModel.parentalPinValue,
                            viewModel.selectedAgeRatingValue.value?.peekContent()?.ageRatingName
                        )
                    }
                }

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
