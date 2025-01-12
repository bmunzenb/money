package com.munzenberger.money.sql

import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteQueryBuilderTest {
    @Test
    fun `delete with condition`() {
        val query =
            deleteQuery("TABLE") {
                where("FOO".eq(42))
            }

        assertEquals("DELETE FROM TABLE WHERE FOO = ?", query.sql)
        assertEquals(listOf(42), query.parameters)
    }
}
