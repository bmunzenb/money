package com.munzenberger.money.presentation.preferences

import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

object PreferenceKeys {
    val windowBounds = PreferenceKey("window.bounds", null, WindowBounds.serializer().nullable)
    val windowMaximized = PreferenceKey("window.maximized", false, Boolean.serializer())
    val navigationRailExpanded = PreferenceKey("rail.expanded", true, Boolean.serializer())
}
