package com.juandgaines.notemark.auth.di

import com.juandgaines.notemark.auth.data.AuthRepositoryImpl
import com.juandgaines.notemark.auth.domain.AuthRepository
import com.juandgaines.notemark.auth.domain.UserDataValidator
import com.juandgaines.notemark.auth.presentation.login.LoginViewModel
import com.juandgaines.notemark.auth.presentation.register.RegisterViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    singleOf(::AuthRepositoryImpl) bind AuthRepository::class
    singleOf(::UserDataValidator)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}
