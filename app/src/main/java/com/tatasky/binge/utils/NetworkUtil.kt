package com.tatasky.binge.utils

import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.DataBindingUtil
import com.tatasky.binge.R
import com.tatasky.binge.databinding.LayoutNoInternetToastBinding
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity

object NetworkUtil {
    private var mContext: Context? = null
    fun initialize(context: Context) {
        mContext = context
    }

    fun checkInternetBeforeNavigate(): Boolean {
        mContext?.let {
            if (!isNetworkConnected(it)) {
                showToast(it,it.getString(R.string.network_title), R.drawable.ic_internet_small)
                return false
            }
        }
        return true
    }
}