package com.juandgaines.notemark.di

import androidx.room.Room
import com.juandgaines.notemark.core.data.auth.EncryptedSessionStorage
import com.juandgaines.notemark.core.data.database.NotemarkDatabase
import com.juandgaines.notemark.core.data.networking.HttpClientFactory
import com.juandgaines.notemark.core.domain.SessionStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val coreDataModule = module {
    singleOf(::EncryptedSessionStorage) bind SessionStorage::class

    single {
        HttpClientFactory(get()).build()
    }

    single {
        Room.databaseBuilder(androidContext(), NotemarkDatabase::class.java, "notemark.db").build()
    }
    single { get<NotemarkDatabase>().noteDao }
}
