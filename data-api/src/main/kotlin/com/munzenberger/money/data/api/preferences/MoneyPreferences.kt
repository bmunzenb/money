package com.munzenberger.money.data.api.preferences

import kotlinx.coroutines.flow.Flow

interface MoneyPreferences {
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
