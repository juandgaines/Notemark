package com.juandgaines.notemark.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

fun initKoin(app: Application) {
    startKoin {
        androidContext(app)
        modules(
            appModule,
            coreDataModule,
            authModule,
            noteModule,
        )
    }
}
