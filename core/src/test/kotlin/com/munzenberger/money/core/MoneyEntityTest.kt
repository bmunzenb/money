package com.munzenberger.money.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

abstract class MoneyEntityTest<I : Identity, P : MoneyEntity<I>> : MoneyDatabaseTestSupport() {
    abstract fun createEntity(): P

    abstract fun getEntity(identity: I): P?

    abstract fun findEntities(): List<P>

    abstract fun updateEntity(entity: P)

    abstract fun assertEntityPropertiesAreEquals(
        p1: P,
        p2: P,
    )

    abstract fun createInvalidIdentity(): I

    @Test
    fun `retrieve invalid identity returns null`() {
        assertNull(getEntity(createInvalidIdentity()))
    }

    @Test
    fun `can delete an unsaved entity`() {
        createEntity().apply {
            delete(database)
        }
    }

    @Test
    fun `can store and retrieve an entity by identity`() {
        val p = createEntity()
        assertNull(p.identity)

        p.save(database)
        assertNotNull(p.identity)

        getEntity(p.identity!!).apply {
            assertNotNull(this)
            assertEquals(this!!.identity, p.identity)
            assertEntityPropertiesAreEquals(p, this)
        }
    }

    @Test
    open fun `can store and retrieve a list of entities`() {
        val list = listOf(createEntity(), createEntity(), createEntity())

        list.forEach {
            it.save(database)
        }

        findEntities().apply {
            assertEquals(this.size, list.size)
            this.zip(list).forEach {
                assertEquals(it.first.identity, it.second.identity)
                assertEntityPropertiesAreEquals(it.first, it.second)
            }
        }
    }

    @Test
    fun `can store and delete an entity`() {
        val p = createEntity().apply { save(database) }
        val identity = p.identity!!

        p.delete(database)
        assertNull(p.identity)

        assertNull(getEntity(identity))
    }

    @Test
    fun `can store and update an entity`() {
        val p = createEntity().apply { save(database) }

        updateEntity(p)

        p.save(database)

        getEntity(p.identity!!).apply {
            assertNotNull(this)
            assertEquals(this!!.identity, p.identity)
            assertEntityPropertiesAreEquals(p, this)
        }
    }

    @Test
    fun `entity equals and hashCode`() {
        val p1 = createEntity()
        p1.save(database)

        val p2 = getEntity(p1.identity!!)
        assertTrue(p1 == p2)
        assertEquals(p1.hashCode(), p2.hashCode())

        val p3 = createEntity()
        p3.save(database)

        assertFalse(p2 == p3)
        assertNotEquals(p2, p3)

        updateEntity(p1)
        assertFalse(p1 == p2)
        assertNotEquals(p1, p2)
    }

    @Test
    fun `rolling back a transaction on insert clears the identity`() {
        val tx = database.createTransaction()

        val p = createEntity().apply { save(tx) }
        assertNotNull(p.identity)

        tx.rollback()
        assertNull(p.identity)
    }

    @Test
    fun `rolling back a transaction on delete restores the identity`() {
        val p = createEntity().apply { save(database) }

        val tx = database.createTransaction()

        val identity = p.identity
        assertNotNull(identity)

        p.delete(tx)
        assertNull(p.identity)

        tx.rollback()
        assertEquals(identity, p.identity)
    }
}
