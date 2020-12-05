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
                .column(AccountTypeTable.categoryColumn, "TEXT NOT NULL")
                .column(AccountTypeTable.variantColumn, "TEXT NOT NULL")
                .column(AccountTypeTable.isCategoryColumn, obj.dialect.booleanType("NOT NULL"))
                .constraint("ACCOUNT_TYPE_CATEGORY_CONSTRAINT", "CHECK (${AccountTypeTable.categoryColumn} IN ('${AccountType.Category.ASSETS.name}', '${AccountType.Category.LIABILITIES.name}', '${AccountType.Category.INCOME.name}', '${AccountType.Category.EXPENSES.name}'))")
                .constraint("ACCOUNT_TYPE_VARIANT_CONSTRAINT", "CHECK (${AccountTypeTable.variantColumn} IN ('${AccountType.Variant.SAVINGS.name}', '${AccountType.Variant.CHECKING.name}', '${AccountType.Variant.ASSET.name}', '${AccountType.Variant.CASH.name}', '${AccountType.Variant.CREDIT.name}', '${AccountType.Variant.LOAN.name}', '${AccountType.Variant.INCOME.name}', '${AccountType.Variant.EXPENSE.name}'))")
                .build())

        obj.execute(Query.createTable(PayeeTable.name)
                .column(PayeeTable.identityColumn, obj.dialect.identityColumnType)
                .column(PayeeTable.nameColumn, "TEXT NOT NULL")
                .build())

        obj.execute(Query.createTable(AccountTable.name)
                .column(AccountTable.identityColumn, obj.dialect.identityColumnType)
                .column(AccountTable.nameColumn, "TEXT NOT NULL")
                .column(AccountTable.numberColumn, "TEXT")
                .columnWithReference(AccountTable.accountTypeColumn, obj.dialect.identityType("NOT NULL"), AccountTypeTable.name, AccountTypeTable.identityColumn)
                .columnWithReference(AccountTable.bankColumn, obj.dialect.identityType, BankTable.name, BankTable.identityColumn)
                .column(AccountTable.initialBalanceColumn, "BIGINT")
                .build())

        obj.execute(Query.createTable(TransactionTable.name)
                .column(TransactionTable.identityColumn, obj.dialect.identityColumnType)
                .columnWithReference(TransactionTable.accountColumn, obj.dialect.identityType("NOT NULL"), AccountTable.name, AccountTable.identityColumn)
                .columnWithReference(TransactionTable.payeeColumn, obj.dialect.identityType, PayeeTable.name, PayeeTable.identityColumn)
                .column(TransactionTable.dateColumn, "BIGINT NOT NULL")
                .column(TransactionTable.numberColumn, "TEXT")
                .column(TransactionTable.memoColumn, "TEXT")
                .column(TransactionTable.statusColumn, "TEXT NOT NULL")
                .constraint("TRANSACTION_STATUS_CONSTRAINT", "CHECK (${TransactionTable.statusColumn} IN ('UNRECONCILED', 'CLEARED', 'RECONCILED'))")
                .build())

        obj.execute(Query.createTable(TransferTable.name)
                .column(TransferTable.identityColumn, obj.dialect.identityColumnType)
                .columnWithReference(TransferTable.transactionColumn, obj.dialect.identityType("NOT NULL"), TransactionTable.name, TransactionTable.identityColumn)
                .columnWithReference(TransferTable.accountColumn, obj.dialect.identityType("NOT NULL"), AccountTable.name, AccountTable.identityColumn)
                .column(TransferTable.amountColumn, "BIGINT NOT NULL")
                .column(TransferTable.numberColumn, "TEXT")
                .column(TransferTable.memoColumn, "TEXT")
                .column(TransferTable.statusColumn, "TEXT NOT NULL")
                .constraint("TRANSFER_STATUS_CONSTRAINT", "CHECK (${TransferTable.statusColumn} IN ('UNRECONCILED', 'CLEARED', 'RECONCILED'))")
                .build())

        obj.executeUpdate(Query.insertInto(AccountTypeTable.name)
                .set(AccountTypeTable.categoryColumn, AccountType.Category.ASSETS.name)
                .set(AccountTypeTable.variantColumn, AccountType.Variant.SAVINGS.name)
                .set(AccountTypeTable.isCategoryColumn, false)
                .build())

        obj.executeUpdate(Query.insertInto(AccountTypeTable.name)
                .set(AccountTypeTable.categoryColumn, AccountType.Category.ASSETS.name)
                .set(AccountTypeTable.variantColumn, AccountType.Variant.CHECKING.name)
                .set(AccountTypeTable.isCategoryColumn, false)
                .build())

        obj.executeUpdate(Query.insertInto(AccountTypeTable.name)
                .set(AccountTypeTable.categoryColumn, AccountType.Category.ASSETS.name)
                .set(AccountTypeTable.variantColumn, AccountType.Variant.ASSET.name)
                .set(AccountTypeTable.isCategoryColumn, false)
                .build())

        obj.executeUpdate(Query.insertInto(AccountTypeTable.name)
                .set(AccountTypeTable.categoryColumn, AccountType.Category.ASSETS.name)
                .set(AccountTypeTable.variantColumn, AccountType.Variant.CASH.name)
                .set(AccountTypeTable.isCategoryColumn, false)
                .build())

        obj.executeUpdate(Query.insertInto(AccountTypeTable.name)
                .set(AccountTypeTable.categoryColumn, AccountType.Category.LIABILITIES.name)
                .set(AccountTypeTable.variantColumn, AccountType.Variant.CREDIT.name)
                .set(AccountTypeTable.isCategoryColumn, false)
                .build())

        obj.executeUpdate(Query.insertInto(AccountTypeTable.name)
                .set(AccountTypeTable.categoryColumn, AccountType.Category.LIABILITIES.name)
                .set(AccountTypeTable.variantColumn, AccountType.Variant.LOAN.name)
                .set(AccountTypeTable.isCategoryColumn, false)
                .build())

        obj.executeUpdate(Query.insertInto(AccountTypeTable.name)
                .set(AccountTypeTable.categoryColumn, AccountType.Category.INCOME.name)
                .set(AccountTypeTable.variantColumn, AccountType.Variant.INCOME.name)
                .set(AccountTypeTable.isCategoryColumn, true)
                .build())

        obj.executeUpdate(Query.insertInto(AccountTypeTable.name)
                .set(AccountTypeTable.categoryColumn, AccountType.Category.EXPENSES.name)
                .set(AccountTypeTable.variantColumn, AccountType.Variant.EXPENSE.name)
                .set(AccountTypeTable.isCategoryColumn, true)
                .build())
    }
}
