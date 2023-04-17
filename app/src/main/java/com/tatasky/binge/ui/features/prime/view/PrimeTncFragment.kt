package com.tatasky.binge.ui.features.prime.view

import android.webkit.WebViewClient
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.fragment.navArgs
import com.tatasky.binge.R
import com.tatasky.binge.databinding.FragmentPrimeTncBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseFragment
import com.tatasky.binge.ui.features.prime.viewmodel.PrimeViewModel

class PrimeTncFragment:BaseFragment<FragmentPrimeTncBinding, PrimeViewModel>() {
    private val args by navArgs<PrimeTncFragmentArgs>()

    override fun getViewModelClass(): Class<PrimeViewModel>  = PrimeViewModel::class.java

    override fun layoutId(): Int = R.layout.fragment_prime_tnc

    override fun getViewModelOwner(): ViewModelStoreOwner = this

    override fun setObserver() {
    }

    override fun toBeCalledOnce() {
        binding.webview.webViewClient = WebViewClient()
        binding.webview.settings.javaScriptEnabled = true
        binding.webview.loadUrl(if(!args.url.startsWith("http")) ("https://" + args.url) else args.url)
    }

}