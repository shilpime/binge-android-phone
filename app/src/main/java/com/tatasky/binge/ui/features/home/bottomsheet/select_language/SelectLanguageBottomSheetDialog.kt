package com.tatasky.binge.ui.features.home.bottomsheet.select_language

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tatasky.binge.R
import com.tatasky.binge.analytics.APPLAUNCH
import com.tatasky.binge.analytics.NUDGE
import com.tatasky.binge.data.networking.models.response.VerbiageData
import com.tatasky.binge.data.networking.models.response.Verbiages
import com.tatasky.binge.databinding.FragmentSelectLanguageBottomSheetDialogBinding
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.ui.base.frameworks.extensions.startProgressAvd
import com.tatasky.binge.ui.features.home.HomeAnalytics
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.home.bottomsheet.HomeBottomSheetViewModel
import com.tatasky.binge.utils.CATEGORY_LANGUAGE_DRAWER
import com.tatasky.binge.utils.showToastOverDialog
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class SelectLanguageBottomSheetDialog : BottomSheetDialogFragment() {

    @Inject
    lateinit var homeAnalytics: HomeAnalytics
    @Inject
    lateinit var sharedPrefs: PrefsRepo

    @Inject
    lateinit var mViewModelFactory: ViewModelProvider.Factory
    lateinit var mBinding: FragmentSelectLanguageBottomSheetDialogBinding

    private var isLoaded: Boolean = false
    private val mHandler = Handler(Looper.getMainLooper())
    private var showProgress: Runnable = Runnable { }
    private var loaderDelayTime = 0L

    private var mBottomSheetBehavior: BottomSheetBehavior<*>? = null
    private lateinit var mHomeBottomSheetViewModel: HomeBottomSheetViewModel
    private var mDisableLanguageRecyclerListener = object: RecyclerView.OnItemTouchListener{
        override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
            if(mBottomSheetBehavior?.state != BottomSheetBehavior.STATE_EXPANDED) {
                mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                return true
            }
            return false
        }

        override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

        }

        override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

        }

    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mBinding =
            FragmentSelectLanguageBottomSheetDialogBinding.inflate(inflater, container, false)
        mBinding.lifecycleOwner = viewLifecycleOwner
        mHomeBottomSheetViewModel = ViewModelProvider(
            requireActivity(),
            mViewModelFactory
        )[HomeBottomSheetViewModel::class.java]
        mBinding.vm = mHomeBottomSheetViewModel
        return mBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val verbiage = sharedPrefs.getConfigResponse()?.data?.config?.getLanguageVerbiage(
            CATEGORY_LANGUAGE_DRAWER
        )
        verbiage?.let{
            if(verbiage.data.others.buttonTitle.isNullOrEmpty()){
                verbiage.data.others.buttonTitle = getString(R.string.proceed)
            }
            if(verbiage.data.header.isNullOrEmpty()){
                verbiage.data.header = getString(R.string.select_content_language)
            }
        }
        mBinding.verbiage =
            verbiage

        mBinding.rvLanguageListing.addOnItemTouchListener(mDisableLanguageRecyclerListener)
        if (!isLoaded) {
            hideProgress()
            isLoaded = true
        }

        loaderDelayTime = sharedPrefs.getLoaderDelayTime()
        showProgress = Runnable {
            showProgress()
        }

        mHomeBottomSheetViewModel.progressListener.observe(viewLifecycleOwner) {
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

        dialog?.setOnShowListener { dialog ->
            (dialog as BottomSheetDialog)
                .findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                ?.let { bottomSheetInternal ->
                    mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetInternal)
                    mBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                    val bottomBgBlurMargin =
                        bottomSheetInternal.height - (mBottomSheetBehavior?.peekHeight ?: 0)
                    val newBgBlurLayoutParams =
                        mBinding.bgBlur.layoutParams as ConstraintLayout.LayoutParams
                    newBgBlurLayoutParams.bottomMargin = bottomBgBlurMargin
                    mBinding.bgBlur.layoutParams = newBgBlurLayoutParams
                    mBottomSheetBehavior?.addBottomSheetCallback(object :
                        BottomSheetBehavior.BottomSheetCallback() {

                        override fun onSlide(bottomSheet: View, slideOffset: Float) {
                            newBgBlurLayoutParams.bottomMargin =
                                bottomBgBlurMargin - (bottomBgBlurMargin * slideOffset).toInt()
                            mBinding.bgBlur.layoutParams = newBgBlurLayoutParams

                        }

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        when (newState) {
                            BottomSheetBehavior.STATE_COLLAPSED -> Log.i("states","STATE_COLLAPSED")
                            BottomSheetBehavior.STATE_EXPANDED -> {
                                /*homeAnalytics.trackContentLanguageBottomSheetExpand()*/
                                Log.i("states", "STATE_EXPANDED")
                            }
                            BottomSheetBehavior.STATE_DRAGGING -> Log.i("states","STATE_DRAGGING")
                            BottomSheetBehavior.STATE_SETTLING -> Log.i("states","STATE_SETTLING")
                            BottomSheetBehavior.STATE_HIDDEN -> Log.i("states","STATE_HIDDEN")
                            else -> Log.i("states","OTHER_STATE")
                        }
                    }
                })

            }
                   }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //todo: discuss with shilpi if we can do it in improved way
        mBinding.progressBarOverlay.setOnTouchListener { _, _ -> true }
        homeAnalytics.trackContentLanguageBottomSheetOpen(
            if (sharedPrefs.isFirstTimeLanguagePopUpShown()) APPLAUNCH else NUDGE
        )
        setListeners()
        setObservers()

    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacks(showProgress)
        view?.closeKeyboard()
    }

    private fun setObservers() {
        mHomeBottomSheetViewModel.showToast().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { message ->
                dialog?.let { it1 -> showToastOverDialog(it1,message) }
            }
        })
        mHomeBottomSheetViewModel.getLanguageAdapter().getSelectContentLanguageButtonStatus()
            .observe(viewLifecycleOwner, Observer {
                it.getContentIfNotHandled()?.let { isEnabled ->
                    mBinding.btnSelectLang.isEnabled = isEnabled
                }
            })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        mBinding.btnSelectLang.setOnClickListener {
            //after one or more language selection it will trigger
            mHomeBottomSheetViewModel.saveLanguages {
                sharedPrefs.getPrefLanguages().takeIf { it.isNotEmpty() }?.let {
                    homeAnalytics.trackContentLanguageSelected(
                        source = if (sharedPrefs.isFirstTimeLanguagePopUpShown()) APPLAUNCH else NUDGE,
                        it.getOrNull(0),
                        it.getOrNull(1),
                        it.getOrNull(2),
                        it.getOrNull(3)
                    )
                }
                dismiss()
            }

        }
        mBinding.tvNotNow.setOnClickListener {
            homeAnalytics.trackContentLanguageBottomSheetSkip()
            dismiss()
        }
        mBinding.bgBlur.setOnTouchListener { _, _ ->
            true
        }
    }

    override fun getTheme(): Int {
        return R.style.AppBottomSheetDialogTheme
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mHomeBottomSheetViewModel.getLanguageAdapter().resetAdapter()
        if (mHomeBottomSheetViewModel.sharedPrefs.isFirstTimeLanguagePopUpShown()) {
            mHomeBottomSheetViewModel.sharedPrefs.setFirstTimeLanguagePopUpShown(false)
            mHomeBottomSheetViewModel.refreshLanguageWidget()
        }
    }

    private fun allowedTouchWhenLoading() = false

    private fun showProgress() {
        mBinding.progressBar.startProgressAvd(true)
    }

    private fun hideProgress() {
        mBinding.progressBar.startProgressAvd(false)
        mBinding.progressBarOverlay.visibility = View.GONE
    }
}