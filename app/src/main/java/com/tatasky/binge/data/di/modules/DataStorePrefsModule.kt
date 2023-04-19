package com.tatasky.binge.data.di.modules

import android.content.Context
import com.tatasky.binge.data.datastore.DataStorePrefs
import com.tatasky.binge.domain.repositories.DataStorePrefsRepo
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DataStorePrefsModule {

    @Singleton
    @Provides
    fun provideDataStorePrefsRepo(context: Context): DataStorePrefsRepo {
        return DataStorePrefs(context)
    }

}