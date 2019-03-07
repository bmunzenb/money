package com.munzenberger.money.core.version

import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.core.model.*
import com.munzenberger.money.sql.Query
import com.munzenberger.money.version.ApplicableVersion

class MoneyCoreVersion_1 : ApplicableVersion<MoneyDatabase> {

    override val versionId = 1L

    override fun apply(obj: MoneyDatabase) {

        obj.execute(Query.createTable(BankTable.name)
                .column(BankTable.identityColumn, obj.dialect.identityColumnType)
                .column(BankTable.nameColumn, "TEXT NOT NULL")
                .build())

        obj.execute(Query.createTable(AccountTypeTable.name)
                .column(AccountTypeTable.identityColumn, obj.dialect.identityColumnType)
                .column(AccountTypeTable.nameColumn, "TEXT NOT NULL")
                .column(AccountTypeTable.categoryColumn, "TEXT NOT NULL")
                .build())

        obj.execute(Query.createTable(PayeeTable.name)
                .column(PayeeTable.identityColumn, obj.dialect.identityColumnType)
                .column(PayeeTable.nameColumn, "TEXT NOT NULL")
                .build())

        obj.execute(Query.createTable(AccountTable.name)
                .column(AccountTable.identityColumn, obj.dialect.identityColumnType)
                .column(AccountTable.nameColumn, "TEXT NOT NULL")
                .column(AccountTable.numberColumn, "TEXT")
                .columnWithReference(AccountTable.accountTypeColumn, "${obj.dialect.identityReferenceType} NOT NULL", AccountTypeTable.name, AccountTypeTable.identityColumn)
                .columnWithReference(AccountTable.bankColumn, obj.dialect.identityReferenceType, BankTable.name, BankTable.identityColumn)
                .build())

        obj.execute(Query.createTable(CategoryTable.name)
                .column(CategoryTable.identityColumn, obj.dialect.identityColumnType)
                .columnWithReference(CategoryTable.accountColumn, "${obj.dialect.identityReferenceType} NOT NULL", AccountTable.name, AccountTable.identityColumn)
                .column(CategoryTable.nameColumn, "TEXT")
                .build())

        obj.execute(Query.createTable(TransactionTable.name)
                .column(TransactionTable.identityColumn, obj.dialect.identityColumnType)
                .columnWithReference(TransactionTable.accountColumn, "${obj.dialect.identityReferenceType} NOT NULL", AccountTable.name, AccountTable.identityColumn)
                .columnWithReference(TransactionTable.payeeColumn, obj.dialect.identityReferenceType, PayeeTable.name, PayeeTable.identityColumn)
                .column(TransactionTable.dateColumn, "BIGINT NOT NULL")
                .column(TransactionTable.memoColumn, "TEXT")
                .build())

        obj.execute(Query.createTable(TransferTable.name)
                .column(TransferTable.identityColumn, obj.dialect.identityColumnType)
                .columnWithReference(TransferTable.transactionColumn, "${obj.dialect.identityReferenceType} NOT NULL", TransactionTable.name, TransactionTable.identityColumn)
                .columnWithReference(TransferTable.categoryColumn, "${obj.dialect.identityReferenceType} NOT NULL", CategoryTable.name, CategoryTable.identityColumn)
                .column(TransferTable.amountColumn, "BIGINT NOT NULL")
                .column(TransferTable.memoColumn, "TEXT")
                .build())
    }
}
