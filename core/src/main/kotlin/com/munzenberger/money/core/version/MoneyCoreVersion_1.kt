package com.munzenberger.money.core.version

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.model.AccountTypeQueryBuilder
import com.munzenberger.money.core.model.BankModelQueryBuilder
import com.munzenberger.money.sql.Query
import com.munzenberger.money.version.ApplicableVersion

class MoneyCoreVersion_1 : ApplicableVersion<MoneyDatabase> {

    override val versionId = 1L

    override fun apply(obj: MoneyDatabase) {

        obj.execute(Query.createTable(BankModelQueryBuilder.table)
                .column(BankModelQueryBuilder.identityColumn, "BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
                .column(BankModelQueryBuilder.nameColumn, "TEXT NOT NULL")
                .build())

        obj.execute(Query.createTable(AccountTypeQueryBuilder.table)
                .column(AccountTypeQueryBuilder.identityColumn, "BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
                .column(AccountTypeQueryBuilder.nameColumn, "TEXT NOT NULL")
                .column(AccountTypeQueryBuilder.categoryColumn, "TEXT NOT NULL")
                .build())


    }
}
