package com.tatasky.binge.app

import com.tatasky.binge.di.FakeNetworkModule
import com.tatasky.binge.test.BuildConfig
import com.tatasky.binge.ui.base.MyApp
import com.tatasky.binge.ui.base.di.AppComponent
import com.tatasky.binge.ui.base.di.DaggerAppComponent
import okhttp3.mockwebserver.MockWebServer
import java.util.logging.Handler

/**
 * Helps to configure required dependencies for Instru Tests.
 * Method provideDependency can be overrided and new dependencies can be supplied.
 */
class TestMyApp : MyApp() {

    override var appComponent: TestAppComponent =
        DaggerTestAppComponent.builder()
            .application(this)
            .network(FakeNetworkModule("https://127.0.0.1"))
            .build()

}