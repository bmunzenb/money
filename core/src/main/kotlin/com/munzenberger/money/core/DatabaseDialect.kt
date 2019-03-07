package com.munzenberger.money.core

interface DatabaseDialect {

    val identityColumnType: String
    val identityReferenceType: String
}

object H2DatabaseDialect : DatabaseDialect {

    override val identityReferenceType = "INTEGER"
    override val identityColumnType = "$identityReferenceType NOT NULL AUTO_INCREMENT PRIMARY KEY"
}

object SQLiteDatabaseDialect : DatabaseDialect {

    override val identityReferenceType = "INTEGER"
    override val identityColumnType = "$identityReferenceType NOT NULL PRIMARY KEY AUTOINCREMENT"
}
