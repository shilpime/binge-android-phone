package com.tatasky.binge.app

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Custom Instrumentation Test runner.
 * Helps to configure environment with new App instance.
 */
class CustomInstrumentationRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader,
                                className: String,
                                context: Context): Application {
        return super.newApplication(cl,
            TestMyApp::class.java.name,
            context)
    }
}