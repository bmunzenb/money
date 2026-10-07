package com.munzenberger.money.data.api.transaction

interface TransferEntryWriter {
    fun add(transferEntry: TransferEntry)

    fun update(transferEntry: TransferEntry)

    fun removeById(transferEntryId: TransferEntryId)
}

fun TransferEntryWriter.remove(transferEntry: TransferEntry) {
    removeById(transferEntry.id)
}
