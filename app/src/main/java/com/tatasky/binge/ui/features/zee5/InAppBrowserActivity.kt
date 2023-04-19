package com.tatasky.binge.ui.features.zee5

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import androidx.core.content.ContextCompat
import com.tatasky.binge.R
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import com.tatasky.binge.ui.base.frameworks.extensions.startProgressAvd
import com.tatasky.binge.ui.features.common.CommonSampleViewModel
import com.tatasky.binge.utils.e
import kotlinx.android.synthetic.main.activity_recharge.*


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class InAppBrowserActivity : BaseActivity<CommonSampleViewModel>() {

    private lateinit var webview: WebView
    private val CHROME_PACKAGE_NAME = "com.android.chrome"

    override fun init(savedInstanceState: Bundle?) {
        webview = findViewById<WebView>(R.id.webview)

        if (intent.data != null) {
            /*if(getCustomTabsPackages()?.find { it.activityInfo.packageName == CHROME_PACKAGE_NAME } != null){
                try {
                    val builder = CustomTabsIntent.Builder()
                    builder.setShowTitle(false)
                    builder.setCloseButtonIcon(
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.ic_back
                        )
                    )
                    builder.setUrlBarHidingEnabled(true)
                    builder.setDefaultColorSchemeParams(
                        CustomTabColorSchemeParams.Builder()
                            .setToolbarColor(ContextCompat.getColor(this, R.color.darkBackground))
                            .build()
                    )
                    val customTabsIntent = builder.build()
                    customTabsIntent.intent.setPackage(CHROME_PACKAGE_NAME)
                    customTabsIntent.launchUrl(this, intent.data!!)
                    finish()
//            }
                }
                catch (e : Exception){
                    startWebView(intent.data!!.toString())
                }
            }
            else{*/
//                val builder = CustomTabsIntent.Builder()
//                val customTabsIntent = builder.build()
//                customTabsIntent.launchUrl(this,intent.data!!)
                startWebView(intent.data!!.toString())
//            }
        } else {
            finish()
        }
    }

    fun showProgressbar() {
        e("BaseActivity", "inside showProgress")
        val loaderView = findViewById<View>(R.id.progressBar)
        if (loaderView is ImageView)
            (loaderView as ImageView).startProgressAvd(true)
    }

    fun hideProgressbar() {
        e("BaseActivity", "inside hideProgress")
        val loaderView = findViewById<View>(R.id.progressBar)
        if (loaderView is ImageView)
            (loaderView as ImageView).startProgressAvd(false)
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun startWebView(url: String) {
        webview.settings.javaScriptEnabled = true
        showProgressbar()
        webview.setBackgroundColor(ContextCompat.getColor(this, R.color.darkBackground))
        webview.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                val userAgent = webview.settings.userAgentString
                e("InAppBrowserActivity", "inside onPageStarted $url userAgent:$userAgent")
                viewModel.setProgressing(true)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                e("InAppBrowserActivity", "insideCommit Visible $url")
                hideProgressbar()
                super.onPageCommitVisible(view, url)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                e("InAppBrowserActivity", "inside finished $url")
                hideProgressbar()
                super.onPageFinished(view, url)
            }

            /*override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.proceed()
                e("InAppBrowserActivity", "inside onReceivedSslError $error")
                super.onReceivedSslError(view, handler, error)
            }*/

        }
//        webview.settings.loadWithOverviewMode = true;
        webview.settings.useWideViewPort = true
//        webview.settings.domStorageEnabled = true

        webview.setWebChromeClient(ChromeClient(this))
        //Load url in webView
        webview.loadUrl(url)
    }

    private class ChromeClient(private val context: Activity) : WebChromeClient() {
        private var mCustomView: View? = null
        private var mCustomViewCallback: WebChromeClient.CustomViewCallback? = null
        protected var mFullscreenContainer: FrameLayout? = null
        private var mOriginalOrientation = 0
        private var mOriginalSystemUiVisibility = 0
        private val FULL_SCREEN_SETTING = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE

        override fun getDefaultVideoPoster(): Bitmap? {
            return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        }

        override fun onHideCustomView() {
            (context.window.decorView as FrameLayout).removeView(mCustomView)
            mCustomView = null
            context.window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
            context.requestedOrientation = mOriginalOrientation
            mCustomViewCallback?.onCustomViewHidden()
            mCustomViewCallback = null
        }

        override fun onShowCustomView(
            paramView: View?,
            paramCustomViewCallback: WebChromeClient.CustomViewCallback?
        ) {
            if (mCustomView != null) {
                onHideCustomView()
                return
            }
            mCustomView = paramView
            mOriginalSystemUiVisibility = context.window.decorView.systemUiVisibility
            mOriginalOrientation = context.requestedOrientation
            mCustomViewCallback = paramCustomViewCallback
            (context.window.decorView as FrameLayout).addView(
                mCustomView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            context.window.decorView.systemUiVisibility = FULL_SCREEN_SETTING
            context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_USER
            mCustomView!!.setOnSystemUiVisibilityChangeListener { visibility: Int -> updateControls() }
        }

        private fun updateControls() {
            val params = this.mCustomView?.layoutParams as FrameLayout.LayoutParams
            params.bottomMargin = 0
            params.topMargin = 0
            params.leftMargin = 0
            params.rightMargin = 0
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            params.width = ViewGroup.LayoutParams.MATCH_PARENT
            this.mCustomView?.setLayoutParams(params)
            context.window.decorView.systemUiVisibility = FULL_SCREEN_SETTING;
        }
    }

    override fun getContentViewId(): Int = R.layout.activity_inapp_browser

    override fun getRootLayoutContainer(): View = root_container

    override fun getViewModelClass(): Class<CommonSampleViewModel> =
        CommonSampleViewModel::class.java

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action === KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (webview.canGoBack()) {
                        webview.goBack()
                    } else {
                        webview.destroy()
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}