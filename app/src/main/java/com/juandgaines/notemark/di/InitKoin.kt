package com.juandgaines.notemark.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

fun initKoin(app: Application) {
    startKoin {
        androidContext(app)
        workManagerFactory()
        modules(
            appModule,
            coreDataModule,
            authModule,
            noteModule,
        )
    }
}
