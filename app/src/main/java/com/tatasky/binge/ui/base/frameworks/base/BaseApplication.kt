package com.tatasky.binge.ui.base.frameworks.base

import android.content.Context
import androidx.multidex.MultiDex
import com.tatasky.binge.ui.base.di.AppComponent
import dagger.android.support.DaggerApplication


abstract class BaseApplication : DaggerApplication() {

    abstract val appComponent: AppComponent

    override fun applicationInjector() = appComponent

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}