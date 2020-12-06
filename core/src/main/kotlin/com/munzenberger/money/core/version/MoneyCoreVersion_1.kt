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
                .column("ACCOUNT_TYPE_IS_CATEGORY", obj.dialect.booleanType("NOT NULL"))
                .constraint("ACCOUNT_TYPE_GROUP_CONSTRAINT", "CHECK (ACCOUNT_TYPE_GROUP IN ('ASSETS', 'LIABILITIES', 'INCOME', 'EXPENSES'))")
                .constraint("ACCOUNT_TYPE_VARIANT_CONSTRAINT", "CHECK (ACCOUNT_TYPE_VARIANT IN ('SAVINGS', 'CHECKING', 'ASSET', 'CASH', 'CREDIT', 'LOAN', 'INCOME', 'EXPENSE'))")
                .build())

        obj.execute(Query.createTable("PAYEES")
                .column("PAYEE_ID", obj.dialect.identityColumnType)
                .column("PAYEE_NAME", "TEXT NOT NULL")
                .build())

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

        obj.execute(Query.createTable("TRANSFERS")
                .column("TRANSFER_ID", obj.dialect.identityColumnType)
                .columnWithReference("TRANSFER_TRANSACTION_ID", obj.dialect.identityType("NOT NULL"), "TRANSACTIONS", "TRANSACTION_ID")
                .columnWithReference("TRANSFER_ACCOUNT_ID", obj.dialect.identityType("NOT NULL"), "ACCOUNTS", "ACCOUNT_ID")
                .column("TRANSFER_AMOUNT", "BIGINT NOT NULL")
                .column("TRANSFER_NUMBER", "TEXT")
                .column("TRANSFER_MEMO", "TEXT")
                .column("TRANSFER_STATUS", "TEXT NOT NULL")
                .constraint("TRANSFER_STATUS_CONSTRAINT", "CHECK (TRANSFER_STATUS IN ('UNRECONCILED', 'CLEARED', 'RECONCILED'))")
                .build())

        obj.executeUpdate(Query.insertInto("ACCOUNT_TYPES")
                .set("ACCOUNT_TYPE_GROUP", "ASSETS")
                .set("ACCOUNT_TYPE_VARIANT", "SAVINGS")
                .set("ACCOUNT_TYPE_IS_CATEGORY", false)
                .build())

        obj.executeUpdate(Query.insertInto("ACCOUNT_TYPES")
                .set("ACCOUNT_TYPE_GROUP", "ASSETS")
                .set("ACCOUNT_TYPE_VARIANT", "CHECKING")
                .set("ACCOUNT_TYPE_IS_CATEGORY", false)
                .build())

        obj.executeUpdate(Query.insertInto("ACCOUNT_TYPES")
                .set("ACCOUNT_TYPE_GROUP", "ASSETS")
                .set("ACCOUNT_TYPE_VARIANT", "ASSET")
                .set("ACCOUNT_TYPE_IS_CATEGORY", false)
                .build())

        obj.executeUpdate(Query.insertInto("ACCOUNT_TYPES")
                .set("ACCOUNT_TYPE_GROUP", "ASSETS")
                .set("ACCOUNT_TYPE_VARIANT", "CASH")
                .set("ACCOUNT_TYPE_IS_CATEGORY", false)
                .build())

        obj.executeUpdate(Query.insertInto("ACCOUNT_TYPES")
                .set("ACCOUNT_TYPE_GROUP", "LIABILITIES")
                .set("ACCOUNT_TYPE_VARIANT", "CREDIT")
                .set("ACCOUNT_TYPE_IS_CATEGORY", false)
                .build())

        obj.executeUpdate(Query.insertInto("ACCOUNT_TYPES")
                .set("ACCOUNT_TYPE_GROUP", "LIABILITIES")
                .set("ACCOUNT_TYPE_VARIANT", "LOAN")
                .set("ACCOUNT_TYPE_IS_CATEGORY", false)
                .build())

        obj.executeUpdate(Query.insertInto("ACCOUNT_TYPES")
                .set("ACCOUNT_TYPE_GROUP", "INCOME")
                .set("ACCOUNT_TYPE_VARIANT", "INCOME")
                .set("ACCOUNT_TYPE_IS_CATEGORY", true)
                .build())

        obj.executeUpdate(Query.insertInto("ACCOUNT_TYPES")
                .set("ACCOUNT_TYPE_GROUP", "EXPENSES")
                .set("ACCOUNT_TYPE_VARIANT", "EXPENSE")
                .set("ACCOUNT_TYPE_IS_CATEGORY", true)
                .build())
    }
}
