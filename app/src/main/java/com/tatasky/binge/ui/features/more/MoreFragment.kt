//package com.tatasky.binge.ui.features.more
//
//import android.os.Bundle
//import android.view.MotionEvent
//import android.view.View
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelStoreOwner
//import androidx.navigation.Navigation
//import androidx.navigation.fragment.findNavController
//import com.google.android.material.transition.MaterialSharedAxis
//import com.tatasky.binge.BuildConfig
//import com.tatasky.binge.R
//import com.tatasky.binge.analytics.NO
//import com.tatasky.binge.databinding.FragmentMoreBinding
//import com.tatasky.binge.interfaces.CommonDialogEventListener
//import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
//import com.tatasky.binge.ui.features.dialog.DialogModel
//import com.tatasky.binge.utils.AUTO_PLAY_TRAILER_SETTINGS_KEY
//import com.tatasky.binge.utils.DeviceInfoUtils
//import com.tatasky.binge.utils.logoutApplication
//import com.tatasky.binge.utils.navigateSafe
//import java.util.*
//import javax.inject.Inject
//
///**
// * Created by Srikant Karnani on 3/1/20.
// */
//class MoreFragment : BaseFragment<FragmentMoreBinding, SettingsViewModel>() {
//
//    @Inject
//    lateinit var moreAnalytics: MoreAnalytics
//
//    override fun getViewModelClass(): Class<SettingsViewModel> = SettingsViewModel::class.java
//
//    override fun layoutId(): Int = R.layout.fragment_more
//
//    override fun getViewModelOwner(): ViewModelStoreOwner =
//        findNavController().getViewModelStoreOwner(R.id.more)
//
//    override fun onResume() {
//        super.onResume()
//        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
//            this.duration = 500
//        }
//        reenterTransition = backward
//
//        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
//            this.duration = 500
//        }
//        exitTransition = forward
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
//            duration=500
//        }
//        reenterTransition = backward
//
//        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
//            duration=500
//        }
//        exitTransition = forward
//    }
//
//    override fun setObserver() {
//        viewModel.getSignoutResponse().observe(requireActivity(), Observer {
//            it.getContentIfNotHandled()?.let {
//                moreAnalytics.trackLogout()
//                activity?.let { act ->
//                    context?.let {
//                        localBroadcastHelper.unregisterBroadcast(it, mLogoutListener)
//                    }
//                    logoutApplication(act)
//                }
//            }
//        })
//        viewModel.getToggledSetting().observe(viewLifecycleOwner, Observer {
//            it.getContentIfNotHandled()?.let {
//                viewModel.sharedPrefs.setAutoPlayTrailerOn(!binding.tileAutoPlayTrailer.swSettings.isChecked)
//                binding.tileAutoPlayTrailer.swSettings.isChecked = !binding.tileAutoPlayTrailer.swSettings.isChecked
//                moreAnalytics.trackAutoPlaySettingsChanged(if(binding.tileAutoPlayTrailer.swSettings.isChecked) "YES" else NO)
//            }
//        })
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//        binding.lifecycleOwner = viewLifecycleOwner
//        binding.autoPlayOn = viewModel.sharedPrefs.getAutoPlayTrailerOn()
//    }
//
//    override fun toBeCalledOnce() {
//        moreAnalytics.trackMoreVisit()
//        binding.tileAutoPlayTrailer.swSettings.setOnTouchListener(object : View.OnTouchListener {
//            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                binding.tileAutoPlayTrailer.swSettings.isClickable = false
//                viewModel.toggleSetting(AUTO_PLAY_TRAILER_SETTINGS_KEY)
//                return false
//            }
//        })
//        binding.version = String.format(
//            Locale.getDefault(),
//            getString(R.string.version_info),
//            BuildConfig.VERSION_NAME
//        )
//        binding.tileContactUs.settingMenuContainer.setOnClickListener(
//            Navigation.createNavigateOnClickListener(
//                MoreFragmentDirections.actionMoreFragmentToContactUsFragment()
//            )
//        )
//        binding.tileNotification.settingMenuContainer.setOnClickListener(
//            Navigation.createNavigateOnClickListener(
//                MoreFragmentDirections.actionMoreFragmentToNotificationSettingsFragment()
//            )
//        )
//        binding.tilePolicy.settingMenuContainer.setOnClickListener {
//            findNavController().navigateSafe(
//                MoreFragmentDirections.actionMoreFragmentToPrivacyPolicyFragment()
//            )
//        }
//        binding.tileTNC.settingMenuContainer.setOnClickListener {
//            findNavController().navigateSafe(
//                MoreFragmentDirections.actionMoreFragmentToTermsConditionFragment()
//            )
//        }
//
//        binding.btnSignOut.setOnClickListener {
//            showDialog(DialogModel(
//                    false,
//                    R.drawable.ic_subscription_error,
//                    getString(R.string.signout),
//                    getString(R.string.yes),
//                    getString(R.string.no),
//                    getString(R.string.logout_confirmation)
//            ),
//                    object : CommonDialogEventListener {
//                        override fun onPrimaryButtonClick() {
//                            hideDialog()
//                            viewModel.deviceId = DeviceInfoUtils.getDeviceId(requireContext())
//                            viewModel.removeDeviceAndSignout()
//                        }
//
//                        override fun onSecondaryButtonClick() {
//                            hideDialog()
//                        }
//
//                        override fun onCloseButtonClick() {
//                            hideDialog()
//                        }
//                    }
//            )
//        }
//    }
//}