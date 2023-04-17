package com.tatasky.binge.di

import android.app.Application
import com.tatasky.binge.data.di.modules.NetworkModule
import com.tatasky.binge.data.networking.ApplicationApis
import com.tatasky.binge.data.networking.services.CommonService
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.pubnub.LocalBroadcastHelper
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class FakeNetworkModule(val baseUrl : String) {

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .client(OkHttpClient.Builder().callTimeout(10L, TimeUnit.SECONDS).connectTimeout(10L, TimeUnit.SECONDS).build())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrl)
            .build()
    }

    @Singleton
    @Provides
    fun provideUserAccountService(retrofit: Retrofit): CommonService {
        return CommonService(retrofit.create(ApplicationApis::class.java))
    }
}