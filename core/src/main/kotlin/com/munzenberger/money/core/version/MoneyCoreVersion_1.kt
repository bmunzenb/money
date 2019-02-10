package com.munzenberger.money.core.version

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.model.*
import com.munzenberger.money.sql.Query
import com.munzenberger.money.version.ApplicableVersion

class MoneyCoreVersion_1 : ApplicableVersion<MoneyDatabase> {

    override val versionId = 1L

    override fun apply(obj: MoneyDatabase) {

        obj.execute(Query.createTable(BankModelQueryBuilder.table)
                .column(BankModelQueryBuilder.identityColumn, "BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
                .column(BankModelQueryBuilder.nameColumn, "TEXT NOT NULL")
                .build())

        obj.execute(Query.createTable(AccountTypeModelQueryBuilder.table)
                .column(AccountTypeModelQueryBuilder.identityColumn, "BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
                .column(AccountTypeModelQueryBuilder.nameColumn, "TEXT NOT NULL")
                .column(AccountTypeModelQueryBuilder.categoryColumn, "TEXT NOT NULL")
                .build())

        obj.execute(Query.createTable(PayeeModelQueryBuilder.table)
                .column(PayeeModelQueryBuilder.identityColumn, "BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
                .column(PayeeModelQueryBuilder.nameColumn, "TEXT NOT NULL")
                .build())

        obj.execute(Query.createTable(AccountModelQueryBuilder.table)
                .column(AccountModelQueryBuilder.identityColumn, "BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
                .column(AccountModelQueryBuilder.nameColumn, "TEXT NOT NULL")
                .column(AccountModelQueryBuilder.numberColumn, "TEXT")
                .columnWithReference(AccountModelQueryBuilder.accountTypeColumn, "BIGINT NOT NULL", AccountTypeModelQueryBuilder.table, AccountTypeModelQueryBuilder.identityColumn)
                .columnWithReference(AccountModelQueryBuilder.bankColumn, "BIGINT", BankModelQueryBuilder.table, BankModelQueryBuilder.identityColumn)
                .build())

        obj.execute(Query.createTable(CategoryModelQueryBuilder.table)
                .column(CategoryModelQueryBuilder.identityColumn, "BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
                .columnWithReference(CategoryModelQueryBuilder.accountColumn, "BIGINT NOT NULL", AccountModelQueryBuilder.table, AccountModelQueryBuilder.identityColumn)
                .column(CategoryModelQueryBuilder.nameColumn, "TEXT")
                .build())

        obj.execute(Query.createTable(TransactionModelQueryBuilder.table)
                .column(TransactionModelQueryBuilder.identityColumn, "BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY")
                .columnWithReference(TransactionModelQueryBuilder.accountColumn, "BIGINT NOT NULL", AccountModelQueryBuilder.table, AccountModelQueryBuilder.identityColumn)
                .columnWithReference(TransactionModelQueryBuilder.payeeColumn, "BIGINT", PayeeModelQueryBuilder.table, PayeeModelQueryBuilder.identityColumn)
                .column(TransactionModelQueryBuilder.dateColumn, "BIGINT NOT NULL")
                .column(TransactionModelQueryBuilder.memoColumn, "TEXT")
                .build())
    }
}
