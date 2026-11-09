package com.munzenberger.money.presentation.preferences

import kotlinx.serialization.builtins.serializer
import java.util.prefs.Preferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class UserPreferencesTest {

    private val node = InMemoryPreferences()
    private val preferences = UserPreferences(node)

    private val enabled = PreferenceKey("test.enabled", true, Boolean.serializer())
    private val count = PreferenceKey("test.count", 0, Int.serializer())
    private val text = PreferenceKey("test.text", "", String.serializer())

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
    fun `set keeps the previous value when the new one is too long to store`() {
        preferences[text] = "a"

        preferences[text] = "x".repeat(Preferences.MAX_VALUE_LENGTH)

        assertEquals("a", preferences[text])
    }

    @Test
    fun `a key's name can't be longer than the maximum key length`() {
        PreferenceKey("x".repeat(Preferences.MAX_KEY_LENGTH), 0, Int.serializer())

        assertFailsWith<IllegalArgumentException> {
            PreferenceKey("x".repeat(Preferences.MAX_KEY_LENGTH + 1), 0, Int.serializer())
        }
    }

    @Test
    fun `node stores values in the child node with its name`() {
        preferences.node("child")[count] = 5

        assertEquals("5", node.node("child").get("test.count", null))
        assertNull(node.get("test.count", null))
        assertEquals(0, preferences[count])
    }

    @Test
    fun `node returns the values stored in the same child node`() {
        preferences.node("child")[count] = 5

        assertEquals(5, preferences.node("child")[count])
        assertEquals(0, preferences.node("other")[count])
    }

    @Test
    fun `a node's name can't be longer than the maximum name length`() {
        preferences.node("x".repeat(Preferences.MAX_NAME_LENGTH))

        assertFailsWith<IllegalArgumentException> {
            preferences.node("x".repeat(Preferences.MAX_NAME_LENGTH + 1))
        }
    }

    @Test
    fun `a node's name can't contain a slash`() {
        assertFailsWith<IllegalArgumentException> {
            preferences.node("a/b")
        }
    }
}
