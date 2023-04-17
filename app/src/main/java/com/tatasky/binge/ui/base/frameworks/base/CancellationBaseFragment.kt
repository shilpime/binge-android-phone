package com.tatasky.binge.ui.base.frameworks.base

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_PARTNER_HOME
import com.tatasky.binge.analytics.SOURCE_SWITCH_SUBSCRIPTION
import com.tatasky.binge.analytics.SWITCHSUBSCRIPTION
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.LoginResponse
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.features.device_management.DeviceListManagementActivity
import com.tatasky.binge.ui.features.device_management.DeviceListManagementAnalytics
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.recharge.launchRechargeActivity
import com.tatasky.binge.ui.features.switchaccount.SwitchAccountActivity
import com.tatasky.binge.utils.*
import java.util.*
import javax.inject.Inject

abstract class CancellationBaseFragment<VB : ViewDataBinding, baseViewModel : CancellationBaseViewModel> :
	BaseFragment<VB, baseViewModel>() {

	private var tempDSN: String? = null
	private var tempTargetBAID: String = ""
	private var _selectedProfile: LoginResponse.BingeSubscription? = null
	private var isSilentTrigger:Boolean = false

	@Inject
	lateinit var deviceMgmtAnalytics: DeviceListManagementAnalytics

	fun shouldStartCancellationTrigger(silentTrigger:Boolean = false) : Boolean{
		isSilentTrigger = silentTrigger
		return sharedPrefs.getDeviceCancellationFlag()
    }

	fun triggerCancellationDialog(dialogHeader: String?, dialogText: String?, listOfBaIds: List<LoginResponse.BingeSubscription>) { //No other Binge Subscription
		if (listOfBaIds.isEmpty()) {
			if(isSilentTrigger){
				viewModel.createBingeAccount(sharedPrefs.getOriginalSubscriberId())
			} else {
				val eventListener = object : CommonDialogEventListener {
					override fun onPrimaryButtonClick() { //Take user to pack selection journey
						viewModel.createBingeAccount(sharedPrefs.getOriginalSubscriberId())
					}

					override fun onSecondaryButtonClick() {
						hideDialog()
						viewModel.closePrimeActivity()
					}

					override fun onCloseButtonClick() {

					}

				}
				showCancelledUserDialog(
					dialogHeader,
					dialogText,
					getString(R.string.subscribe_now),
					getString(R.string.cancel),
					eventListener
				)
			}
		} // Only 1 other account
		else if (listOfBaIds.size == 1) {
			tempDSN = if (listOfBaIds[0].baId.isNullOrBlank()) listOfBaIds[0].deviceSerialNumber else null
			tempTargetBAID = listOfBaIds[0].baId ?: ""
			val eventListener = object : CommonDialogEventListener {
				override fun onPrimaryButtonClick() { //Take user to pack selection journey
					viewModel.switchBAID(sharedPrefs.getBaId(), if (listOfBaIds[0].baId.isNullOrBlank()) listOfBaIds[0].deviceSerialNumber
					else null, listOfBaIds[0].baId ?: "")
				}

				override fun onSecondaryButtonClick() {
					hideDialog()
					viewModel.closePrimeActivity()
				}

				override fun onCloseButtonClick() {

				}
			}
			showCancelledUserDialog(dialogHeader, dialogText, getString(R.string._switch), null, eventListener)
		} //More than 1 active | inactive
		else {
			val eventListener = object : CommonDialogEventListener {
				override fun onPrimaryButtonClick() {
					//start activity
					viewModel.closePrimeActivity()
					context?.let {
						startActivity(Intent(it, SwitchAccountActivity::class.java).apply { putExtra(Intent.EXTRA_USER, true) })
					}
				}

				override fun onSecondaryButtonClick() {
					hideDialog()
					viewModel.closePrimeActivity()
				}

				override fun onCloseButtonClick() {

				}
			}
			showCancelledUserDialog(dialogHeader, dialogText, getString(R.string._switch), null, eventListener)
		}
	}
	private var runnable: Runnable = Runnable { startHomeScreen(activity) }
	private fun showCancelledUserDialog(dialogHeader: String?, dialogText: String?, primaryButtonText: String?, secondaryButtonText: String?, eventListener: CommonDialogEventListener) {
		showDialog(DialogModel(false, R.drawable.ic_subscription_error, dialogHeader, primaryButtonText, secondaryButtonText, dialogText, null, null), eventListener)
	}

	override fun onError(errorModel: ErrorModel) {
		if (errorModel.code == LOGIN_MAX_DEVICE_ERROR_CODE && sharedPrefs.getDeviceCancellationFlag()) {
			showDialog(
				DialogModel(
					false,
					null,
					errorModel.message,
					getString(R.string.ok),
					null,
					null,
					null,
					null
				),
				object : CommonDialogEventListener {
					override fun onPrimaryButtonClick() {
						viewModel.removeDeviceAndSignout()
					}

					override fun onSecondaryButtonClick() {
					}

					override fun onCloseButtonClick() {
					}

				}
			)
		} else
			super.onError(errorModel)
	}

	override fun setObserver() {
		viewModel.getMaxDeviceLimitReachedResponse().observe(viewLifecycleOwner) {
			it.getContentIfNotHandled()?.let { maxDeviceLimitReachedResponse ->
				showDialog(
					DialogModel(
						false,
						R.drawable.ic_device_center,
						maxDeviceLimitReachedResponse.message,
						getString(R.string.review_devices),
						getString(R.string.text_non_underlined_Not_Now),
						maxDeviceLimitReachedResponse.title
					), object :
						CommonDialogEventListener {
						override fun onPrimaryButtonClick() {
							hideDialog()
							activity?.let { context ->
								startActivityForResult(Intent(context, DeviceListManagementActivity::class.java).apply {
									putExtra(DeviceListManagementActivity.KEY_TEMP_BAID, tempTargetBAID)
									putExtra(DeviceListManagementActivity.KEY_IS_DEVICE_REVIEW_ON_MAX_LIMIT_REACHED, true)
								}, DeviceListManagementActivity.DEVICE_LIMIT_REQUEST_CODE)
							}
						}

						override fun onSecondaryButtonClick() {
							deviceMgmtAnalytics.trackDeviceLimitPopupSkip(SWITCHSUBSCRIPTION)
							hideDialog()
						}

						override fun onCloseButtonClick() {
							hideDialog()
						}
					})
			}
		}

		/*viewModel.getMaxDeviceLimitReachedResponse().observe(viewLifecycleOwner, Observer {
			it.getContentIfNotHandled()?.let { maxDeviceLimitReachedResponse ->
				deviceMgmtAnalytics.trackDeviceLimitPopupShown(SOURCE_SWITCH_SUBSCRIPTION)

				showDialog(
					DialogModel(
						false,
						R.drawable.ic_device_center,
						maxDeviceLimitReachedResponse.message,
						getString(R.string.review_devices),
						getString(R.string.not_now),
						maxDeviceLimitReachedResponse.title
					), object :
						CommonDialogEventListener {
						override fun onPrimaryButtonClick() {
							deviceMgmtAnalytics.trackDeviceLimitPopupReviewClick(
								SOURCE_SWITCH_SUBSCRIPTION)
							hideDialog()
							activity?.let {
								startActivityForResult(Intent(it, SwitchAccountActivity::class.java).apply {
									putExtra("baid", tempTargetBAID)
									putExtra("isDeviceReviewOnMaxLimitReached", true)
									putExtra("source", SOURCE_SWITCH_SUBSCRIPTION)
								}, SwitchAccountActivity.DEVICE_LIMIT_REQUEST_CODE)
							}
						}

						override fun onSecondaryButtonClick() {
							deviceMgmtAnalytics.trackDeviceLimitPopupSkip(SOURCE_SWITCH_SUBSCRIPTION)
							hideDialog()
						}

						override fun onCloseButtonClick() {
							hideDialog()
						}
					})
			}

		})*/

		viewModel.rechargeResponse().observe(this, Observer {
			it.getContentIfNotHandled()?.data?.let {
				if (it.rechargeUrl.isNullOrBlank()) {
					onError(ErrorModel(message = "Recharge cannot be processed now please try after some time"))
					return@Observer
				}
				try {
					launchRechargeActivity(this, Uri.parse(it.rechargeUrl))
				} catch (e: Exception) {
					e.printStackTrace()
				}
			}
		})
		viewModel.getBaLookupResponse().observe(viewLifecycleOwner, Observer {
			it.getContentIfNotHandled()?.let {
				it.data?.let { baDetail ->
					triggerCancellationDialog(baDetail.bingeSubscriptionHeader, baDetail.bingeSubscriptionMessage, baDetail.listOfBaIds)
				}
			}
		})
		viewModel.deviceForceLogout().observe(this, Observer {
			it.getContentIfNotHandled()?.let {
				context?.let {
					logoutApplication(it)
				}
			}
		})
		viewModel.getSelectedBAID().observe(viewLifecycleOwner, Observer {
			it.getContentIfNotHandled()?.let {
				_selectedProfile = it
			}
		})
		viewModel.getSwitchAccountResponse().observe(viewLifecycleOwner, Observer {
			it.getContentIfNotHandled()?.let {
				_selectedProfile?.let { selectedProfile->
					if (selectedProfile.baId.isNullOrBlank()) {
						selectedProfile.baId = it.data?.baId
						selectedProfile.profileId = it.data?.profileId
					}
					setSelectedAccountDetail(selectedProfile, sharedPrefs)
					Handler(Looper.getMainLooper()).postDelayed(runnable, 500)
				}
			}
		})
		viewModel.getBingeAccountResponse().observe(viewLifecycleOwner, Observer {
			it.getContentIfNotHandled()?.let {newBingeResponse ->
				viewModel.saveLoggedInDetails(newBingeResponse)
				startActivity(
					getSubscriptionActivityIntent(
						context,
						true,
						null,
						SOURCE_PARTNER_HOME,
						initiateRecharge = true,
						fromDialog = false
					).apply {
						flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
					}
				)
				viewModel.closePrimeActivity()
			}
		})
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == SwitchAccountActivity.DEVICE_LIMIT_REQUEST_CODE)
			if (resultCode == Activity.RESULT_OK)
				viewModel.switchBAID(
					sharedPrefs.getBaId(),
					tempDSN,
					tempTargetBAID
				)
			else
				context?.let { context ->
					logoutApplication(context)
				}
	}
}