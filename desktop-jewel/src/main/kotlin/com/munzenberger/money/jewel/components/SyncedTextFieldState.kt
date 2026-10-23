package com.munzenberger.money.jewel.components

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow

/**
 * A [TextFieldState] for Jewel's text fields, which hold their own text, kept in step with a [value] held
 * in UI state. What the user types is passed to [onValueChange], and a [value] that changes from
 * elsewhere, like a saved account being loaded or an amount being reformatted, replaces the text.
 */
@Composable
fun rememberSyncedTextFieldState(value: String, onValueChange: (String) -> Unit): TextFieldState {
    val state = rememberTextFieldState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    // The text last passed to onValueChange, or last taken from value, so neither is echoed back as a change.
    var lastValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (value != lastValue) {
            lastValue = value
            state.setTextAndPlaceCursorAtEnd(value)
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }.collect { text ->
            if (text != lastValue) {
                lastValue = text
                currentOnValueChange(text)
            }
        }
    }

    return state
}
