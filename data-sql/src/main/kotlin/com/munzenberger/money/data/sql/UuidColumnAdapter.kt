package com.munzenberger.money.data.sql

import app.cash.sqldelight.ColumnAdapter
import kotlin.uuid.Uuid

/** Stores a [Uuid] in a `BLOB AS Uuid` column as its 16 bytes. */
internal object UuidColumnAdapter : ColumnAdapter<Uuid, ByteArray> {
    override fun decode(databaseValue: ByteArray): Uuid = Uuid.fromByteArray(databaseValue)

    override fun encode(value: Uuid): ByteArray = value.toByteArray()
}
