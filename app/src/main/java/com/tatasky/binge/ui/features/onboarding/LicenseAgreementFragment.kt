package com.tatasky.binge.ui.features.onboarding

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentLicenseAgreementBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.base.frameworks.extensions.show
import com.tatasky.binge.ui.features.more.MoreAnalytics
import com.tatasky.binge.ui.features.more.SettingsViewModel
import com.tatasky.binge.utils.isNetworkConnected
import javax.inject.Inject


/**
 * Created by Srikant Karnani on 12/3/21.
 */
class LicenseAgreementFragment :
    BaseFragment<FragmentLicenseAgreementBinding, SettingsViewModel>() {

    override fun getViewModelClass(): Class<SettingsViewModel> = SettingsViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_license_agreement

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    override fun setObserver() {
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigateUp()
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        binding.toolbarLayout.show()
    }

    override fun toBeCalledOnce() {
        if (isNetworkConnected(requireContext())) {
            binding.webview.settings.javaScriptEnabled = true
            binding.webview.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.darkBackground
                )
            )
            binding.webview.loadUrl(viewModel.sharedPrefs.getEulaUrl() ?: "")
//        binding.tvTnc.loadUrl("https://tatasky-qa-anywhere-web-app.videoready.tv/eula")
            binding.webview.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    viewModel.setProgressing(true)
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    viewModel.setProgressing(false)
                    super.onPageCommitVisible(view, url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    viewModel.setProgressing(false)
//                    injectCSS()
                    super.onPageFinished(view, url)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: ""
                    try {
                        val toOpenUrl: String = Uri.parse(viewModel.sharedPrefs.getEulaUrl() ?: "")
                            .let { it.encodedAuthority + it.encodedPath } ?: ""
                        if (url.startsWith(WebView.SCHEME_TEL)
                            || url.startsWith(WebView.SCHEME_MAILTO)
                            || url.startsWith(WebView.SCHEME_GEO)
                        ) {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(url)
                            startActivity(intent) // view.context.startActivity(intent);
                            return true
                        } else if (url.contains(toOpenUrl ?: "", true)) {
                            return super.shouldOverrideUrlLoading(view, request)
                        }

                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent) // view.context.startActivity(intent);
                        return true
//                    return super.shouldOverrideUrlLoading(view, request)
                    } catch (e: Exception) {
                        return super.shouldOverrideUrlLoading(view, request)
                    }
                }
            }
        } else {
            onNetworkError("", false)
        }
    }

    private fun injectCSS() {
        try {
            val inputStream = context?.assets?.open("style.css")
            inputStream?.let {
                val buffer = ByteArray(inputStream.available())
                inputStream.read(buffer)
                inputStream.close()
                val encoded = Base64.encodeToString(buffer, Base64.NO_WRAP)
                binding.webview.loadUrl(
                    "javascript:(function() {" +
                            "var parent = document.getElementsByTagName('head').item(0);" +
                            "var style = document.createElement('style');" +
                            "style.type = 'text/css';" +
                            // Tell the browser to BASE64-decode the string into your script !!!
                            "style.innerHTML = window.atob('" + encoded + "');" +
                            "parent.appendChild(style)" +
                            "})()"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}