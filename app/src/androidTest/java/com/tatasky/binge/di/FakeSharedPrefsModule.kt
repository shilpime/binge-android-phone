package com.tatasky.binge.di

import android.content.Context
import com.tatasky.binge.app.FakeSharedPrefs
import com.tatasky.binge.data.prefs.SharedPrefs
import com.tatasky.binge.domain.repositories.PrefsRepo
import dagger.Module
import dagger.Provides
import io.mockk.mockk
import javax.inject.Singleton

@Module
class FakeSharedPrefsModule {
    @Singleton
    @Provides
    fun providePrefsRepo(): PrefsRepo {
        return mockk()
    }
}