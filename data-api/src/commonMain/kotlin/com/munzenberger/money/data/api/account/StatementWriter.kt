package com.munzenberger.money.data.api.account

import com.munzenberger.money.data.api.EntityNotFoundException

interface StatementWriter {
    fun add(statement: Statement)

    /** Updates the stored statement with [statement]'s id. Throws [EntityNotFoundException] if there is none. */
    fun update(statement: Statement)

    fun removeById(statementId: StatementId)
}

fun StatementWriter.remove(statement: Statement) {
    removeById(statement.id)
}
