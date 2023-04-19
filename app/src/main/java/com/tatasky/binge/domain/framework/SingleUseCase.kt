package com.tatasky.binge.domain.framework

import io.reactivex.Single


interface SingleUseCase<T> {
    fun execute(): Single<T>
}
