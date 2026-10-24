package com.munzenberger.money.data.api.preferences

import kotlinx.serialization.KSerializer

/**
 * Identifies a preference by its [name], with the [default] returned when no value is stored and
 * the [serializer] used to store its values.
 */
class PreferenceKey<T>(
    val name: String,
    val default: T,
    val serializer: KSerializer<T>,
)
