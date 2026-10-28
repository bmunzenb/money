package com.munzenberger.money.presentation.preferences

import java.util.prefs.AbstractPreferences

/** A [java.util.prefs.Preferences] node kept in memory, so tests don't touch the user's preferences. */
class InMemoryPreferences(
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
