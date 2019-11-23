package com.munzenberger.money.core

import org.junit.Assert.*
import org.junit.Test

abstract class PersistableTest<P : Persistable<*>> : MoneyDatabaseTestSupport() {

    abstract fun createPersistable(): P

    abstract fun getPersistable(identity: Long): P?

    abstract fun getAllPersistables(): List<P>

    abstract fun updatePersistable(persistable : P)

    abstract fun assertPersistablePropertiesAreEquals(p1: P, p2: P)

    @Test
    fun `retrieve invalid identity returns null`() {

        assertNull(getPersistable(42L))
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
}
