package com.munzenberger.money.core

import com.munzenberger.money.sql.Query

interface DatabaseDialect {

    val identityType: String
    val identityColumnType: String
    val booleanType: String

    fun identityType(constraints: String) = "$identityType $constraints"
    fun booleanType(constraints: String) = "$booleanType $constraints"

    fun initialize(database: MoneyDatabase) {
        // if a dialect requires initialization after obtaining a connection
        // to the database, override this function
    }
}

object H2DatabaseDialect : DatabaseDialect {

    override val identityType = "INTEGER"
    // https://www.h2database.com/html/grammar.html#column_definition
    override val identityColumnType = identityType("NOT NULL IDENTITY")
    override val booleanType = "BOOLEAN"
}

object SQLiteDatabaseDialect : DatabaseDialect {

    override val identityType = "INTEGER"
    // https://sqlite.org/autoinc.html
    override val identityColumnType = identityType("NOT NULL PRIMARY KEY")
    // https://www.sqlite.org/datatype3.html#boolean_datatype
    override val booleanType = "INTEGER"

    override fun initialize(database: MoneyDatabase) {
        // SQLite requires explicitly enabling foreign key constraints
        // https://www.sqlite.org/foreignkeys.html#fk_enable
        database.execute(Query("PRAGMA foreign_keys = ON"))
    }
}
