package com.tatasky.binge.data.di.modules

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.tatasky.binge.data.database.AppDatabase
import com.tatasky.binge.data.database.dao.LADao
import com.tatasky.binge.data.database.dao.PacksDao
import com.tatasky.binge.data.database.dao.TokenDao
import com.tatasky.binge.domain.repositories.PrefsRepo
import com.tatasky.binge.domain.usecase.CommonUseCase
import com.tatasky.binge.learnactions.LearnActionHelper
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Created by Srikant Karnani on 27/2/20.
 */
@Module
class DatabaseModule {

    @Singleton
    @Provides
    internal fun providesRoomDatabase(mApplication: Application): AppDatabase {
        return Room.databaseBuilder(mApplication, AppDatabase::class.java, "binge_anywhere-db")
            .allowMainThreadQueries()
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    @Singleton
    @Provides
    fun provideLAHelper(
        context: Context,
        laDao: AppDatabase,
        sharedPrefs: PrefsRepo,
        commonUseCase: CommonUseCase
    ): LearnActionHelper {
        return LearnActionHelper(context, laDao, sharedPrefs, commonUseCase)
    }

    @Singleton
    @Provides
    internal fun providesPacksDao(demoDatabase: AppDatabase): PacksDao {
        return demoDatabase.packsDao
    }

    @Singleton
    @Provides
    internal fun providesLADao(demoDatabase: AppDatabase): LADao {
        return demoDatabase.laDao
    }
    @Singleton
    @Provides
    internal fun providesTokenDao(demoDatabase: AppDatabase): TokenDao {
        return demoDatabase.tokenDao
    }
}