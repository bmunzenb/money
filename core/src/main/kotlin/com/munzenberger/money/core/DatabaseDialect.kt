package com.munzenberger.money.core

interface DatabaseDialect {

    val identityType: String
    val identityColumnType: String
    val booleanType: String

    fun identityType(constraints: String) = "$identityType $constraints"
    fun booleanType(constraints: String) = "$booleanType $constraints"
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
    override val booleanType = "INTEGER"
}
