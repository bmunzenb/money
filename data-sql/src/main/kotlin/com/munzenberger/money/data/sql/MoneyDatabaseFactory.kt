package com.munzenberger.money.data.sql

import app.cash.sqldelight.db.SqlDriver

/** Creates a [MoneyDatabase] on [driver], with [UuidColumnAdapter] for each `BLOB AS Uuid` column. */
internal fun createMoneyDatabase(driver: SqlDriver): MoneyDatabase =
    MoneyDatabase(
        driver = driver,
        AccountAdapter = Account.Adapter(
            idAdapter = UuidColumnAdapter,
            bank_idAdapter = UuidColumnAdapter,
        ),
        BankAdapter = Bank.Adapter(
            idAdapter = UuidColumnAdapter,
        ),
        CategoryAdapter = Category.Adapter(
            idAdapter = UuidColumnAdapter,
            parent_idAdapter = UuidColumnAdapter,
        ),
        CategoryEntryAdapter = CategoryEntry.Adapter(
            idAdapter = UuidColumnAdapter,
            transaction_idAdapter = UuidColumnAdapter,
            category_idAdapter = UuidColumnAdapter,
        ),
        PayeeAdapter = Payee.Adapter(
            idAdapter = UuidColumnAdapter,
        ),
        StatementAdapter = Statement.Adapter(
            idAdapter = UuidColumnAdapter,
            account_idAdapter = UuidColumnAdapter,
        ),
        TransactionAdapter = Transaction.Adapter(
            idAdapter = UuidColumnAdapter,
            account_idAdapter = UuidColumnAdapter,
            payee_idAdapter = UuidColumnAdapter,
        ),
        TransferEntryAdapter = TransferEntry.Adapter(
            idAdapter = UuidColumnAdapter,
            transaction_idAdapter = UuidColumnAdapter,
            account_idAdapter = UuidColumnAdapter,
        ),
    )
