package com.juandgaines.notemark.di

import com.juandgaines.notemark.core.data.auth.EncryptedSessionStorage
import com.juandgaines.notemark.core.data.networking.HttpClientFactory
import com.juandgaines.notemark.core.domain.SessionStorage
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    singleOf(::EncryptedSessionStorage) bind SessionStorage::class

    single {
        HttpClientFactory(get()).build()
    }
}
