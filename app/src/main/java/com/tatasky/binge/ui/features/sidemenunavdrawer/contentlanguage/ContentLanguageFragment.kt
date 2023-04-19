package com.tatasky.binge.ui.features.sidemenunavdrawer.contentlanguage

import android.content.res.Configuration
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelStoreOwner
import com.tatasky.binge.R
import com.tatasky.binge.analytics.APPLAUNCH
import com.tatasky.binge.analytics.NUDGE
import com.tatasky.binge.data.networking.models.response.Verbiages
import com.tatasky.binge.databinding.FragmentContentLanguageBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.home.bottomsheet.HomeBottomSheetViewModel
import com.tatasky.binge.ui.features.sidemenunavdrawer.SideMenuDrawerAnalytics
import com.tatasky.binge.utils.*
import javax.inject.Inject

class ContentLanguageFragment :
    BaseFragment<FragmentContentLanguageBinding, HomeBottomSheetViewModel>() {

    private var subHeader: Verbiages? = null

    @Inject
    lateinit var sideMenuDrawerAnalytics: SideMenuDrawerAnalytics

    override fun getViewModelClass(): Class<HomeBottomSheetViewModel> {
        return HomeBottomSheetViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_content_language
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return this
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        uiChanges()
    }
    private fun uiChanges() {
        val layoutParams= binding.rvLanguageListing?.layoutParams as ConstraintLayout.LayoutParams
        val layoutParams1= binding.textView?.layoutParams as ConstraintLayout.LayoutParams
        val layoutParams2= binding.tvLanguageHeader?.layoutParams as ConstraintLayout.LayoutParams
        activity?.let {
           layoutParams.apply {
               layoutParams.marginStart = it.resources.getDimensionPixelSize(R.dimen.tab_padding)
               layoutParams1.marginStart = it.resources.getDimensionPixelSize(R.dimen.tab_padding)
               layoutParams.marginEnd  = it.resources.getDimensionPixelSize(R.dimen.tab_padding_right)
           }
        }
        binding.rvLanguageListing?.layoutParams=layoutParams
        binding.textView?.layoutParams=layoutParams1
        binding.tvLanguageHeader?.layoutParams=layoutParams2
    }

    override fun setObserver() {
        viewModel.getLanguagePopulateCallback().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { _ ->
                binding.btnSelectLang.show()
                if(subHeader?.data?.subHeader?.isNullOrEmpty() == false){
                    binding.tvLanguageHeader.show()
                }
            }
        }
        viewModel.showToast().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { message ->
                showToast(context, message)
            }
        }

        viewModel.getLanguageAdapter().getSelectContentLanguageButtonStatus().observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { isEnabled ->
                binding.btnSelectLang.isEnabled = isEnabled
            }
        }
    }

    override fun toBeCalledOnce() {
        if (context?.let { isTablet(it) } == true)
        {
            binding.toolbarLayout.visibility = View.INVISIBLE
            binding.textView?.visibility =View.VISIBLE
        }
        sideMenuDrawerAnalytics.trackSelectContentLanguageView()
        binding.vm = viewModel
        viewModel.fetchLanguages(this.javaClass.name)
        viewModel.getLanguageAdapter().setIsSaveLanguageButtonAvailable(true)
        subHeader = sharedPrefs.getConfigResponse()?.data?.config?.getLanguageVerbiage(
            CATEGORY_LANGUAGE_SETTING
        )
        subHeader?.let {
            if(it.data.others.buttonTitle.isNullOrEmpty()){
                it.data.others.buttonTitle = getString(R.string.proceed)
            }
            if(it.data.header.isNullOrEmpty()){
                it.data.header = getString(R.string.select_content_language)
            }
        }
        binding.verbiage = subHeader

        binding.btnSelectLang.setOnClickListener {
            viewModel.saveLanguages(showErrorOnEmptySelection = false) {
                startHomeScreen(activity)
            }
        }
    }


}
