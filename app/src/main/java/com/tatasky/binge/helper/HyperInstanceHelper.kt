package com.tatasky.binge.helper

import `in`.juspay.services.HyperServices
import android.content.Context
import com.tatasky.binge.ui.base.frameworks.SingletonHolder
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity

/**
 * Class to get Single instance of Juspay HyperService
 * to be used in Single/Multi-activity integration
 */
class HyperInstanceHelper private constructor(context: Context) {

    var hyperInstance: HyperServices? = null
    init {
        hyperInstance = HyperServices(context as BaseActivity<*>)
    }

    companion object: SingletonHolder<HyperInstanceHelper, Context>(::HyperInstanceHelper)

}
