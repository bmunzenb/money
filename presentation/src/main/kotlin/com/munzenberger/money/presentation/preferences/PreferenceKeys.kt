package com.munzenberger.money.presentation.preferences

import com.munzenberger.money.data.api.preferences.PreferenceKey
import kotlinx.serialization.builtins.nullable

object PreferenceKeys {
    val windowBounds = PreferenceKey("window.bounds", null, WindowBounds.serializer().nullable)
}
