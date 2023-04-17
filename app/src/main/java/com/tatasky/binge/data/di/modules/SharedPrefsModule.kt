package com.tatasky.binge.data.di.modules

import android.content.Context
import com.tatasky.binge.data.prefs.SharedPrefs
import com.tatasky.binge.domain.repositories.PrefsRepo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class SharedPrefsModule {

    @Singleton
    @Provides
    fun providePrefsRepo(context: Context): PrefsRepo {
        return SharedPrefs(context)
    }
}