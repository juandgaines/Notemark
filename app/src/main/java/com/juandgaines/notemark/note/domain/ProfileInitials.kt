package com.juandgaines.notemark.note.domain

fun getProfileInitials(username: String): String {
    val words = username.trim().split("\\s+".toRegex())
    return when {
        words.isEmpty() || words[0].isEmpty() -> ""
        words.size == 1 -> words[0].take(2).uppercase()
        words.size == 2 -> "${words[0].first()}${words[1].first()}".uppercase()
        else -> "${words.first().first()}${words.last().first()}".uppercase()
    }
}
