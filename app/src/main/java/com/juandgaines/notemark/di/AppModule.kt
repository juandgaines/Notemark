package com.juandgaines.notemark.di

import com.juandgaines.notemark.App
import com.juandgaines.notemark.MainViewModel
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<CoroutineScope> {
        (androidApplication() as App).applicationScope
    }
    viewModelOf(::MainViewModel)
}
