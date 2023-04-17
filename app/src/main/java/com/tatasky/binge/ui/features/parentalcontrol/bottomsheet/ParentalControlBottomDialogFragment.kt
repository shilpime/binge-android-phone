package com.tatasky.binge.ui.features.parentalcontrol.bottomsheet

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentParentalControlBottomDialogBinding
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.ui.base.frameworks.extensions.startProgressAvd
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.parentalcontrol.ParentalControlViewModel
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

const val KEY_PARENTAL_CONTROL_BOTTOM_DIALOG_RESULT = "parentalControlBottomDialogResult"

const val ACTION_RATING_CHANGE = "action_rating_change"
const val ACTION_PIN_CREATE = "action_pin_create"
const val ACTION_PIN_CHANGE = "action_pin_change"
const val ACTION_PIN_VERIFICATION = "action_pin_verification"
const val ACTION_PIN_FORGOT = "action_pin_forgot"

class ParentalControlBottomDialogFragment : BottomSheetDialogFragment() {

    @Inject
    lateinit var sharedPrefs: PrefsRepo

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    private lateinit var mBinding: FragmentParentalControlBottomDialogBinding
    private lateinit var mViewModel: ParentalControlViewModel
    private val parentalControlBottomDialogFragmentArgs by navArgs<ParentalControlBottomDialogFragmentArgs>()

    private var isLoaded: Boolean = false
    private val mHandler = Handler(Looper.getMainLooper())
    private var showProgress: Runnable = Runnable { }
    var loaderDelayTime = 0L

    private lateinit var standardBottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentParentalControlBottomDialogBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(
                            KEY_PARENTAL_CONTROL_BOTTOM_DIALOG_RESULT,
                            ParentalControlBottomSheetResult(
                                null,
                                null,
                                parentalControlBottomDialogFragmentArgs.actionBeforeOpeningBottomSheet
                            )
                        )
                    isEnabled = false
                    activity?.onBackPressed()
                }
            })
        if (!isLoaded) {
            hideProgress()
            isLoaded = true
        }

        loaderDelayTime = sharedPrefs.getLoaderDelayTime()
        showProgress = Runnable {
            showProgress()
        }

        mViewModel.progressListener.observe(viewLifecycleOwner) {
            if (it) {
                if (!allowedTouchWhenLoading()) {
                    mBinding.progressBarOverlay.visibility = View.VISIBLE
                    mBinding.root.closeKeyboard()
                }

                mHandler.postDelayed(
                    showProgress, loaderDelayTime
                )
            } else {
                mHandler.removeCallbacks(showProgress)
                hideProgress()
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(
            this,
            mViewModelFactory
        )[ParentalControlViewModel::class.java]

        mViewModel.actionBeforeOpeningBottomSheet =
            parentalControlBottomDialogFragmentArgs.actionBeforeOpeningBottomSheet

        mViewModel.ageRatingValue = parentalControlBottomDialogFragmentArgs.ageRatingValue

        mViewModel.fromNudge = parentalControlBottomDialogFragmentArgs.fromNudge
        mViewModel.pinEntrySource = parentalControlBottomDialogFragmentArgs.source

        mBinding.progressBarOverlay.setOnTouchListener { _, _ -> true }

        standardBottomSheetBehavior = (dialog as BottomSheetDialog).behavior
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        mViewModel.parentalControlBottomDialogResult.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let { result ->
                setResultAndDismissBottomSheet(result)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(showProgress)
        view?.closeKeyboard()
    }

    override fun getTheme(): Int {
        return R.style.GuestLoginBottomSheetDialogTheme
    }

    private fun allowedTouchWhenLoading() = false

    private fun showProgress() {
        mBinding.progressBar.startProgressAvd(true)
    }

    private fun hideProgress() {
        mBinding.progressBar.startProgressAvd(false)
        mBinding.progressBarOverlay.visibility = View.GONE
    }

    private fun setResultAndDismissBottomSheet(result: String) {
        dialog?.dismiss()
        val pinValue = if (result == ParentalControlBottomSheetResultStatus.PIN_VERIFIED)
            mViewModel.parentalPinValue
        else
            null

        if (mViewModel.fromNudge == true) {
            findNavController().getBackStackEntry(findNavController().graph.startDestination)?.savedStateHandle
                ?.set(
                    KEY_PARENTAL_CONTROL_BOTTOM_DIALOG_RESULT,
                    ParentalControlBottomSheetResult(
                        result,
                        pinValue,
                        mViewModel.actionBeforeOpeningBottomSheet,
                        mViewModel.fromNudge
                    )
                )
        } else {
            findNavController().previousBackStackEntry
                ?.savedStateHandle
                ?.set(
                    KEY_PARENTAL_CONTROL_BOTTOM_DIALOG_RESULT,
                    ParentalControlBottomSheetResult(
                        result,
                        pinValue,
                        mViewModel.actionBeforeOpeningBottomSheet,
                        mViewModel.fromNudge
                    )
                )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        mViewModel.setProgressing(false)
        super.onDismiss(dialog)
    }
}