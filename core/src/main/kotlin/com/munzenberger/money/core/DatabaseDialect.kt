package com.munzenberger.money.core

interface DatabaseDialect {

    val identityType: String
    val identityColumnType: String

    fun identityType(constraints: String) = "$identityType $constraints"
}

object H2DatabaseDialect : DatabaseDialect {

    override val identityType = "INTEGER"
    override val identityColumnType = identityType("NOT NULL AUTO_INCREMENT PRIMARY KEY")
}

object SQLiteDatabaseDialect : DatabaseDialect {

    override val identityType = "INTEGER"
    override val identityColumnType = identityType("NOT NULL PRIMARY KEY AUTOINCREMENT")
}
