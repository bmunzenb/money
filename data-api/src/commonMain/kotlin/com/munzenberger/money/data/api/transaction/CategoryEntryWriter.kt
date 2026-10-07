package com.munzenberger.money.data.api.transaction

interface CategoryEntryWriter {
    fun add(categoryEntry: CategoryEntry)

    fun update(categoryEntry: CategoryEntry)

    fun removeById(categoryEntryId: CategoryEntryId)
}

fun CategoryEntryWriter.remove(categoryEntry: CategoryEntry) {
    removeById(categoryEntry.id)
}
