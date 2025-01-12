package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

abstract class MoneyEntityTest<I : Identity, P : MoneyEntity<I>> : MoneyDatabaseTestSupport() {
    abstract fun createPersistable(): P

    abstract fun getPersistable(identity: I): P?

    abstract fun getAllPersistables(): List<P>

    abstract fun updatePersistable(persistable: P)

    abstract fun assertPersistablePropertiesAreEquals(
        p1: P,
        p2: P,
    )

    abstract fun createInvalidIdentity(): I

    @Test
    fun `retrieve invalid identity returns null`() {
        assertNull(getPersistable(createInvalidIdentity()))
    }

    @Test
    fun `can delete an unsaved persistable`() {
        createPersistable().apply {
            delete(database)
        }
    }

    @Test
    fun `can store and retrieve a persistable by identity`() {
        val p = createPersistable()
        assertNull(p.identity)

        p.save(database)
        assertNotNull(p.identity)

        getPersistable(p.identity!!).apply {
            assertNotNull(this)
            assertEquals(this!!.identity, p.identity)
            assertPersistablePropertiesAreEquals(p, this)
        }
    }

    @Test
    open fun `can store and retrieve a list of persistables`() {
        val list = listOf(createPersistable(), createPersistable(), createPersistable())

        list.forEach {
            it.save(database)
        }

        getAllPersistables().apply {
            assertEquals(this.size, list.size)
            this.zip(list).forEach {
                assertEquals(it.first.identity, it.second.identity)
                assertPersistablePropertiesAreEquals(it.first, it.second)
            }
        }
    }

    @Test
    fun `can store and delete a persistable`() {
        val p = createPersistable().apply { save(database) }
        val identity = p.identity!!

        p.delete(database)
        assertNull(p.identity)

        assertNull(getPersistable(identity))
    }

    @Test
    fun `can store and update a persistable`() {
        val p = createPersistable().apply { save(database) }

        updatePersistable(p)

        p.save(database)

        getPersistable(p.identity!!).apply {
            assertNotNull(this)
            assertEquals(this!!.identity, p.identity)
            assertPersistablePropertiesAreEquals(p, this)
        }
    }

    @Test
    fun `persistable equals and hashCode`() {
        val p1 = createPersistable()
        p1.save(database)

        val p2 = getPersistable(p1.identity!!)
        assertTrue(p1 == p2)
        assertEquals(p1.hashCode(), p2.hashCode())

        val p3 = createPersistable()
        p3.save(database)

        assertFalse(p2 == p3)
        assertNotEquals(p2, p3)

        updatePersistable(p1)
        assertFalse(p1 == p2)
        assertNotEquals(p1, p2)
    }

    @Test
    fun `rolling back a transaction on insert clears the identity`() {
        val tx = database.createTransaction()

        val p = createPersistable().apply { save(tx) }
        assertNotNull(p.identity)

        tx.rollback()
        assertNull(p.identity)
    }

    @Test
    fun `rolling back a transaction on delete restores the identity`() {
        val p = createPersistable().apply { save(database) }

        val tx = database.createTransaction()

        val identity = p.identity
        assertNotNull(identity)

        p.delete(tx)
        assertNull(p.identity)

        tx.rollback()
        assertEquals(identity, p.identity)
    }
}
