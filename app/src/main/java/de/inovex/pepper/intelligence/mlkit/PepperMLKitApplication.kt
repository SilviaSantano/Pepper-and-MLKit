package de.inovex.pepper.intelligence.mlkit

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@HiltAndroidApp
class PepperMLKitApplication : Application() {
    val executorService: ExecutorService = Executors.newFixedThreadPool(4)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            Timber.plant(object : Timber.Tree() {
                protected override fun log(
                    priority: Int,
                    tag: String?,
                    message: String,
                    t: Throwable?
                ) {
                    Log.println(priority, tag, message)
                }
            })
        }
    }
}
