package io.github.zhutq.rxplayground

import android.app.Application
import timber.log.Timber
import timber.log.Timber.DebugTree

class PlaygroundApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}