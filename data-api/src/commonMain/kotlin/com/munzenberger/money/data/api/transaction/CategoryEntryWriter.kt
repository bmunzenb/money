package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.EntityNotFoundException

interface CategoryEntryWriter {
    fun add(categoryEntry: CategoryEntry)

    /** Updates the stored category entry with [categoryEntry]'s id. Throws [EntityNotFoundException] if there is none. */
    fun update(categoryEntry: CategoryEntry)

    fun removeById(categoryEntryId: CategoryEntryId)
}

fun CategoryEntryWriter.remove(categoryEntry: CategoryEntry) {
    removeById(categoryEntry.id)
}
