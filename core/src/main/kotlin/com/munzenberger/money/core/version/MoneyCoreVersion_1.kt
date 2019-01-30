package com.munzenberger.money.core.version

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.model.BankRepository
import com.munzenberger.money.sql.Query
import com.munzenberger.money.version.ApplicableVersion

class MoneyCoreVersion_1 : ApplicableVersion<MoneyDatabase> {

    override val versionId = 1L

    override fun apply(obj: MoneyDatabase) {

        obj.execute(Query.createTable(BankRepository.table)
                .column(BankRepository.identityColumn, "BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
                .column(BankRepository.nameColumn, "TEXT NOT NULL")
                .build())
    }
}
