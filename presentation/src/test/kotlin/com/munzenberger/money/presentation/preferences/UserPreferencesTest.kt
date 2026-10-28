package com.munzenberger.money.presentation.preferences

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.serializer
import java.util.prefs.AbstractPreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserPreferencesTest {

    private val node = InMemoryPreferences()
    private val preferences = UserPreferences(node)

    private val enabled = PreferenceKey("test.enabled", true, Boolean.serializer())
    private val count = PreferenceKey("test.count", 0, Int.serializer())

    @Test
    fun `get returns the default when no value is stored`() {
        assertEquals(true, preferences[enabled])
        assertEquals(0, preferences[count])
    }

    @Test
    fun `get returns the value that was set`() {
        preferences[count] = 5

        assertEquals(5, preferences[count])
    }

    @Test
    fun `set stores the value as JSON under the key's name`() {
        preferences[enabled] = false

        assertEquals("false", node.get("test.enabled", null))
    }

    @Test
    fun `remove restores the default`() {
        preferences[enabled] = false

        preferences.remove(enabled)

        assertEquals(true, preferences[enabled])
        assertNull(node.get("test.enabled", null))
    }

    @Test
    fun `get returns the default when the stored value can't be read`() {
        node.put("test.enabled", "not a boolean")

        assertEquals(true, preferences[enabled])
    }

    @Test
    fun `observe emits the current value, then each change`() = runTest {
        preferences.observe(count).test {
            assertEquals(0, awaitItem())

            preferences[count] = 1
            assertEquals(1, awaitItem())

            preferences.remove(count)
            assertEquals(0, awaitItem())
        }
    }

    @Test
    fun `observe doesn't emit when another key changes`() = runTest {
        preferences.observe(count).test {
            assertEquals(0, awaitItem())

            preferences[enabled] = false
            preferences[count] = 1

            assertEquals(1, awaitItem())
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
