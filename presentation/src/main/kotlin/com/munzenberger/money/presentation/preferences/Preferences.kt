package com.munzenberger.money.presentation.preferences

import kotlinx.coroutines.flow.Flow

/**
 * Installation-specific user preferences, such as the window's size and location.
 */
interface Preferences {
    /**
     * Returns the stored value for [key], or its default if none is stored, or it can't be read.
     */
    operator fun <T> get(key: PreferenceKey<T>): T

    operator fun <T> set(key: PreferenceKey<T>, value: T)

    /**
     * Removes the stored value for [key], so [get] returns its default again.
     */
    fun remove(key: PreferenceKey<*>)

    /**
     * Returns a flow that emits the current value for [key], then each change to it.
     */
    fun <T> observe(key: PreferenceKey<T>): Flow<T>
}
