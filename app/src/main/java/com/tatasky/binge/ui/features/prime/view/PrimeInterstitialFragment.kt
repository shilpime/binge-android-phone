package com.tatasky.binge.ui.features.prime.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_HOME
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.data.networking.models.response.PrimeInterstitialResponse
import com.tatasky.binge.databinding.FragmentPrimeInterstitialBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.helper.loadImageTextViewDrawable
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.prime.PrimeAnalytics
import com.tatasky.binge.ui.features.prime.viewmodel.PrimeViewModel
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.recharge.launchAmazonActivationActivity
import com.tatasky.binge.ui.features.recharge.launchRechargeActivity
import com.tatasky.binge.utils.*
import com.tatasky.binge.utils.BindingAdapters.Companion.setHtmlText
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class PrimeInterstitialFragment : BaseFragment<FragmentPrimeInterstitialBinding, PrimeViewModel>() {

	@Inject
	lateinit var primeAnalytics : PrimeAnalytics
	override fun getViewModelClass(): Class<PrimeViewModel> = PrimeViewModel::class.java
	private val mHandler = Handler(Looper.getMainLooper())

	override fun layoutId(): Int = R.layout.fragment_prime_interstitial

	override fun getViewModelOwner() = activity as PrimeActivity

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == RechargeActivity.RECHARGE_REQUEST_CODE) {
			if (1 == data?.getIntExtra("rechargeStatus", 0)) {
				showDialog(DialogModel(false, R.drawable.ic_success_tick, getString(R.string.payment_success), getString(R.string.continue_to_prime), null, getString(R.string.recharge_success_amazon)), object :
					CommonDialogEventListener {
					override fun onPrimaryButtonClick() {
						hideDialog()
						viewModel.activatePrime(viewModel.getPrimeInterstitialResponse().value?.peekContent()?.packList?.get(0)?.packId ?: "")
					}

					override fun onSecondaryButtonClick() {
					}

					override fun onCloseButtonClick() {
					}
				})
			} else {
				try {
					primeAnalytics.trackPrimeLowBalanceCancel(viewModel.getPrimeInterstitialResponse().value?.peekContent()?.packList?.get(0)?.packType ?: "PAID")
				} catch (e: Exception) { }
				showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.payment_failure), getString(R.string.ok), null, null), object :
					CommonDialogEventListener {
					override fun onPrimaryButtonClick() {
						hideDialog()
						activity?.finish()
					}

					override fun onSecondaryButtonClick() {
					}

					override fun onCloseButtonClick() {
					}
				})
			}
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val forward = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
			this.duration = 250
		}
		enterTransition = forward

		val backward = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
			this.duration = 250
		}
		returnTransition = backward
		reenterTransition = backward
		exitTransition = forward
	}

	val callback: OnBackPressedCallback = object : OnBackPressedCallback(
		true // default to enabled
	) {
		override fun handleOnBackPressed() {
			try {
				primeAnalytics.trackPrimePackAddSkip(viewModel.getPrimeInterstitialResponse().value?.peekContent()?.packList?.get(0)?.packType ?: "PAID", viewModel.source)
			}catch (e:Exception){}
			isEnabled = false
			activity?.onBackPressed()
		}
	}

	override fun setObserver() {
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
		viewModel.progressListener.removeObservers(viewLifecycleOwner)
		viewModel.progressListener.observe(viewLifecycleOwner, Observer {
			if (it) {
				mHandler.postDelayed(
					showProgress, loaderDelayTime
				)
				mActivity?.showProgress()
				(mActivity as PrimeActivity).activityTouchable = false
				view?.closeKeyboard()
			} else {
				mHandler.removeCallbacks(showProgress)
				mActivity?.hideProgress()
				(mActivity as PrimeActivity).activityTouchable = true
			}
		})

		viewModel.getPrimeActivationResponse().observe(viewLifecycleOwner, Observer {
			it.getContentIfNotHandled()?.let { it ->
				when (it.code) {
					PRIME_TS_BALANCE_LOW -> { //Low balance but subscribed to binge
//						findNavController().navigateSafe(PrimeInterstitialFragmentDirections.actionPrimeInterstitialFragmentToPrimeRechargeFragment(viewModel.getPrimeInterstitialResponse().value?.peekContent()?.packList?.get(0)!!, it.message?:""))
						viewModel.startRecharge(viewModel.source ?: "")
					}
					PRIME_NOT_SUBSCRIBED_BINGE -> { //Not subscribed to binge
						showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.no_subscription) , getString(R.string.proceed), getString(R.string.cancel), it.message), object :
							CommonDialogEventListener {
							override fun onPrimaryButtonClick() {
								//startActivity(getSubscriptionActivityIntent(context, false, null, SOURCE_HOME, true))
								hideDialog()
								//Todo check for managedApp
//								moveToManagedApp(sharedPrefs,activity,"",context=context, checkFdo = false)
								startActivity(
									getSubscriptionActivityIntent(
										context,
										startPackListing = true,
										fromScreen = SOURCE_HOME
									)
								)
								activity?.finish()
							}

							override fun onSecondaryButtonClick() {
								hideDialog()
							}

							override fun onCloseButtonClick() {
							}

						})
					}
					PRIME_INACTIVE_SUBSCRIBED_BINGE -> {
						showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.subscription_inactive), getString(R.string.recharge), getString(R.string.skip), it.message), object :
							CommonDialogEventListener {
							override fun onPrimaryButtonClick() {
//								startActivity(getSubscriptionActivityIntent(context, false, null, SOURCE_HOME, true))
								viewModel.startRecharge(viewModel.source ?: "")
								hideDialog()
//								activity?.finish()
							}

							override fun onSecondaryButtonClick() {
								hideDialog()
							}

							override fun onCloseButtonClick() {
							}

						})
					}
					PRIME_TS_PENDING_ACTIVATION -> {
						showDialog(DialogModel(false, R.drawable.ic_subscription_error, it.message, getString(R.string.ok), null), object :
							CommonDialogEventListener {
							override fun onPrimaryButtonClick() {
								hideDialog()
							}

							override fun onSecondaryButtonClick() {
								hideDialog()
							}

							override fun onCloseButtonClick() {
							}
						})
					}
					CODE_SUCCESS -> {
						if (it.data?.url.isNullOrBlank()) {
							onError(ErrorModel(message = "Recharge Cannot be processed please try after some time"))
							return@Observer
						}
						try {
							launchAmazonActivationActivity(this, Uri.parse(it.data?.url),viewModel.getPrimeInterstitialResponse().value?.peekContent()?.packList?.get(0)?.packType?:"PAID", viewModel.source ?: "")
							activity?.finish()
						} catch (e: Exception) {
							onError(ErrorModel(message = "Recharge Cannot be processed please try after some time"))
						}
					}
					else -> {
						onError(ErrorModel(it.code, it.message))
					}
				}
			}
			binding.btnPrimary.isEnabled = true
		})

		viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
			it.getContentIfNotHandled()?.let {
				if(viewModel.firstError)
					activity?.finish()
			}
			binding.btnPrimary.isEnabled = true
		})

		viewModel.getPrimeInterstitialResponse().observe(viewLifecycleOwner, Observer {
			it.getContentIfNotHandled()?.let {response ->
				binding.model = response.data
				response.data?.benefits?.let { it1->
					addView(it1)
				}

				response.data?.firstPrimeLogo?.let{ it1 ->
					transparentImageLoad(binding.firstPrimeLogo, it1)
				}

				response.data?.secondPrimeLogo?.let{ it1 ->
					transparentImageLoad(binding.secondPrimeLogo, it1)
				}

				response.data?.image1?.let { it1 ->
					imageLoad(binding.promotionalBannerIv.findViewById(R.id.prom_banner_child1) ,
						it1
					)
				}

				response.data?.image2?.let { it1 ->
					imageLoad(binding.promotionalBannerIv.findViewById(R.id.prom_banner_child2) ,
						it1
					)
				}

				response.data?.image3?.let { it1 ->
					imageLoad(binding.promotionalBannerIv.findViewById(R.id.prom_banner_child3) ,
						it1
					)
				}

				response.data?.image4?.let { it1 ->
					imageLoad(binding.promotionalBannerIv.findViewById(R.id.prom_banner_child4) ,
						it1
					)
				}
				binding.btnPrimary.setHtmlText(response.packList?.get(0)?.buttonText?:
					getString(R.string.join_prime_template, response.packList?.get(0)?.buttonTitle, response.packList?.get(0)?.getFormattedPrice(), response.packList?.get(0)?.renewalCycle))
				binding.btnSecondary.setOnClickListener {
					sharedPrefs.setExistingPrimeInterstitialClickCount(sharedPrefs.getExistingPrimeInterstitialClickCount() + 1)
					sharedPrefs.setLastExistingPrimeButtonClick(Calendar.getInstance().apply {
						this.set(Calendar.HOUR_OF_DAY, 0)
						this.set(Calendar.MINUTE, 0)
						this.set(Calendar.SECOND, 0)
						this.set(Calendar.MILLISECOND, 0)
					}.timeInMillis)
					(activity as PrimeActivity).navigateToPrime()
				}
				binding.root.show()
			}
		})
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		requireActivity().onBackPressedDispatcher.addCallback(
			this, // LifecycleOwner
			callback
		)
	}
	override fun toBeCalledOnce() {
		viewModel.fetchInterstitialScreenData()
		primeAnalytics.trackPrimePackAdd(viewModel.getPrimeInterstitialResponse().value?.peekContent()?.packList?.get(0)?.packType?:"PAID")
		binding.btnPrimary.setOnClickListener {
			binding.btnPrimary.isEnabled = false
			primeAnalytics.trackPrimePackAddContinue(viewModel.getPrimeInterstitialResponse().value?.peekContent()?.packList?.get(0)?.packType?:"PAID")
			viewModel.activatePrime(viewModel.getPrimeInterstitialResponse().value?.peekContent()?.packList?.get(0)?.packId ?: "")
		}
		binding.tncBody.paint?.isUnderlineText = true
		binding.tncBody.setOnClickListener {
			findNavController().navigateSafe(PrimeInterstitialFragmentDirections.actionPrimeInterstitialFragmentToPrimeTncFragment(binding.tncBody.text.toString()))
		}
	}

	fun addView(benefits: ArrayList<PrimeInterstitialResponse.Benefits>){
		for(i in benefits.indices){
			val view = LayoutInflater.from(activity).inflate(R.layout.layout_amazon_services, binding.amazonServices,false)
			val textView = view.findViewById<TextView>(R.id.service1_tv)
			benefits[i].benefitImage?.let { loadImageTextViewDrawable(textView,it,sharedPrefs.getCloudenieryUrl()) }
			textView.text = benefits[i].benefitText
			binding.amazonServices.addView(view)
		}
	}
}