package com.munzenberger.money.presentation.preferences

import com.munzenberger.money.presentation.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
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

    // Bumped on every change, so each observer re-reads its key and emits only if its value changed.
    private val revision = MutableStateFlow(0L)

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

    operator fun <T> set(key: PreferenceKey<T>, value: T) {
        node.put(key.name, json.encodeToString(key.serializer, value))
        revision.update { it + 1 }
    }

    /**
     * Removes the stored value for [key], so [get] returns its default again.
     */
    fun remove(key: PreferenceKey<*>) {
        node.remove(key.name)
        revision.update { it + 1 }
    }

    /**
     * Returns a flow that emits the current value for [key], then each change to it.
     */
    fun <T> observe(key: PreferenceKey<T>): Flow<T> =
        revision.map { get(key) }.distinctUntilChanged()

    private companion object {
        const val NODE_PATH = "com/munzenberger/money"
    }
}
