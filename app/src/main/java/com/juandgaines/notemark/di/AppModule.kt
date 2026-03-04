package com.juandgaines.notemark.di

import com.juandgaines.notemark.MainViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::MainViewModel)
}
