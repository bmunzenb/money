package com.munzenberger.money.core.version

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.sql.Query
import com.munzenberger.money.version.ApplicableVersion

class MoneyCoreVersion_1 : ApplicableVersion<MoneyDatabase> {

    override val versionId = 1L

    override fun apply(obj: MoneyDatabase) {

        obj.execute(Query.createTable("BANKS")
                .column("BANK_ID", obj.dialect.identityColumnType)
                .column("BANK_NAME", "TEXT NOT NULL")
                .build())

        obj.execute(Query.createTable("ACCOUNT_TYPES")
                .column("ACCOUNT_TYPE_ID", obj.dialect.identityColumnType)
                .column("ACCOUNT_TYPE_GROUP", "TEXT NOT NULL")
                .column("ACCOUNT_TYPE_VARIANT", "TEXT NOT NULL")
                .constraint("ACCOUNT_TYPE_GROUP_CONSTRAINT", "CHECK (ACCOUNT_TYPE_GROUP IN ('ASSETS', 'LIABILITIES'))")
                .constraint("ACCOUNT_TYPE_VARIANT_CONSTRAINT", "CHECK (ACCOUNT_TYPE_VARIANT IN ('SAVINGS', 'CHECKING', 'ASSET', 'CASH', 'CREDIT', 'LOAN'))")
                .build())

        obj.execute(Query.createTable("PAYEES")
                .column("PAYEE_ID", obj.dialect.identityColumnType)
                .column("PAYEE_NAME", "TEXT NOT NULL")
                .build())

        obj.execute(Query.createTable("CATEGORIES")
                .column("CATEGORY_ID", obj.dialect.identityColumnType)
                .column("CATEGORY_NAME", "TEXT NOT NULL")
                .columnWithReference("CATEGORY_PARENT_ID", obj.dialect.identityType, "CATEGORIES", "CATEGORY_ID")
                .column("CATEGORY_TYPE", "TEXT NOT NULL")
                .constraint("CATEGORY_TYPE_CONSTRAINT", "CHECK (CATEGORY_TYPE IN ('INCOME', 'EXPENSE'))")
                .build()
        )

        obj.execute(Query.createTable("ACCOUNTS")
                .column("ACCOUNT_ID", obj.dialect.identityColumnType)
                .column("ACCOUNT_NAME", "TEXT NOT NULL")
                .column("ACCOUNT_NUMBER", "TEXT")
                .columnWithReference("ACCOUNT_TYPE_ID", obj.dialect.identityType("NOT NULL"), "ACCOUNT_TYPES", "ACCOUNT_TYPE_ID")
                .columnWithReference("ACCOUNT_BANK_ID", obj.dialect.identityType, "BANKS", "BANK_ID")
                .column("ACCOUNT_INITIAL_BALANCE", "BIGINT")
                .build())

        obj.execute(Query.createTable("TRANSACTIONS")
                .column("TRANSACTION_ID", obj.dialect.identityColumnType)
                .columnWithReference("TRANSACTION_ACCOUNT_ID", obj.dialect.identityType("NOT NULL"), "ACCOUNTS", "ACCOUNT_ID")
                .columnWithReference("TRANSACTION_PAYEE_ID", obj.dialect.identityType, "PAYEES", "PAYEE_ID")
                .column("TRANSACTION_DATE", "BIGINT NOT NULL")
                .column("TRANSACTION_NUMBER", "TEXT")
                .column("TRANSACTION_MEMO", "TEXT")
                .column("TRANSACTION_STATUS", "TEXT NOT NULL")
                .constraint("TRANSACTION_STATUS_CONSTRAINT", "CHECK (TRANSACTION_STATUS IN ('UNRECONCILED', 'CLEARED', 'RECONCILED'))")
                .build())

        obj.execute(Query.createIndex("TRANSACTION_ACCOUNT_INDEX", "TRANSACTIONS")
                .column("TRANSACTION_ACCOUNT_ID")
                .build())

        obj.execute(Query.createTable("TRANSFERS")
                .column("TRANSFER_ID", obj.dialect.identityColumnType)
                .columnWithReference("TRANSFER_TRANSACTION_ID", obj.dialect.identityType("NOT NULL"), "TRANSACTIONS", "TRANSACTION_ID")
                .columnWithReference("TRANSFER_ACCOUNT_ID", obj.dialect.identityType("NOT NULL"), "ACCOUNTS", "ACCOUNT_ID")
                .column("TRANSFER_AMOUNT", "BIGINT NOT NULL")
                .column("TRANSFER_NUMBER", "TEXT")
                .column("TRANSFER_MEMO", "TEXT")
                .column("TRANSFER_STATUS", "TEXT NOT NULL")
                .column("TRANSFER_ORDER_IN_TRANSACTION", "INTEGER NOT NULL")
                .constraint("TRANSFER_STATUS_CONSTRAINT", "CHECK (TRANSFER_STATUS IN ('UNRECONCILED', 'CLEARED', 'RECONCILED'))")
                .build())

        obj.execute(Query.createIndex("TRANSFER_TRANSACTION_ACCOUNT_INDEX", "TRANSFERS")
                .column("TRANSFER_TRANSACTION_ID")
                .column("TRANSFER_ACCOUNT_ID")
                .build())

        obj.execute(Query.createTable("ENTRIES")
                .column("ENTRY_ID", obj.dialect.identityColumnType)
                .columnWithReference("ENTRY_TRANSACTION_ID", obj.dialect.identityType("NOT NULL"), "TRANSACTIONS", "TRANSACTION_ID")
                .columnWithReference("ENTRY_CATEGORY_ID", obj.dialect.identityType("NOT NULL"), "CATEGORIES", "CATEGORY_ID")
                .column("ENTRY_AMOUNT", "BIGINT NOT NULL")
                .column("ENTRY_MEMO", "TEXT")
                .column("ENTRY_ORDER_IN_TRANSACTION", "INTEGER NOT NULL")
                .build())

        obj.execute(Query.createIndex("ENTRY_TRANSACTION_INDEX", "ENTRIES")
                .column("ENTRY_TRANSACTION_ID")
                .build())

        obj.execute(Query.createTable("STATEMENTS")
                .column("STATEMENT_ID", obj.dialect.identityColumnType)
                .columnWithReference("STATEMENT_ACCOUNT_ID", obj.dialect.identityType("NOT NULL"), "ACCOUNTS", "ACCOUNT_ID")
                .column("STATEMENT_CLOSING_DATE", "BIGINT NOT NULL")
                .column("STATEMENT_ENDING_BALANCE", "BIGINT NOT NULL")
                .column("STATEMENT_IS_RECONCILED", obj.dialect.booleanType("NOT NULL"))
                .build())

        obj.insertAccountType("ASSETS", "SAVINGS")
        obj.insertAccountType("ASSETS", "CHECKING")
        obj.insertAccountType("ASSETS", "ASSET")
        obj.insertAccountType("ASSETS", "CASH")
        obj.insertAccountType("LIABILITIES", "CREDIT")
        obj.insertAccountType("LIABILITIES", "LOAN")
    }

    private fun MoneyDatabase.insertAccountType(group: String, variant: String) {
        executeUpdate(Query.insertInto("ACCOUNT_TYPES")
                .set("ACCOUNT_TYPE_GROUP", group)
                .set("ACCOUNT_TYPE_VARIANT", variant)
                .build())
    }
}
