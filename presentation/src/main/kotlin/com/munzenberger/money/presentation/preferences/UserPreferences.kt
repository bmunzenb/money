package com.munzenberger.money.presentation.preferences

import com.munzenberger.money.presentation.logger
import kotlinx.serialization.json.Json
import java.util.logging.Level
import java.util.prefs.Preferences

/**
 * The user's preferences, stored in a [java.util.prefs.Preferences] [node], with each value encoded
 * as JSON by its key's serializer.
 */
class UserPreferences(
    private val node: Preferences = Preferences.userRoot().node(NODE_PATH),
    private val json: Json = Json,
) {

    /**
     * Returns the stored value for [key], or its default if none is stored, or it can't be read.
     */
    operator fun <T> get(key: PreferenceKey<T>): T {
        val stored = node.get(key.name, null) ?: return key.default
        return try {
            json.decodeFromString(key.serializer, stored)
        } catch (e: IllegalArgumentException) {
            logger.log(Level.WARNING, "Unable to read preference ${key.name}", e)
            key.default
        }
    }

    /**
     * Stores [value] for [key]. If it can't be stored, such as when it's too long, the previous value is kept and the
     * failure logged.
     */
    operator fun <T> set(key: PreferenceKey<T>, value: T) {
        try {
            node.put(key.name, json.encodeToString(key.serializer, value))
        } catch (e: IllegalArgumentException) {
            logger.log(Level.WARNING, "Unable to store preference ${key.name}", e)
        }
    }

    /**
     * Removes the stored value for [key], so [get] returns its default again.
     */
    fun remove(key: PreferenceKey<*>) {
        node.remove(key.name)
    }

    private companion object {
        const val NODE_PATH = "com/munzenberger/money"
    }
}
