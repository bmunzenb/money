package com.munzenberger.money.presentation.preferences

import kotlinx.serialization.Serializable

@Serializable
data class WindowBounds(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
)
