package com.juandgaines.notemark.di

import com.juandgaines.notemark.note.data.NoteRepositoryImpl
import com.juandgaines.notemark.note.data.remote.KtorRemoteNoteDataSource
import com.juandgaines.notemark.note.data.remote.RemoteNoteDataSource
import com.juandgaines.notemark.note.data.sync.SyncWorker
import com.juandgaines.notemark.note.data.sync.WorkManagerSyncScheduler
import com.juandgaines.notemark.note.domain.NoteRepository
import com.juandgaines.notemark.note.domain.SyncScheduler
import com.juandgaines.notemark.note.presentation.note_detail.NoteDetailViewModel
import com.juandgaines.notemark.note.presentation.note_list.NoteListViewModel
import com.juandgaines.notemark.settings.data.DataStoreSyncPreferences
import com.juandgaines.notemark.settings.domain.SyncPreferences
import com.juandgaines.notemark.settings.presentation.settings_screen.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val noteModule = module {
    singleOf(::KtorRemoteNoteDataSource) bind RemoteNoteDataSource::class
    singleOf(::NoteRepositoryImpl) bind NoteRepository::class

    workerOf(::SyncWorker)
    single<SyncScheduler> { WorkManagerSyncScheduler(androidContext()) }
    singleOf(::DataStoreSyncPreferences) bind SyncPreferences::class

    viewModelOf(::NoteListViewModel)
    viewModelOf(::NoteDetailViewModel)
    viewModelOf(::SettingsViewModel)
}
