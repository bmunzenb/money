package com.munzenberger.money.data.sql

import app.cash.sqldelight.db.QueryResult
import com.munzenberger.money.data.api.EntityNotFoundException

/**
 * Throws [EntityNotFoundException] with [lazyMessage] unless the statement that returned [this] row count
 * changed exactly one row.
 */
internal fun QueryResult<Long>.requireOneRow(lazyMessage: () -> String) {
    if (value != 1L) throw EntityNotFoundException(lazyMessage())
}
