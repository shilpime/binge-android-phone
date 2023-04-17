package com.tatasky.binge.ui.features.prime.view

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.tatasky.binge.R
import com.tatasky.binge.analytics.PARA_SOURCE
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.dialog.DialogViewModel
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.ui.features.prime.PrimeAnalytics
import com.tatasky.binge.ui.features.recharge.RechargeViewModel
import com.tatasky.binge.utils.*
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_recharge.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PrimeActivationActivity : BaseActivity<RechargeViewModel>() {

//	private lateinit var dialogViewModel: DialogViewModel
	@Inject
	lateinit var primeAnalytics: PrimeAnalytics

	@Inject
	lateinit var localBroadcastHelper: LocalBroadcastHelper

	override fun init(savedInstanceState: Bundle?) {
		if (intent.data == null) {
			setResult(Activity.RESULT_CANCELED)
			finish()
		} else {
			startWebView(intent.data!!.toString())
		}
		localBroadcastHelper.registerBroadcast(this, mBroadcastReceiver, localBroadcastHelper.ACTION_PRIME)

		setSupportActionBar(findViewById(R.id.toolbar))
		supportActionBar?.setDisplayHomeAsUpEnabled(true);
		supportActionBar?.setDisplayShowTitleEnabled(false);
		viewModel.forceLogout.observe(this, Observer {
			logoutApplication(this)
		})

		viewModel.errorMessage.observe(this, Observer {
			it.getContentIfNotHandled()?.let { errorModel ->
				if (errorModel.statusCode == RESPONSE_CODE_NETWORK_ERROR) {
					showDialog(
						DialogModel(false, R.drawable.ic_internet, getString(R.string.network_title), getString(R.string.ok), null,
							getString(R.string.network_error_message)),
						object : CommonDialogEventListener {
							override fun onPrimaryButtonClick() {
								setResult(Activity.RESULT_OK, Intent().apply { putExtra("rechargeStatus", 0) })
								finish()
							}

							override fun onSecondaryButtonClick() {
							}

							override fun onCloseButtonClick() {
							}
						})
				} else if (errorModel.statusCode == RESPONSE_CODE_SUCCESS || errorModel.statusCode == CODE_SUCCESS) {
					showDialog(
						DialogModel(false, null, errorModel.message, "Ok", null),
						object : CommonDialogEventListener {
							override fun onPrimaryButtonClick() {
								setResult(Activity.RESULT_OK, Intent().apply { putExtra("rechargeStatus", 0) })
								finish()
							}

							override fun onCloseButtonClick() {
							}

							override fun onSecondaryButtonClick() {
							}
						})
				} else {
					showDialog(
						DialogModel(false, R.drawable.ic_subscription_error, COMMON_ERROR_TITLE, "Ok", null, COMMON_ERROR_MSG, errorModel.statusCode),
						object : CommonDialogEventListener {
							override fun onPrimaryButtonClick() {
								setResult(Activity.RESULT_OK, Intent().apply { putExtra("rechargeStatus", 0) })
								finish()
							}

							override fun onCloseButtonClick() {
							}

							override fun onSecondaryButtonClick() {
							}
						})
				}
			}
		})
	}
	var disposable : Disposable? =null
	val mBroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			if(localBroadcastHelper.ACTION_PRIME == intent.action){
				if(sharedPrefs.isPrimeRedirectionEnabled()){
					disposable = Completable.timer(
						sharedPrefs.getPrimeRedirectionDelay().toLong(),
						TimeUnit.SECONDS,
						AndroidSchedulers.mainThread()
					).subscribe { startHomeScreen(this@PrimeActivationActivity) }
				}
			}
		}
	}

	private fun startWebView(url: String) {
		val webview = findViewById<WebView>(R.id.webview)
		webview.settings.javaScriptEnabled = true
		webview.webViewClient = object : WebViewClient() {
			override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
				try {
					if (url.startsWith(WebView.SCHEME_TEL)
						|| url.startsWith(WebView.SCHEME_MAILTO)
						|| url.startsWith(WebView.SCHEME_GEO)
						|| url.startsWith("market:")
					) {
						val intent = Intent(Intent.ACTION_VIEW)
						intent.data = Uri.parse(url)
						startActivity(intent) // view.context.startActivity(intent);
						return true
					}
					view.loadUrl(url)
				} catch (exception: Exception) {
					exception.printStackTrace()
				}
				return true
			}

			//Show loader on url load
			override fun onLoadResource(view: WebView, url: String) {}
			override fun onPageFinished(view: WebView, url: String) {
				//                try {
				//                    if (url.contains("binge-login")) {
				//                        setResult(Activity.RESULT_OK, Intent().apply { putExtra("rechargeStatus", 1) })
				//                        finish()
				//                    }
				//                } catch (exception: Exception) {
				//                    exception.printStackTrace()
				//                }
			}
		}

		//Load url in webView
		webview.loadUrl(url)
	}

	override fun onSupportNavigateUp(): Boolean {
		onBackPressed()
		return true
	}

	override fun onBackPressed() {
		primeAnalytics.trackPrimePackAddSkip(intent.extras?.get(AMAZON_TYPE)?.toString()?:"PAID", intent.extras?.getString(
			PARA_SOURCE))
		if (isTaskRoot) {
			startActivity(Intent(this, LandingActivity::class.java))
			// using finish() is optional, use it if you do not want to keep currentActivity in stack
			finish()
		} else {
			super.onBackPressed()
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		disposable?.dispose()
		localBroadcastHelper.unregisterBroadcast(this, mBroadcastReceiver)
	}

	companion object {
		val RECHARGE_REQUEST_CODE = 1222
		val AMAZON_TYPE = "amazon_pack_type"
	}

	override fun getContentViewId(): Int = R.layout.activity_recharge

	override fun getRootLayoutContainer(): View = root_container

	override fun getViewModelClass(): Class<RechargeViewModel> = RechargeViewModel::class.java
}