package com.munzenberger.money.data.api.account

interface StatementWriter {
    fun add(statement: Statement)

    fun update(statement: Statement)

    fun removeById(statementId: StatementId)
}

fun StatementWriter.remove(statement: Statement) {
    removeById(statement.id)
}
