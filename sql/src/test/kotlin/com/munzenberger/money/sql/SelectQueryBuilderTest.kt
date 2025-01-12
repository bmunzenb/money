package com.munzenberger.money.sql

import org.junit.Assert.assertEquals
import org.junit.Test

class SelectQueryBuilderTest {
    @Test
    fun `select table`() {
        val query = selectQuery("TABLE") {}

        assertEquals("SELECT * FROM TABLE", query.sql)
    }

    @Test
    fun `select table and columns`() {
        val query =
            selectQuery("TABLE") {
                cols("COL_A", "COL_B", "COL_C")
            }

        assertEquals("SELECT COL_A, COL_B, COL_C FROM TABLE", query.sql)
    }

    @Test
    fun `select with where clause`() {
        val query =
            selectQuery("TABLE") {
                cols("COL")
                where("COL".eq(1))
            }

        assertEquals("SELECT COL FROM TABLE WHERE COL = ?", query.sql)
        assertEquals(listOf(1), query.parameters)
    }

    @Test
    fun `select with join`() {
        val query =
            selectQuery("TABLE_A") {
                cols("COL_A")
                innerJoin("TABLE_A", "COL_A", "TABLE_B", "COL_B")
            }

        assertEquals("SELECT COL_A FROM TABLE_A INNER JOIN TABLE_B ON TABLE_A.COL_A = TABLE_B.COL_B", query.sql)
    }

    @Test
    fun `select with order`() {
        val query =
            selectQuery("TABLE") {
                cols("COL_A", "COL_B")
                orderBy("COL_A", "COL_B")
            }

        assertEquals("SELECT COL_A, COL_B FROM TABLE ORDER BY COL_A ASC, COL_B ASC", query.sql)
    }
}
