package com.tatasky.binge.ui.features.more

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentContactusBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.utils.navigateSafe
import javax.inject.Inject

/**
 * Created by Srikant Karnani on 14/1/20.
 */
class ContactUsFragment : BaseFragment<FragmentContactusBinding, CommonSampleViewModel>() {

    @Inject
    lateinit var moreAnalytics: MoreAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            this.duration = 500
        }
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            this.duration = 500
        }
        returnTransition = backward
        reenterTransition = backward
        exitTransition = forward
    }

    override fun getViewModelClass(): Class<CommonSampleViewModel> =
        CommonSampleViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_contactus

    override fun getViewModelOwner(): ViewModelStoreOwner =
        findNavController().getViewModelStoreOwner(R.id.more)

    override fun setObserver() {
    }

    override fun toBeCalledOnce() {
//        moreAnalytics.trackContactUsVisit()
        binding.tileEmail.settingMenuContainer.setOnClickListener {
            moreAnalytics.trackRaiseRequest()
        }
        binding.tileFaq.settingMenuContainer.setOnClickListener {
            findNavController().navigateSafe(ContactUsFragmentDirections.actionContactUsFragmentToFaqFragment())
        }
        binding.tileEmail.settingMenuContainer.setOnClickListener {
        }
        binding.tileChat.settingMenuContainer.setOnClickListener {
        }
        binding.tileFeedback.settingMenuContainer.setOnClickListener {
        }
    }
}