package com.juandgaines.notemark.core.presentation.util

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(val value: String): UiText
    class StringResource(
        @StringRes val id: Int,
        val args: Array<Any> = arrayOf()
    ): UiText
    class PluralResource(
        @PluralsRes val id: Int,
        val quantity: Int,
        val args: Array<Any> = arrayOf()
    ): UiText

    @Composable
    fun asString(): String {
        return when(this) {
            is DynamicString -> value
            is StringResource -> stringResource(id = id, *args)
            is PluralResource -> {
                val context = LocalContext.current
                context.resources.getQuantityString(id, quantity, *args)
            }
        }
    }

    fun asString(context: Context): String {
        return when(this) {
            is DynamicString -> value
            is StringResource -> context.getString(id, *args)
            is PluralResource -> context.resources.getQuantityString(id, quantity, *args)
        }
    }
}
