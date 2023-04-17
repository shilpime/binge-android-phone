package com.tatasky.binge.base

import android.net.SSLCertificateSocketFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.tatasky.binge.app.DaggerTestAppComponent
import com.tatasky.binge.app.TestMyApp
import com.tatasky.binge.di.FakeNetworkModule
import com.tatasky.binge.test.BuildConfig
import com.tatasky.binge.ui.base.frameworks.base.BaseViewModel
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import java.io.BufferedReader
import java.io.Reader

abstract class BaseViewModelTest<T : BaseViewModel> {

    /**
     * For MockWebServer instance
     */
    lateinit var mockServer: MockWebServer

    lateinit var viewModel: T

    /**
     * Default, let server be shut down
     */
    private var mShouldStart = false

    @Before
    open fun setUp() {
        val app =
            InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestMyApp
        startMockServer(true)
        app.appComponent = DaggerTestAppComponent.builder()
            .application(app)
            .network(FakeNetworkModule(getMockWebServerUrl()))
            .build()
        app.appComponent.inject(app)
    }

    /**
     * Helps to read input file returns the respective data in mocked call
     */
    fun mockNetworkResponseWithFileContent(fileName: String, responseCode: Int) =
        mockServer.enqueue(
            MockResponse()
                .setResponseCode(responseCode)
                .setBody(getJson(fileName))
        )

    /**
     * Reads input file and converts to json
     */
    fun getJson(path: String): String {
        var content: String = ""
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val inputStream = testContext.assets.open(path)
        val reader = BufferedReader(inputStream.reader() as Reader?)
        reader.use { reader ->
            content = reader.readText()
        }
        return content
    }

    /**
     * Start Mockwebserver
     */
    private fun startMockServer(shouldStart: Boolean) {
        if (shouldStart) {
            mShouldStart = shouldStart
            mockServer = MockWebServer()
            mockServer.start(8080)
        }
    }

    /**
     * Set Mockwebserver url
     */
    fun getMockWebServerUrl() =
        mockServer.url("/").toString()

    /**
     * Stop Mockwebserver
     */
    private fun stopMockServer() {
        if (mShouldStart) {
            mockServer.shutdown()
        }
    }

    @After
    open fun tearDown() {
        //Stop Mock server
        stopMockServer()
    }
}