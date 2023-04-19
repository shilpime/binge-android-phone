package com.tatasky.binge.ui.features.recharge

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Observer
import com.tatasky.binge.BuildConfig
import com.tatasky.binge.R
import com.tatasky.binge.analytics.Cancelled
import com.tatasky.binge.analytics.FAILED
import com.tatasky.binge.interfaces.CommonDialogEventListener
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.features.dialog.CommonDialog
import com.tatasky.binge.ui.features.dialog.DialogModel
import com.tatasky.binge.ui.features.home.LandingActivity
import com.tatasky.binge.utils.*
import kotlinx.android.synthetic.main.activity_recharge.*
import javax.inject.Inject


class RechargeActivity : BaseActivity<RechargeViewModel>() {

//    private lateinit var dialogViewModel: DialogViewModel
    private var commonDialog: CommonDialog? = null
    @Inject
    lateinit var localBroadcastHelper: LocalBroadcastHelper

    override fun init(savedInstanceState: Bundle?) {
        if (intent.getStringExtra(RECHARGE_SID) != null && intent.data == null) {
            if (viewModel.sharedPrefs.getLoginStatus())
                viewModel.fetchBalance()
            else
                logoutApplication(this)
        } else {
            if (intent.data == null) {
                setResult(Activity.RESULT_CANCELED)
                finish()
                //            viewModel.startRecharge(intent.getStringExtra(RechargeActivity.RECHARGE_SID), "0")
            } else {
                subscriptionAnalytics.trackViewRechargeScreen()
                startWebView(intent.data!!.toString())
            }
        }
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        supportActionBar?.setDisplayShowTitleEnabled(false);
        viewModel.getWalletBalance().observe(this, Observer {
            it.getContentIfNotHandled()?.let {
                viewModel.startRecharge(viewModel.sharedPrefs.getOriginalSubscriberId(), it.data?.recommendedRechargeAmount!!)
            }
        })

        viewModel.rechargeResponse().observe(this, Observer {
            it.getContentIfNotHandled()?.data?.let {
                if (it.rechargeUrl.isNullOrBlank()) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                } else
                    startWebView(it.rechargeUrl!!)
            }
        })
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


    private fun startWebView(url: String) {
        val webview = findViewById<WebView>(R.id.webview)
        webview.settings.javaScriptEnabled = true
        webview.settings.domStorageEnabled = true
        webview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                try {
                    if (url.startsWith(WebView.SCHEME_TEL)
                        || url.startsWith(WebView.SCHEME_MAILTO)
                        || url.startsWith(WebView.SCHEME_GEO)
                    ) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent) // view.context.startActivity(intent);
                        return true
                    }
                    if (url.contains(BuildConfig.hostName + "/payment?status=success")) {
                        subscriptionAnalytics.trackRechargeSuccess()
                        setResult(Activity.RESULT_OK, Intent().apply { putExtra("rechargeStatus", 1) })
                        finish()
                    } else if (url.contains(BuildConfig.hostName + "/payment?status=fail")) {
                        subscriptionAnalytics.trackRechargeFailure(FAILED)
                        setResult(Activity.RESULT_OK, Intent().apply { putExtra("rechargeStatus", 0) })
                        finish()
                    } else{
                        view.loadUrl(url)
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                return true
            }

            //Show loader on url load
            override fun onLoadResource(view: WebView, url: String) {}
            override fun onPageFinished(view: WebView, url: String) {
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
        subscriptionAnalytics.trackRechargeFailure(Cancelled)
        if (isTaskRoot) {
            startActivity(Intent(this, LandingActivity::class.java))
            // using finish() is optional, use it if you do not want to keep currentActivity in stack
            finish()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        val RECHARGE_REQUEST_CODE = 1221
        val RECHARGE_SID = "sid"
    }

    override fun getContentViewId(): Int = R.layout.activity_recharge

    override fun getRootLayoutContainer(): View = root_container

    override fun getViewModelClass(): Class<RechargeViewModel> = RechargeViewModel::class.java
}