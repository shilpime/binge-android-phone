package com.tatasky.binge.ui.features.prime.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.transition.MaterialSharedAxis
import com.tatasky.binge.R
import com.tatasky.binge.analytics.SOURCE_HOME
import com.tatasky.binge.data.networking.models.ErrorModel
import com.tatasky.binge.databinding.FragmentPrimeRechargeBinding
import com.tatasky.binge.helper.imageLoad
import com.tatasky.binge.helper.transparentImageLoad
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.closeKeyboard
import com.tatasky.binge.ui.base.frameworks.extensions.hide
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.prime.PrimeAnalytics
import com.tatasky.binge.ui.features.prime.viewmodel.PrimeViewModel
import com.tatasky.binge.ui.features.recharge.RechargeActivity
import com.tatasky.binge.ui.features.recharge.launchAmazonActivationActivity
import com.tatasky.binge.ui.features.recharge.launchRechargeActivity
import com.tatasky.binge.utils.*
import javax.inject.Inject
import kotlin.math.floor

class PrimeRechargeFragment : BaseFragment<FragmentPrimeRechargeBinding, PrimeViewModel>() {
	@Inject
	lateinit var primeAnalytics : PrimeAnalytics
	private val args by navArgs<PrimeRechargeFragmentArgs>()
	private val mHandler = Handler(Looper.getMainLooper())

	override fun getViewModelClass(): Class<PrimeViewModel> = PrimeViewModel::class.java

	override fun layoutId(): Int = R.layout.fragment_prime_recharge

	override fun getViewModelOwner() = activity as PrimeActivity

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

	override fun setObserver() {
		viewModel.progressListener.removeObservers(viewLifecycleOwner)
		viewModel.progressListener.observe(viewLifecycleOwner, Observer {
			if (it) {
				mHandler.postDelayed(
					showProgress, loaderDelayTime
				)
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
						toBeCalledOnce()
					}
					PRIME_NOT_SUBSCRIBED_BINGE, PRIME_INACTIVE_SUBSCRIBED_BINGE -> { //Not subscribed to binge
						showDialog(DialogModel(false, R.drawable.ic_subscription_error, it.message, getString(R.string.proceed), getString(R.string.cancel)), object :
							CommonDialogEventListener {
							override fun onPrimaryButtonClick() {
								startActivity(getSubscriptionActivityIntent(context, false, null, SOURCE_HOME, true))
								hideDialog()
								activity?.finish()
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
							launchAmazonActivationActivity(this, Uri.parse(it.data?.url), args.primePack.packType?:"PAID", viewModel.source ?: "")
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
		})

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
	}

	override fun toBeCalledOnce() {
		binding.tvMsg.text = args.message
		primeAnalytics.trackPrimeLowBalance(args.primePack.packType?:"PAID")
		binding.layoutPrimePack.tvPrice.text =
			getString(R.string.rupees_cyclic, "${args.primePack.getFormattedPrice()}", args.primePack.renewalCycle)
		binding.etRecharge.setText((floor(args.primePack.recommendedAmount.toString().toFloatOrNull() ?: 0f)).toInt().toString())
		binding.etRecharge.isEnabled = false
		binding.layoutPrimePack.tvExpiryMsg.hide()
		binding.btnCancel.setOnClickListener {
			primeAnalytics.trackPrimeLowBalanceCancel(args.primePack.packType?:"PAID")
			findNavController().popBackStack()
		}
		transparentImageLoad(binding.layoutPrimePack.packIcon, args.primePack.iconUrl?:"")
		binding.btnRecharge.setOnClickListener {
			val amount = (binding.etRecharge.text.toString().toFloatOrNull() ?: 0f).toInt()
			val rsToPay = (floor(args.primePack.minimumRecharge.toString().toFloatOrNull() ?: 0f)).toInt()
			if (amount < rsToPay) {
				binding.tilRecharge.error = getString(R.string.recharge_error_min, rsToPay)
				return@setOnClickListener
			} else if (amount > viewModel.getMaximumRechargeAmount()) {
				binding.tilRecharge.error =
					getString(R.string.recharge_error_max, viewModel.getMaximumRechargeAmount())
				return@setOnClickListener
			} else {
				viewModel.startRecharge(amount.toString())
			}
		}
		binding.ivInfo.setOnClickListener {
			showDialog(DialogModel(false, R.drawable.ic_info1, getString(R.string.amazon_recommended_recharge_msg) , getString(R.string.ok), null), object : CommonDialogEventListener{
				override fun onPrimaryButtonClick() {
					hideDialog()
				}

				override fun onSecondaryButtonClick() {
				}

				override fun onCloseButtonClick() {
				}
			})
		}
	}

	override fun onResume() {
		super.onResume()
		binding.toolbarLayout.findViewById<ViewGroup>(R.id.toolbar_layout)?.findViewById<Toolbar>(R.id.toolbar)?.let {
			it.setNavigationOnClickListener{
				findNavController().navigateUp()
				primeAnalytics.trackPrimeLowBalanceCancel(args.primePack.packType?:"PAID")
			}
		}
	}

	val callback: OnBackPressedCallback = object : OnBackPressedCallback(
		true // default to enabled
	) {
		override fun handleOnBackPressed() {
			try {
				primeAnalytics.trackPrimeLowBalanceCancel(args.primePack.packType?:"PAID")
			}catch (e:Exception){}
			isEnabled = false
			activity?.onBackPressed()
		}
	}

	override fun onAttach(context: Context) {
		super.onAttach(context)
		requireActivity().onBackPressedDispatcher.addCallback(
			this, // LifecycleOwner
			callback
		)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == RechargeActivity.RECHARGE_REQUEST_CODE) {
			if (1 == data?.getIntExtra("rechargeStatus", 0)) {
				showDialog(DialogModel(false, R.drawable.ic_success_tick, getString(R.string.payment_success), getString(R.string.continue_to_prime), null, getString(R.string.recharge_success_amazon)), object :
					CommonDialogEventListener {
					override fun onPrimaryButtonClick() {
						hideDialog()
						viewModel.activatePrime(args.primePack.packId ?: "")
					}

					override fun onSecondaryButtonClick() {
					}

					override fun onCloseButtonClick() {
					}
				})
			} else {
				try {
					primeAnalytics.trackPrimeLowBalanceCancel(args.primePack.packType ?: "PAID")
				} catch (e: Exception) { }
				showDialog(DialogModel(false, R.drawable.ic_subscription_error, getString(R.string.payment_failure), getString(R.string.ok), null, null), object :
					CommonDialogEventListener {
					override fun onPrimaryButtonClick() {
						hideDialog()
					}

					override fun onSecondaryButtonClick() {
					}

					override fun onCloseButtonClick() {
					}
				})
			}
		}
	}
}