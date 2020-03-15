package io.github.zhutq.rxplayground

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert.assertEquals
import org.junit.Test

class RxTest {
    @Test
    fun testSingle() {
        val single = Single.create { emitter: SingleEmitter<String> ->
            emitter.onSuccess("single object")
        }
        val disposable = single.subscribe { x: String -> println(x) }
        disposable.dispose()

        val testObserver = single.test()
        testObserver.assertValue("single object")
        testObserver.assertComplete()
    }

    @Test
    fun testReplay() {
        var createCount = 0
        var processCount = 0
        val replay: Observable<String> = Single
            .fromCallable {
                println("single created") // only once with only once with .replay(1).refCount()
                createCount++
                "abc"
            }
            .flatMapObservable {
                Observable.create { emitter: ObservableEmitter<String> ->
                    println("flatMapObservable") // only once with .replay(1).refCount()
                    processCount++
                    emitter.onNext(it) // note that it's not calling onComplete
                }
            }
            .replay(1).refCount()
        replay.doOnNext { println("1: $it") }.subscribe()
        replay.doOnNext { println("2: $it") }.subscribe()

        assertEquals(createCount, 1)
        assertEquals(processCount, 1)
    }

    @Test
    fun testReplay2() {
        var createCount = 0
        var processCount = 0
        val replay: Observable<String> = Single
            .fromCallable {
                println("single created") // twice even with .replay(1).refCount()
                createCount++
                "abc"
            }
            .flatMapObservable {
                Observable.create { emitter: ObservableEmitter<String> ->
                    println("flatMapObservable") // twice even with .replay(1).refCount()
                    processCount++
                    emitter.onNext(it)
                    emitter.onComplete() // source ends, subscribe/unsubscribe for each subscriber
                }
            }
            .replay(1).refCount()
        replay.doOnNext { println("1: $it") }.subscribe()
        replay.doOnNext { println("2: $it") }.subscribe()

        assertEquals(createCount, 2)
        assertEquals(processCount, 2)
    }

    @Test
    fun testShare() {
        // https://github.com/ReactiveX/RxJava/issues/4995
        // With share(), if the upstream terminates, the internal subscriber count is set to zero.
        // If a new subscriber comes in, that makes share() resubscribe to the upstream.
        val fromCallable: Observable<String> =
            Observable.fromCallable {
                println("callable called") // still twice
                System.currentTimeMillis().toString()
            }.share()
        fromCallable.doOnNext { println("fromCallable 1: $it") }.subscribe()
        fromCallable.doOnNext { println("fromCallable 2: $it") }.subscribe()

        println()

        // share won't replay
        val observable = Observable
            .create { emitter: ObservableEmitter<String> ->
                val text = System.currentTimeMillis().toString()
                println("observable emit $text")
                emitter.onNext(text)
            }
            .share()
        observable.doOnNext { println("subscriber 1: $it") }.subscribe() // called
        observable.doOnNext { println("subscriber 2: $it") }.subscribe() // not called

        println()

        val replay = Observable
            .create { emitter: ObservableEmitter<String> ->
                val text = System.currentTimeMillis().toString()
                println("observable emit $text")
                emitter.onNext(text)
            }
            .replay(1).refCount()
        replay.doOnNext { println("replay 1: $it") }.subscribe() // called
        replay.doOnNext { println("replay 2: $it") }.subscribe() // called
    }

    @Test
    fun testSchedulers() {
        val behaviorSubject =
            BehaviorSubject.createDefault(System.currentTimeMillis())
        behaviorSubject
            .doOnNext { println("$it ${Thread.currentThread().name}") }
            .subscribeOn(Schedulers.io())
            .subscribe()

        Thread.sleep(10)
        behaviorSubject.onNext(System.currentTimeMillis()) // main

        Thread.sleep(10)
    }

    @Test
    fun testSchedulersSwitch() {
        val behaviorSubject = BehaviorSubject.createDefault(0)
        behaviorSubject
            .switchMap {
                if (it % 2 == 0) {
                     Observable.just(it)
                } else {
                     Observable.just(it)
                         .subscribeOn(Schedulers.io())
                }
            }
            .doOnNext { println("$it ${Thread.currentThread().name}") }
            .subscribe()

        Thread.sleep(10)
        behaviorSubject.onNext(1) // switched to io

        Thread.sleep(10)
        behaviorSubject.onNext(2) // main

        Thread.sleep(10)
    }

    @Test
    fun testCompletedBehaviorSubject() {
        val behaviorSubject = BehaviorSubject.createDefault(0.0f)
        behaviorSubject.onNext(0.5f)
        behaviorSubject.onNext(1.0f)
        behaviorSubject.onComplete()
        behaviorSubject.subscribe( // subscribe after completed: only completed is printed
            { println(it) },
            { println(it) }
        ) { println("Completed") }
    }

    @Test
    fun testCompletedPublishSubject() {
        val subject = PublishSubject.create<Float>()
        subject.onNext(0.5f)
        subject.onNext(1.0f)
        subject.onComplete()
        subject.subscribe( // subscribe after completed: only completed is printed
            { println(it) },
            { println(it) }
        ) { println("Completed") }
    }
}
