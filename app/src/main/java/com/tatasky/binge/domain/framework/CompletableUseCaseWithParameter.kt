package com.tatasky.binge.domain.framework

import io.reactivex.Completable

interface CompletableUseCaseWithParameter<P> {
    fun execute(parameter: P): Completable
}
