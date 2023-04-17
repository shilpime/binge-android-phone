package com.tatasky.binge.ui.features.prime.view

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.CalendarContract
import android.view.MotionEvent
import android.view.View
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_HOME
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.ContentItem
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.details.DetailAnalytics
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.dialog.DialogViewModel
import com.tatasky.binge.ui.features.player.PlayerAnalytics
import com.tatasky.binge.ui.features.prime.PrimeAnalytics
import com.tatasky.binge.ui.features.prime.viewmodel.PrimeViewModel
import com.tatasky.binge.utils.*
import kotlinx.android.synthetic.main.activity_amazon_prime.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 * Created by Srikant on 02/06/21.
 */
class PrimeActivity : BaseActivity<PrimeViewModel>() {

	@Inject
	lateinit var primeAnalytics: PrimeAnalytics
	private val CHROME_PACKAGE_NAME = "com.android.chrome"

	override fun getContentViewId(): Int = R.layout.activity_amazon_prime

	override fun getRootLayoutContainer(): View = root_container

	override fun getViewModelClass(): Class<PrimeViewModel> = PrimeViewModel::class.java

	var activityTouchable = true

	override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		return if (!activityTouchable) true else super.dispatchTouchEvent(ev)
	}

	//returns true if prime installed else false
	fun checkPrimeApp(): Boolean {
		return packageManager.isPackageInstalled("com.amazon.avod.thirdpartyclient")
	}

	fun openPrimePlayStore() {
		val intent = Intent(Intent.ACTION_VIEW)
		intent.data =
			Uri.parse("https://play.google.com/store/apps/details?id=com.amazon.avod.thirdpartyclient")
		startActivity(intent)
		finish()
	}

	@SuppressLint("SourceLockedOrientationActivity")
	override fun init(savedInstanceState: Bundle?) {

		/**
		 *This activity is a fullscreen transparent activity, In android Oreo (API 26)
		 * you can not change orientation for transparent Activity
		 */

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
			requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
		} else {
			requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
		}

		val contentItem = try {
			intent.extras?.getParcelable<ContentItem>("contentItem")
		} catch (e: Exception) {
			null
		}
		val subscribedPack = viewModel.subscribedPack()
		val primePackDetails = subscribedPack?.primePackDetails ?: sharedPrefs.getPrimePackDetails()

		val primeHeader1 = primePackDetails?.primeMessageDetails?.primeTitle1
		val primeHeader2 = primePackDetails?.primeMessageDetails?.primeTitle2
		val primeMessage1 = primePackDetails?.primeMessageDetails?.primeMessage1
		val primeMessage2 = primePackDetails?.primeMessageDetails?.primeMessage2

		contentItem?.let {
			viewModel.source = it.source
			val currTime = Calendar.getInstance().apply {
				this.set(Calendar.HOUR_OF_DAY, 0)
				this.set(Calendar.MINUTE, 0)
				this.set(Calendar.SECOND, 0)
				this.set(Calendar.MILLISECOND, 0)
			}.timeInMillis
			if (true == primePackDetails?.isActive) {
				val primePopupCount = sharedPrefs.getPrimePopupCount()
				val primePopupFrequency = sharedPrefs.getPrimePopupFrequency()
				if (primePopupCount >= primePopupFrequency) {
					val lastPrimePopupShownTimestamp = sharedPrefs.getLastPrimePopupShownTimeStamp()
					if (TimeUnit.MILLISECONDS.toDays((currTime - lastPrimePopupShownTimestamp)) >= sharedPrefs.getPrimePopupInterval()) {
						sharedPrefs.setPrimePopupShownCount(0)
					} else {
						navigateToPrime()
						finish()
						return
					}
				}
				val dialogViewModel =
					ViewModelProvider(this, viewModelFactory).get(DialogViewModel::class.java)
				val dialogModel =
					DialogModel(false, R.drawable.ic_amazon_prime, primeHeader1, getString(R.string.proceed), getString(R.string.cancel), primeMessage1)
				val eventListener = object : CommonDialogEventListener {
					override fun onPrimaryButtonClick() {
						navigateToPrime()
						finish()
						hideDialog()
					}

					override fun onCloseButtonClick() {
					}

					override fun onSecondaryButtonClick() {
						hideDialog()
						finish()
					}
				}
				dialogViewModel.setDialogModel(dialogModel)
				dialogViewModel.setEventHandler(eventListener)
				showDialog()
				sharedPrefs.setPrimePopupShownCount(sharedPrefs.getPrimePopupCount() + 1)
				sharedPrefs.setLastPrimePopupShownTimeStamp(currTime)

					//				try {
					//					if (sharedPrefs.getPrimePopupCount() < 2) {
					//						val dialogViewModel =
					//							ViewModelProvider(this, viewModelFactory).get(DialogViewModel::class.java)
					//						if (checkPrimeApp()) {
					//							val dialogModel =
					//								DialogModel(false, R.drawable.ic_amazon_prime, primeHeader1, getString(R.string.proceed), getString(R.string.cancel), primeMessage1)
					//							val eventListener = object : CommonDialogEventListener {
					//								override fun onPrimaryButtonClick() {
					//									viewModel.actionCW(it, 1, 10)
					//									hideDialog()
					//									val primeIntent = Intent(Intent.ACTION_VIEW)
					//									primeIntent.data =
					//										Uri.parse(getString(R.string.amazon_prime_deeplink_url, it.providerContentId))
					//									primeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
					//									startActivity(primeIntent)
					//									finish()
					//								}
					//
					//								override fun onCloseButtonClick() {
					//								}
					//
					//								override fun onSecondaryButtonClick() {
					//									hideDialog()
					//									finish()
					//								}
					//							}
					//							dialogViewModel.setDialogModel(dialogModel)
					//							dialogViewModel.setEventHandler(eventListener)
					//						} else {
					//							val dialogModel =
					//								DialogModel(false, R.drawable.ic_amazon_prime, primeHeader2, getString(R.string.proceed), getString(R.string.cancel), primeMessage2)
					//							val eventListener = object : CommonDialogEventListener {
					//								override fun onPrimaryButtonClick() {
					//									viewModel.actionCW(it, 1, 10)
					//									hideDialog()
					//									openPrimePlayStore()
					//								}
					//
					//								override fun onCloseButtonClick() {
					//								}
					//
					//								override fun onSecondaryButtonClick() {
					//									hideDialog()
					//									finish()
					//								}
					//							}
					//							dialogViewModel.setDialogModel(dialogModel)
					//							dialogViewModel.setEventHandler(eventListener)
					//						}
					//						showDialog()
					//						sharedPrefs.increasePrimePopupCount()
					//					} else {
					//						navigateToPrime()
					//					}
					//				} catch (e:Exception){
					//					viewModel.actionCW(it, 1, 10)
					//					val primeIntent = Intent(Intent.ACTION_VIEW)
					//					primeIntent.data =
					//						Uri.parse(getString(R.string.amazon_prime_deeplink_url, it.providerContentId))
					//					primeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
					//					startActivity(primeIntent)
					//					finish()
					//				}
				} else if (true == primePackDetails?.isSuspended) {
					if(sharedPrefs.getDeviceCancellationFlag()){
						fragment_container.background = ColorDrawable(Color.TRANSPARENT)
						fragment_container.show()
						val navHostFragment =
							supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
						val navController = navHostFragment.navController
						val graphInflater = navController.navInflater
						val navGraph = graphInflater.inflate(R.navigation.nav_prime)
						navGraph.startDestination = R.id.transparentFragment
						navController.graph = navGraph
						return
					}
					val dialogViewModel =
						ViewModelProvider(this, viewModelFactory).get(DialogViewModel::class.java)
					when {
						true == primePackDetails?.primeMessageDetails?.statusType?.equals("inactive", true) -> {
							val dialogModel =
								DialogModel(false, R.drawable.ic_subscription_error, primeHeader1, getString(R.string.recharge), getString(R.string.skip), primeMessage1)
							val eventListener = object : CommonDialogEventListener {
								override fun onPrimaryButtonClick() {
									hideDialog()
									primeAnalytics.trackPrimeSuspended(getString(R.string.recharge))
									startActivity(getSubscriptionActivityIntent(this@PrimeActivity, false, null, SOURCE_HOME, true))
									finish()
								}

								override fun onCloseButtonClick() {
								}

								override fun onSecondaryButtonClick() {
									primeAnalytics.trackPrimeSuspended("SKIP")
									finish()
								}
							}
							dialogViewModel.setDialogModel(dialogModel)
							dialogViewModel.setEventHandler(eventListener)
						}
						true == primePackDetails?.primeMessageDetails?.statusType?.equals("trivial", true) -> {
							val dialogModel =
								DialogModel(false, R.drawable.ic_subscription_error, primeHeader1, getString(R.string.subscribe_now), getString(R.string.cancel), primeMessage1)
							val eventListener = object : CommonDialogEventListener {
								override fun onPrimaryButtonClick() {
									hideDialog()
									primeAnalytics.trackPrimeSuspended(getString(R.string.subscribe_now))
									startActivity(getSubscriptionActivityIntent(this@PrimeActivity, false, null, SOURCE_HOME, true))
									finish()
								}

								override fun onCloseButtonClick() {
								}

								override fun onSecondaryButtonClick() {
									primeAnalytics.trackPrimeSuspended(getString(R.string.cancel))
									finish()
								}
							}
							dialogViewModel.setDialogModel(dialogModel)
							dialogViewModel.setEventHandler(eventListener)
						}
						else -> {
							val dialogModel =
								DialogModel(false, R.drawable.ic_amazon_prime, primeHeader1, getString(R.string.resume_prime), getString(R.string.skip), primeMessage1)
							val eventListener = object : CommonDialogEventListener {
								override fun onPrimaryButtonClick() {
									hideDialog()
									primeAnalytics.trackPrimeSuspended(getString(R.string.resume_prime))
									viewModel.resumePrime()
								}

								override fun onCloseButtonClick() {
								}

								override fun onSecondaryButtonClick() {
									primeAnalytics.trackPrimeSuspended("SKIP")
									navigateToPrime()
									finish()
									hideDialog()

								}
							}
							dialogViewModel.setDialogModel(dialogModel)
							dialogViewModel.setEventHandler(eventListener)
						}
					}
					showDialog()
				} else {
					val existingPrimeBtnClickCount = sharedPrefs.getExistingPrimeInterstitialClickCount()
					val existingPrimeBtnClickFrequency = sharedPrefs.getExistingPrimeInterstitialFrequency()
					if(existingPrimeBtnClickCount >= existingPrimeBtnClickFrequency){
						val lastExistingPrimeBtnClick = sharedPrefs.getLastExistingPrimeButtonClick()
						when {
							TimeUnit.MILLISECONDS.toDays(currTime-lastExistingPrimeBtnClick) >= sharedPrefs.getExistingPrimeInterstitialInterval() -> {
								sharedPrefs.setExistingPrimeInterstitialClickCount(0)
							}
							it.providerContentId.isBlank() -> {
							}
							else -> {
								navigateToPrime()
								finish()
								return@let
							}
						}
					}
					fragment_container.show()
					val navHostFragment =
						supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
					val navController = navHostFragment.navController
					navController.setGraph(R.navigation.nav_prime)
				}
			} ?: finish()

			viewModel.getPrimeResumeResponse().observe(this, Observer {
				it.getContentIfNotHandled()?.let {
					if (it.code == CODE_SUCCESS) {
						primeAnalytics.trackPrimeResumeSuccess(sharedPrefs.getOriginalSubscriberId())
					}
					this@PrimeActivity.finish()
				}
			})
		}

		fun navigateToPrime() {
			val contentItem = try {
				intent.extras?.getParcelable<ContentItem>("contentItem")
			} catch (e: Exception) {
				null
			}
			contentItem?.takeIf { it.providerContentId.isNotBlank() /*For deeplink handling, Using providerContentId to handle the case of Deep link*/ }?.let {
				viewModel.actionCW(it, 1, 10)
				val primeUri =
					Uri.parse(getString(R.string.amazon_prime_deeplink_url, it.providerContentId))
				openChromeTab(primeUri)
			} ?: kotlin.run {
				// User clicked on I'm existing user when opened Prime interstitial/Upsell via Deeplink
				primeAnalytics.trackPrimePackAddSkip(viewModel.getPrimeInterstitialResponse().value?.peekContent()?.packList?.get(0)?.packType?:"PAID", viewModel.source)
				finish()
			}       //		contentItem?.let {
			//			viewModel.actionCW(it,  1, 10)
			//			if (checkPrimeApp()) {
			//				val primeIntent = Intent(Intent.ACTION_VIEW)
			//				primeIntent.data =
			//					Uri.parse(getString(R.string.amazon_prime_deeplink_url, it.providerContentId))
			//				primeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY
			//				startActivity(primeIntent)
			//				finish()
			//			} else {
			//				openPrimePlayStore()
			//			}
			//		}
		}

		@Throws(ActivityNotFoundException::class)
		private fun openChromeTab(uri: Uri) {
			if (getCustomTabsPackages()?.find { it.activityInfo.packageName == CHROME_PACKAGE_NAME } != null) {
				try {
					val builder = CustomTabsIntent.Builder()
					builder.setShowTitle(true)
					builder.setCloseButtonIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_back))
					builder.setUrlBarHidingEnabled(true)
					builder.setDefaultColorSchemeParams(CustomTabColorSchemeParams.Builder()
						.setToolbarColor(ContextCompat.getColor(this, R.color.darkBackground))
						.build())
					builder.setShareState(CustomTabsIntent.SHARE_STATE_OFF)
					val customTabsIntent = builder.build()
					customTabsIntent.intent.setPackage(CHROME_PACKAGE_NAME)
					customTabsIntent.launchUrl(this, uri)
				} catch (e: Exception) {
					val builder = CustomTabsIntent.Builder()
					val customTabsIntent = builder.build()
					customTabsIntent.launchUrl(this, uri)
				}
			} else {
				try {
					val builder = CustomTabsIntent.Builder()
					val customTabsIntent = builder.build()
					customTabsIntent.launchUrl(this, uri)
				} catch (e: Exception) {

				}

			}
		}


		private fun getCustomTabsPackages(): ArrayList<ResolveInfo>? {
			val pm: PackageManager = packageManager        // Get default VIEW intent handler.
			val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))

			// Get all apps that can handle VIEW intents.
			val resolvedActivityList: List<ResolveInfo> =
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
					pm.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL)
				} else {
					pm.queryIntentActivities(activityIntent, 0)
				}
			val packagesSupportingCustomTabs = ArrayList<ResolveInfo>()
			for (info in resolvedActivityList) {
				val serviceIntent = Intent()
				serviceIntent.setAction(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION)
				serviceIntent.setPackage(info.activityInfo.packageName)
				e("getCustomTabsPackages", "info.activityInfo.packageName : ${info.activityInfo.packageName}")            // Check if this package also resolves the Custom Tabs service.
				if (pm.resolveService(serviceIntent, 0) != null) {
					packagesSupportingCustomTabs.add(info)
				}
			}
			return packagesSupportingCustomTabs
		}
	}