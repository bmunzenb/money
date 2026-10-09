package com.munzenberger.money.data.api.transaction

import com.munzenberger.money.data.api.EntityNotFoundException

interface TransferEntryWriter {
    fun add(transferEntry: TransferEntry)

    /** Updates the stored transfer entry with [transferEntry]'s id. Throws [EntityNotFoundException] if there is none. */
    fun update(transferEntry: TransferEntry)

    fun removeById(transferEntryId: TransferEntryId)
}

fun TransferEntryWriter.remove(transferEntry: TransferEntry) {
    removeById(transferEntry.id)
}
