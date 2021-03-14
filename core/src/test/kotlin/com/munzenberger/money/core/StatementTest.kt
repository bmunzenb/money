package com.munzenberger.money.core

import org.junit.Assert.assertEquals

class StatementTest : PersistableTest<Statement>() {

    override fun createPersistable() = Statement().randomize()

    override fun getPersistable(identity: Long) = Statement.get(identity, database)

    override fun getAllPersistables() = Statement.getAll(database)

    override fun updatePersistable(persistable: Statement) {
        persistable.randomize()
    }

    override fun assertPersistablePropertiesAreEquals(p1: Statement, p2: Statement) {
        assertEquals(p1.closingDate, p2.closingDate)
        assertEquals(p1.endingBalance, p2.endingBalance)
        assertEquals(p1.isReconciled, p2.isReconciled)
    }
}
