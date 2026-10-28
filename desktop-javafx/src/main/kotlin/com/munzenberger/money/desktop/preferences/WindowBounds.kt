package com.munzenberger.money.desktop.preferences

import com.munzenberger.money.presentation.preferences.PreferenceKey
import com.munzenberger.money.presentation.preferences.UserPreferences
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.nullable

@Serializable
data class WindowBounds(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)

private val windowBoundsKey = PreferenceKey("window.bounds", null, WindowBounds.serializer().nullable)

var UserPreferences.windowBounds: WindowBounds?
    get() = get(windowBoundsKey)
    set(value) = set(windowBoundsKey, value)
