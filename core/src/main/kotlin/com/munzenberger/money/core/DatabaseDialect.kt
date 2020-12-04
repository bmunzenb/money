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
    override val identityColumnType = identityType("NOT NULL AUTO_INCREMENT PRIMARY KEY")
    override val booleanType = "BOOLEAN"
}

object SQLiteDatabaseDialect : DatabaseDialect {

    override val identityType = "INTEGER"
    override val identityColumnType = identityType("NOT NULL PRIMARY KEY AUTOINCREMENT")
    override val booleanType = "INTEGER"
}
