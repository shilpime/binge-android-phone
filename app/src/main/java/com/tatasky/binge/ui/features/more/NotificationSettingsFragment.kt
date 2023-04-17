package com.tatasky.binge.ui.features.more

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.R
import com.tatasky.binge.analytics.NO
import com.tatasky.binge.analytics.PARA_TRANSACTION_NOTIFICATION
import com.tatasky.binge.analytics.PARA_WATCH_NOTIFICATION
import com.tatasky.binge.analytics.YES
import com.tatasky.binge.databinding.FragmentNotificationSettingsBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.notifications.NotificationAnalytics
import com.tatasky.binge.utils.TRANSACTIONAL_NOTI_SETTINGS_KEY
import com.tatasky.binge.utils.WATCH_NOTI_SETTINGS_KEY
import java.util.*
import javax.inject.Inject

/**
 * Created by Srikant Karnani on 14/1/20.
 */
class NotificationSettingsFragment :
    BaseFragment<FragmentNotificationSettingsBinding, SettingsViewModel>(),
    View.OnClickListener {
    @Inject
    lateinit var analytics: NotificationAnalytics

    @Inject
    lateinit var moreAnalytics: MoreAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            duration=500
        }
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            duration=500
        }
        returnTransition = backward
    }

    override fun getViewModelClass(): Class<SettingsViewModel> =
        SettingsViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_notification_settings

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.swTransactionNotification.isChecked =
            viewModel.sharedPrefs.getTransactionalNotificationAllowed()
        binding.swWatchNotification.isChecked = viewModel.sharedPrefs.getWatchNotificationAllowed()
        binding.swOffersNotification.isChecked =
            viewModel.sharedPrefs.getOffersNotificationAllowed()

        binding.cbTransactionNotification.isChecked =
            viewModel.sharedPrefs.getBingeUpdateNotification()

        binding.cbOffersNotification.isChecked =
            viewModel.sharedPrefs.getBingeOffersNotification()
        binding.cbSurveyNotification.isChecked =
            viewModel.sharedPrefs.getBingeSurveyNotification()
    }
    override fun setObserver() {
        viewModel.getToggledSetting().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                when (it) {
                    TRANSACTIONAL_NOTI_SETTINGS_KEY -> {
                        viewModel.sharedPrefs.setAllowTransactionalNotification(!binding.swTransactionNotification.swNotification.isChecked)
                        binding.swTransactionNotification.swNotification.isChecked = !binding.swTransactionNotification.swNotification.isChecked
                        moreAnalytics.trackNotificationSettingsChanged(PARA_TRANSACTION_NOTIFICATION, if(binding.swTransactionNotification.swNotification.isChecked) YES else NO)
                    }
                    WATCH_NOTI_SETTINGS_KEY -> {
                        viewModel.sharedPrefs.setAllowWatchNotification(!binding.swWatchNotification.swNotification.isChecked)
                        binding.swWatchNotification.swNotification.isChecked = !binding.swWatchNotification.swNotification.isChecked
                        moreAnalytics.trackNotificationSettingsChanged(PARA_WATCH_NOTIFICATION, if(binding.swWatchNotification.swNotification.isChecked) YES else NO)
                    }
                }
            }
        })
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.cbTransactionNotification.cbNotification -> {
                viewModel.sharedPrefs.setBingeUpdateNotification((v as CompoundButton).isChecked)
            }
            binding.cbOffersNotification.cbNotification -> {
                viewModel.sharedPrefs.setBingeOffersNotification((v as CompoundButton).isChecked)
            }
            binding.cbSurveyNotification.cbNotification -> {
                viewModel.sharedPrefs.setBingeSurveyNotification((v as CompoundButton).isChecked)
            }
        }
    }

    override fun toBeCalledOnce() {
        binding.cbTransactionNotification.cbNotification.setOnClickListener(this)
        binding.cbOffersNotification.cbNotification.setOnClickListener(this)
        binding.cbSurveyNotification.cbNotification.setOnClickListener(this)

        binding.swTransactionNotification.swNotification.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                binding.swTransactionNotification.swNotification.isClickable = false
                viewModel.toggleSetting(TRANSACTIONAL_NOTI_SETTINGS_KEY)
                return false
            }
        })
        binding.swWatchNotification.swNotification.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                binding.swWatchNotification.swNotification.isClickable = false
                viewModel.toggleSetting(WATCH_NOTI_SETTINGS_KEY)
                return false
            }
        })
        binding.swOffersNotification.swNotification.setOnClickListener(this)
        binding.tvCommunicationInfo.text = String.format(
            Locale.getDefault(),
            getString(R.string.communication_sent_to_message),
            viewModel.sharedPrefs.getSelectedProfile()?.emailId
        )
    }
}