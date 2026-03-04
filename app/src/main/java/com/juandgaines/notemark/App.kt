package com.juandgaines.notemark

import android.app.Application
import com.juandgaines.notemark.di.initKoin
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initKoin(this)
    }
}
