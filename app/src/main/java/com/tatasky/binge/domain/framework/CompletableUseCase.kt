package com.tatasky.binge.domain.framework

import io.reactivex.Completable


interface CompletableUseCase {
    fun execute(): Completable
}