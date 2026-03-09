package com.juandgaines.notemark.di

import androidx.room.Room
import com.juandgaines.notemark.core.data.ConnectivityObserverImpl
import com.juandgaines.notemark.core.data.auth.EncryptedSessionStorage
import com.juandgaines.notemark.core.data.database.NotemarkDatabase
import com.juandgaines.notemark.core.data.networking.HttpClientFactory
import com.juandgaines.notemark.core.domain.ConnectivityObserver
import com.juandgaines.notemark.core.domain.SessionStorage
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    singleOf(::EncryptedSessionStorage) bind SessionStorage::class

    single {
        Json { ignoreUnknownKeys = true }
    }

    single {
        HttpClientFactory(get(), get()).build()
    }

    single {
        Room.databaseBuilder(androidContext(), NotemarkDatabase::class.java, "notemark.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<NotemarkDatabase>().noteDao }
    single { get<NotemarkDatabase>().pendingCreationDao }
    single { get<NotemarkDatabase>().syncQueueDao }

    singleOf(::ConnectivityObserverImpl) bind ConnectivityObserver::class
}
