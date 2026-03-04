package com.juandgaines.notemark.auth.domain

class UserDataValidator {

    fun validateUsername(username: String): Boolean {
        return username.length in 3..20
    }

    fun validateEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }

    fun validatePassword(password: String): Boolean {
        return password.length >= 8 &&
            password.any { it.isDigit() } &&
            password.any { !it.isLetterOrDigit() }
    }

    fun validatePasswordsMatch(password: String, repeatPassword: String): Boolean {
        return password == repeatPassword && password.isNotEmpty()
    }

    companion object {
        private val EMAIL_REGEX = Regex(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        )
    }
}
