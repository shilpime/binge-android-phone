package com.tatasky.binge.ui.features.onboarding.marketing

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.tatasky.binge.R
import com.tatasky.binge.ui.base.frameworks.base.BaseActivity
import kotlinx.android.synthetic.main.activity_marketing.*


class MarketingActivity: BaseActivity<MarketingViewModel>() {
    override fun getContentViewId(): Int {
        return R.layout.activity_marketing
    }

    override fun getRootLayoutContainer(): View {
        return root_container_marketing
    }

    override fun getViewModelClass(): Class<MarketingViewModel> {
        return MarketingViewModel::class.java
    }

    override fun init(savedInstanceState: Bundle?) {
//        window?.statusBarColor = ColorUtils.setAlphaComponent(ContextCompat.getColor(this, R.color.light_blue_grey), (255*.1).toInt())
    }

}