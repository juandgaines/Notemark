package com.juandgaines.notemark.settings.domain

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

enum class SyncInterval(val displayName: String, val duration: Duration?) {
    MANUAL_ONLY("Manual only", null),
    FIFTEEN_MINUTES("15 minutes", 15.minutes),
    THIRTY_MINUTES("30 minutes", 30.minutes),
    ONE_HOUR("1 hour", 60.minutes),
}
