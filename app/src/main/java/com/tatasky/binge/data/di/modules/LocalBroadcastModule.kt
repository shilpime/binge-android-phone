package com.tatasky.binge.data.di.modules

import android.content.Context
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class LocalBroadcastModule {
    @Singleton
    @Provides
    fun provideLocalBroadcast(context: Context): LocalBroadcastHelper {
        return LocalBroadcastHelper()
    }
}