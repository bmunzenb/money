package com.munzenberger.money.presentation.preferences

import app.cash.turbine.test
import com.munzenberger.money.data.api.preferences.PreferenceKey
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.serializer
import java.util.prefs.AbstractPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserPreferencesTest {

    private val node = InMemoryPreferences()
    private val preferences = UserPreferences(node)

    @Test
    fun `get returns the default when no value is stored`() {
        assertEquals(true, preferences[PreferenceKeys.navigationRailExpanded])
        assertNull(preferences[PreferenceKeys.windowBounds])
    }

    @Test
    fun `get returns the value that was set`() {
        val bounds = WindowBounds(x = 10.0, y = 20.0, width = 800.0, height = 600.0)

        preferences[PreferenceKeys.windowBounds] = bounds

        assertEquals(bounds, preferences[PreferenceKeys.windowBounds])
    }

    @Test
    fun `set stores the value as JSON under the key's name`() {
        preferences[PreferenceKeys.windowMaximized] = true

        assertEquals("true", node.get("window.maximized", null))
    }

    @Test
    fun `remove restores the default`() {
        preferences[PreferenceKeys.navigationRailExpanded] = false

        preferences.remove(PreferenceKeys.navigationRailExpanded)

        assertEquals(true, preferences[PreferenceKeys.navigationRailExpanded])
        assertNull(node.get("rail.expanded", null))
    }

    @Test
    fun `get returns the default when the stored value can't be read`() {
        node.put("rail.expanded", "not a boolean")

        assertEquals(true, preferences[PreferenceKeys.navigationRailExpanded])
    }

    @Test
    fun `observe emits the current value, then each change`() = runTest {
        val key = PreferenceKey("test.count", 0, Int.serializer())

        preferences.observe(key).test {
            assertEquals(0, awaitItem())

            preferences[key] = 1
            assertEquals(1, awaitItem())

            preferences.remove(key)
            assertEquals(0, awaitItem())
        }
    }

    @Test
    fun `observe doesn't emit when another key changes`() = runTest {
        preferences.observe(PreferenceKeys.windowMaximized).test {
            assertEquals(false, awaitItem())

            preferences[PreferenceKeys.navigationRailExpanded] = false
            preferences[PreferenceKeys.windowMaximized] = true

            assertEquals(true, awaitItem())
        }
    }

    /** A [java.util.prefs.Preferences] node kept in memory, so tests don't touch the user's preferences. */
    private class InMemoryPreferences(
        parent: AbstractPreferences? = null,
        name: String = "",
    ) : AbstractPreferences(parent, name) {
        private val values = mutableMapOf<String, String>()
        private val children = mutableMapOf<String, InMemoryPreferences>()

        override fun putSpi(key: String, value: String) {
            values[key] = value
        }

        override fun getSpi(key: String): String? = values[key]

        override fun removeSpi(key: String) {
            values.remove(key)
        }

        override fun removeNodeSpi() = Unit

        override fun keysSpi(): Array<String> = values.keys.toTypedArray()

        override fun childrenNamesSpi(): Array<String> = children.keys.toTypedArray()

        override fun childSpi(name: String): AbstractPreferences =
            children.getOrPut(name) { InMemoryPreferences(this, name) }

        override fun syncSpi() = Unit

        override fun flushSpi() = Unit
    }
}
