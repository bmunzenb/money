package com.munzenberger.money.presentation.preferences

import kotlinx.serialization.KSerializer
import java.util.prefs.Preferences

/**
 * Identifies a preference by its [name], with the [default] returned when no value is stored and
 * the [serializer] used to store its values. The [name] can be at most [Preferences.MAX_KEY_LENGTH] characters.
 */
class PreferenceKey<T>(
    val name: String,
    val default: T,
    val serializer: KSerializer<T>,
) {
    init {
        require(name.length <= Preferences.MAX_KEY_LENGTH) {
            "Preference name is longer than ${Preferences.MAX_KEY_LENGTH} characters: $name"
        }
    }
}
