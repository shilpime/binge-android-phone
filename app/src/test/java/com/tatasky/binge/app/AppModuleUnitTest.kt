package com.tatasky.binge.app

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [
        (FakeUnitTestModule::class)
    ]
)
class AppModuleUnitTest {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }
}