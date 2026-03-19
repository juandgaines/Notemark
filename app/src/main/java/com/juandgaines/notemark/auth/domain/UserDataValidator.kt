package com.juandgaines.notemark.auth.domain

import android.util.Patterns

class UserDataValidator {

    fun validateUsername(username: String): Boolean {
        return username.length in 3..20
    }

    fun validateEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 8 && password.any { !it.isLetterOrDigit() || it.isDigit() }
    }
}
