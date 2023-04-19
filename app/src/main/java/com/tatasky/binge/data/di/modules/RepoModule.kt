package com.tatasky.binge.data.di.modules

import com.tatasky.binge.data.networking.services.CommonService
import com.tatasky.binge.data.repository.CommonRepoImpl
import com.tatasky.binge.domain.repositories.CommonRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepoModule {

    /**
     * Provide all the Repositories from here
     */
    @Singleton
    @Provides
    fun provideAccountRepo(commonService: CommonService): CommonRepository {
        return CommonRepoImpl(commonService)
    }
}