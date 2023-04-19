package com.tatasky.binge.ui.features.device_management

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.analytics.DEVICEMANAGEMENT
import com.tatasky.binge.analytics.SOURCE_LOGIN
import com.tatasky.binge.analytics.SWITCHSUBSCRIPTION
import com.tatasky.binge.data.networking.models.response.DeviceListResponse
import com.tatasky.binge.databinding.FragmentDeviceManagementBinding
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.device_management.DeviceListManagementActivity.Companion.KEY_IS_DEVICE_REVIEW_ON_MAX_LIMIT_REACHED
import com.tatasky.binge.ui.features.device_management.DeviceListManagementActivity.Companion.KEY_TEMP_BAID
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.myaccount.MyAccountViewModel
import com.tatasky.binge.utils.DeviceInfoUtils
import com.tatasky.binge.utils.VerticalSpaceItemDecoration
import com.tatasky.binge.utils.dpToPx
import com.tatasky.binge.utils.isTablet
import javax.inject.Inject

/**
 * Using same class to handle device management with or without login
 */
class DeviceListFragment : BaseFragment<FragmentDeviceManagementBinding, MyAccountViewModel>() {

    private var deviceName: String? = null
    private var subscriberId: String? = null
    private var isReviewDeviceOnMaxLimitReached: Boolean = false

    @Inject
    lateinit var analytics: DeviceListManagementAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Confirm if transition is required
        /*val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
            this.duration = 500
        }
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
            this.duration = 500
        }
        returnTransition = backward
        reenterTransition = backward
        exitTransition = forward*/

    }
    override fun getViewModelClass(): Class<MyAccountViewModel> {
        return MyAccountViewModel::class.java
    }

    override fun layoutId(): Int {
        return R.layout.fragment_device_management
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        uiChanges()
    }

    private fun uiChanges() {
        val layoutParams= binding.llouter?.layoutParams as LinearLayout.LayoutParams
        activity?.let {
            layoutParams.apply {
                layoutParams.marginStart = it.resources.getDimensionPixelSize(R.dimen.tab_padding)
                layoutParams.marginEnd  = it.resources.getDimensionPixelSize(R.dimen.tab_padding_right)
            }
        }

        binding.llouter?.layoutParams=layoutParams
    }

    override fun getViewModelOwner(): ViewModelStoreOwner {
        return this
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        analytics.trackDeviceListInitiate(sharedPrefs.getOriginalSubscriberId())
    }

    override fun onDestroy() {
        if (isReviewDeviceOnMaxLimitReached) {
            val source = if (!sharedPrefs.getLoginStatus()) SOURCE_LOGIN else {
                if (!isReviewDeviceOnMaxLimitReached) DEVICEMANAGEMENT else SWITCHSUBSCRIPTION
            }
            analytics.trackDeviceLimitExit(source)
        }
        super.onDestroy()
    }

    override fun setObserver() {
        viewModel.updateInPack.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let { it ->
                if(it && !isReviewDeviceOnMaxLimitReached){
                    viewModel.fetchDeviceList(null)
                }
            }
        })
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                if (!viewModel.onlyMessage)
                    findNavController().navigateUp()
                if (isReviewDeviceOnMaxLimitReached)
                    setResultAndExit(Activity.RESULT_CANCELED)
            }
        })
        viewModel.getDeviceListResponse().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled().let { item ->
                binding.recyclerDevices.show()
                subscriberId = item?.data?.subscriberId
                analytics.trackDeviceLimitView(
                    source = if (!sharedPrefs.getLoginStatus()) SOURCE_LOGIN else {
                        if (!isReviewDeviceOnMaxLimitReached) DEVICEMANAGEMENT else SWITCHSUBSCRIPTION
                    },item?.data?.deviceList?.map { it.deviceName }?.joinToString()?:""

                )
                filterPrimaryDevice(item)
                filterOtherDevices(item, item?.data?.smallDeviceCount ?: 0, isReviewDeviceOnMaxLimitReached)

            }
        })

        viewModel.getRemoveDeviceResponse().observe(viewLifecycleOwner, Observer {
            //handle removal of device
            analytics.trackRemoveDevices(
                source = if (!sharedPrefs.getLoginStatus()) SOURCE_LOGIN else {
                    if (!isReviewDeviceOnMaxLimitReached) DEVICEMANAGEMENT else SWITCHSUBSCRIPTION
                },
                deviceName ?: ""
            )
            if (isReviewDeviceOnMaxLimitReached)
                setResultAndExit(Activity.RESULT_OK)
            else
                viewModel.fetchDeviceList(null)
        })

        viewModel.getClickedItem().observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled().let { item ->
                deviceName = item?.deviceName
                //show dialog
                val msg = String.format(getString(R.string.remove_device_msg), item?.deviceNameTruncated)
                showDialog(
                    DialogModel(
                        false, R.drawable.ic_remove_device,
                        msg,
                        getString(R.string.remove_device),
                        getString(R.string.cancel)
                    ),
                    object : CommonDialogEventListener {
                        override fun onPrimaryButtonClick() {
                            analytics.trackDeviceLimitConfirmationPopupShown(
                                source = if (!sharedPrefs.getLoginStatus()) SOURCE_LOGIN else {
                                    if (!isReviewDeviceOnMaxLimitReached) DEVICEMANAGEMENT else SWITCHSUBSCRIPTION
                                },
                                deviceName ?: ""
                            )

                            analytics.trackMixPanelDeviceRemoveConfirm(  source = if (!sharedPrefs.getLoginStatus()) SOURCE_LOGIN else {
                                if (!isReviewDeviceOnMaxLimitReached) DEVICEMANAGEMENT else SWITCHSUBSCRIPTION
                            },
                                deviceName ?: "")
                            hideDialog()
                            item?.let {
                                viewModel.removeDevice(item.deviceNumber, activity?.intent?.getStringExtra(KEY_TEMP_BAID), item.deviceName)
                            }
                        }

                        override fun onCloseButtonClick() {
                            hideDialog()
                            if (isReviewDeviceOnMaxLimitReached)
                                setResultAndExit(Activity.RESULT_CANCELED)
                        }

                        override fun onSecondaryButtonClick() {
                            hideDialog()
                            analytics.trackDeviceLimitRemoveSkip( source = if (!sharedPrefs.getLoginStatus()) SOURCE_LOGIN else {
                                if (!isReviewDeviceOnMaxLimitReached) DEVICEMANAGEMENT else SWITCHSUBSCRIPTION
                            },
                                subscriberId ?: "",deviceName ?: "")
                            if (isReviewDeviceOnMaxLimitReached)
                                setResultAndExit(Activity.RESULT_CANCELED)
                        }
                    })
            }
        })
    }

    private fun setResultAndExit(result: Int) {
        activity?.setResult(result)
        if(!findNavController().navigateUp())
            activity?.finish()
    }

    private fun filterPrimaryDevice(item: DeviceListResponse?) {
        item?.data?.deviceList?.let {
            if (!it.isEmpty()) {
                //    val model = it.find { it.primary}
                val model = it.find { it.primary }
                if (model != null) {
                    binding.item = model
                    viewModel.isPrimaryDevice = true
                    val countText = item.data.largeDeviceFooterMessage ?: String.format(
                        resources.getString(R.string.m_out_n_primary),
                        1
                    )
                    binding.primaryLayout.tvCount.text = countText
                    binding.primaryLayout.tvTitle.text = getString(R.string.primary_device)
                    binding.primaryLayout.tvTitle.show()
                    binding.primaryLayout.llContainer.show()
                    binding.primaryLayout.ivDelete.hide()
                    binding.primaryLayout.tvDescription.show()
                    binding.primaryLayout.tvCount.show()
                    binding.primaryLayout.ivDevice.setImageResource(R.drawable.ic_tv)
                } else {
                    binding.primaryLayout.llContainer.hide()
                }
            }
        }
    }

    private fun filterOtherDevices(item: DeviceListResponse?, smallDevicesCount: Int, shouldHideSmallDevicesHeader: Boolean) {
        item?.data?.deviceList?.let {
            if (it.isNotEmpty()) {
                val list = it.filterNot { it.primary }
                viewModel.setDeviceList(list, item.data.smallDeviceFooterMessage, smallDevicesCount, shouldHideSmallDevicesHeader)
            }
        }
    }

    override fun toBeCalledOnce() {
        /* analytics.trackDeviceLimitView(
             source = if (!sharedPrefs.getLoginStatus()) SOURCE_LOGIN else {
                 if (!isReviewDeviceOnMaxLimitReached) DEVICEMANAGEMENT else "SWITCHSUBSCRIPTION"
             },
             deviceName ?: ""
         )*/
        if (context?.let { isTablet(it) } == true)
        {
            binding.toolbarLayout.visibility = View.INVISIBLE
            binding.textView.visibility =View.VISIBLE
        }
        isReviewDeviceOnMaxLimitReached =
            activity?.intent?.getBooleanExtra(KEY_IS_DEVICE_REVIEW_ON_MAX_LIMIT_REACHED, false) == true
        binding.isDeviceReviewOnMaxLimitReached = isReviewDeviceOnMaxLimitReached
        binding.cancelBtn.setOnClickListener {
            setResultAndExit(Activity.RESULT_CANCELED)
        }
        binding.viewModel = viewModel
        viewModel.currentDeviceId = DeviceInfoUtils.getDeviceId(requireContext())
        viewModel.fetchDeviceList(activity?.intent?.getStringExtra(KEY_TEMP_BAID))
        binding.recyclerDevices.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(requireContext(), 6)))
    }
}
