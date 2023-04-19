package com.tatasky.binge.ui.base.di.modules

import com.tatasky.binge.domain.repositories.CommonRepository
import com.tatasky.binge.domain.usecase.CommonUseCase
import dagger.Module
import dagger.Provides

@Module
class UseCaseModule {
    @Provides
    fun provideApprovalUseCase(userAccountRepository: CommonRepository): CommonUseCase {
        return CommonUseCase(userAccountRepository)
    }
}