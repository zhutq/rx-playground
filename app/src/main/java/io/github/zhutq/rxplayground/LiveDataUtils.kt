package io.github.zhutq.rxplayground

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.functions.Function
import timber.log.Timber

class LiveDataUtils {
    companion object {
        fun <T> fromObservable(errorMessage: String): Function<Observable<T>, LiveData<T>> {
            return Function<Observable<T>, LiveData<T>> { observable: Observable<T> ->
                observable
                    .doOnError { Timber.w(it, errorMessage) }
                    .onErrorResumeNext(Observable.empty())
                    .toFlowable(BackpressureStrategy.LATEST)
                    .to { LiveDataReactiveStreams.fromPublisher(it) }
            }
        }
    }
}