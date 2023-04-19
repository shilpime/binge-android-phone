package com.tatasky.binge.ui.features.more

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.findNavController
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentPrivacypolicyBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.sidemenunavdrawer.SideMenuDrawerAnalytics
import com.tatasky.binge.utils.isNetworkConnected
import javax.inject.Inject


/**
 * Created by Srikant Karnani on 16/1/20.
 */
class PrivacyPolicyFragment : BaseFragment<FragmentPrivacypolicyBinding, SettingsViewModel>() {
    @Inject
    lateinit var sideMenuDrawerAnalytics: SideMenuDrawerAnalytics

    override fun getViewModelClass(): Class<SettingsViewModel> = SettingsViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_privacypolicy

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    override fun setObserver() {
        viewModel.errorOkClicked.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {
                findNavController().navigateUp()
            }
        })
    }

    override fun toBeCalledOnce() {
        sideMenuDrawerAnalytics.trackPrivacyPolicyVisit()
        if (isNetworkConnected(requireContext())) {
            binding.tvPrivacyPolicy.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.darkBackground
                )
            )
//        binding.tvPrivacyPolicy.loadUrl("https://tatasky-qa-anywhere-web-app.videoready.tv/eula")
            binding.tvPrivacyPolicy.loadUrl(viewModel.sharedPrefs.getPrivacyPolicyUrl() ?: "")
            binding.tvPrivacyPolicy.webViewClient = object : WebViewClient() {
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
                    super.onPageFinished(view, url)
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url?.toString() ?: ""
                    try {
                        val toOpenUrl: String = Uri.parse(viewModel.sharedPrefs.getPrivacyPolicyUrl() ?: "")
                            .let { it.encodedAuthority + it.encodedPath } ?: ""
                        if (url.startsWith(WebView.SCHEME_TEL)
                            || url.startsWith(WebView.SCHEME_MAILTO)
                            || url.startsWith(WebView.SCHEME_GEO)
                        ) {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(url)
                            startActivity(intent) // view.context.startActivity(intent);
                            return true
                        } else if (url.contains(
                                toOpenUrl ?: "", true
                            )
                        ) {
                            return super.shouldOverrideUrlLoading(view, request)
                        }
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent) // view.context.startActivity(intent);
                        return true
                    } catch (e: Exception) {
                        return super.shouldOverrideUrlLoading(view, request)
                    }
                }

            }
        } else {
            onNetworkError("", false)
        }
    }
}