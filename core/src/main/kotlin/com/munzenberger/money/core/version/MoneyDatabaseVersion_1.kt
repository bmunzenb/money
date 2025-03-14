package com.munzenberger.money.core.version

import com.munzenberger.money.core.MoneyDatabase
import com.munzenberger.money.sql.createIndex
import com.munzenberger.money.sql.createTable
import com.munzenberger.money.sql.insertQuery
import com.munzenberger.money.version.ApplicableVersion

@Suppress("ktlint:standard:class-naming")
class MoneyDatabaseVersion_1 : ApplicableVersion<MoneyDatabase> {
    override val versionId = 1L

    override fun apply(obj: MoneyDatabase) {
        with(obj) {
            createTable("BANKS") {
                column("BANK_ID", dialect.identityColumnType)
                column("BANK_NAME", "TEXT NOT NULL")
            }

            createTable("ACCOUNT_TYPES") {
                column("ACCOUNT_TYPE_ID", dialect.identityColumnType)
                column("ACCOUNT_TYPE_GROUP", "TEXT NOT NULL")
                column("ACCOUNT_TYPE_VARIANT", "TEXT NOT NULL")
                constraint("ACCOUNT_TYPE_GROUP_CONSTRAINT", "CHECK (ACCOUNT_TYPE_GROUP IN ('ASSETS', 'LIABILITIES'))")
                constraint(
                    "ACCOUNT_TYPE_VARIANT_CONSTRAINT",
                    "CHECK (ACCOUNT_TYPE_VARIANT IN ('SAVINGS', 'CHECKING', 'ASSET', 'CASH', 'CREDIT', 'LOAN'))",
                )
            }

            createTable("PAYEES") {
                column("PAYEE_ID", dialect.identityColumnType)
                column("PAYEE_NAME", "TEXT NOT NULL")
            }

            createTable("CATEGORIES") {
                column("CATEGORY_ID", dialect.identityColumnType)
                column("CATEGORY_NAME", "TEXT NOT NULL")
                column("CATEGORY_PARENT_ID", dialect.identityType) {
                    references("CATEGORIES", "CATEGORY_ID")
                }
                column("CATEGORY_TYPE", "TEXT NOT NULL")
                constraint("CATEGORY_TYPE_CONSTRAINT", "CHECK (CATEGORY_TYPE IN ('INCOME', 'EXPENSE'))")
            }

            createTable("ACCOUNTS") {
                column("ACCOUNT_ID", dialect.identityColumnType)
                column("ACCOUNT_NAME", "TEXT NOT NULL")
                column("ACCOUNT_NUMBER", "TEXT")
                column("ACCOUNT_TYPE_ID", dialect.identityType("NOT NULL")) {
                    references("ACCOUNT_TYPES", "ACCOUNT_TYPE_ID")
                }
                column("ACCOUNT_BANK_ID", dialect.identityType) {
                    references("BANKS", "BANK_ID")
                }
                column("ACCOUNT_INITIAL_BALANCE", "BIGINT")
            }

            createTable("TRANSACTIONS") {
                column("TRANSACTION_ID", dialect.identityColumnType)
                column("TRANSACTION_ACCOUNT_ID", dialect.identityType("NOT NULL")) {
                    references("ACCOUNTS", "ACCOUNT_ID")
                }
                column("TRANSACTION_PAYEE_ID", dialect.identityType) {
                    references("PAYEES", "PAYEE_ID")
                }
                column("TRANSACTION_DATE", "BIGINT NOT NULL")
                column("TRANSACTION_NUMBER", "TEXT")
                column("TRANSACTION_MEMO", "TEXT")
                column("TRANSACTION_STATUS", "TEXT NOT NULL")
                constraint("TRANSACTION_STATUS_CONSTRAINT", "CHECK (TRANSACTION_STATUS IN ('UNRECONCILED', 'CLEARED', 'RECONCILED'))")
            }

            createIndex("TRANSACTION_ACCOUNT_INDEX", "TRANSACTIONS") {
                column("TRANSACTION_ACCOUNT_ID")
            }

            createTable("TRANSFER_ENTRIES") {
                column("TRANSFER_ENTRY_ID", dialect.identityColumnType)
                column("TRANSFER_ENTRY_TRANSACTION_ID", dialect.identityType("NOT NULL")) {
                    references("TRANSACTIONS", "TRANSACTION_ID")
                }
                column("TRANSFER_ENTRY_ACCOUNT_ID", dialect.identityType("NOT NULL")) {
                    references("ACCOUNTS", "ACCOUNT_ID")
                }
                column("TRANSFER_ENTRY_AMOUNT", "BIGINT NOT NULL")
                column("TRANSFER_ENTRY_NUMBER", "TEXT")
                column("TRANSFER_ENTRY_MEMO", "TEXT")
                column("TRANSFER_ENTRY_STATUS", "TEXT NOT NULL")
                column("TRANSFER_ENTRY_ORDER_IN_TRANSACTION", "INTEGER NOT NULL")
                constraint(
                    "TRANSFER_ENTRY_STATUS_CONSTRAINT",
                    "CHECK (TRANSFER_ENTRY_STATUS IN ('UNRECONCILED', 'CLEARED', 'RECONCILED'))",
                )
            }

            createIndex("TRANSFER_ENTRY_TRANSACTION_ACCOUNT_INDEX", "TRANSFER_ENTRIES") {
                column("TRANSFER_ENTRY_TRANSACTION_ID")
                column("TRANSFER_ENTRY_ACCOUNT_ID")
            }

            createTable("CATEGORY_ENTRIES") {
                column("CATEGORY_ENTRY_ID", dialect.identityColumnType)
                column("CATEGORY_ENTRY_TRANSACTION_ID", dialect.identityType("NOT NULL")) {
                    references("TRANSACTIONS", "TRANSACTION_ID")
                }
                column("CATEGORY_ENTRY_CATEGORY_ID", dialect.identityType("NOT NULL")) {
                    references("CATEGORIES", "CATEGORY_ID")
                }
                column("CATEGORY_ENTRY_AMOUNT", "BIGINT NOT NULL")
                column("CATEGORY_ENTRY_MEMO", "TEXT")
                column("CATEGORY_ENTRY_ORDER_IN_TRANSACTION", "INTEGER NOT NULL")
            }

            createIndex("CATEGORY_ENTRY_TRANSACTION_INDEX", "CATEGORY_ENTRIES") {
                column("CATEGORY_ENTRY_TRANSACTION_ID")
            }

            createTable("STATEMENTS") {
                column("STATEMENT_ID", dialect.identityColumnType)
                column("STATEMENT_ACCOUNT_ID", dialect.identityType("NOT NULL")) {
                    references("ACCOUNTS", "ACCOUNT_ID")
                }
                column("STATEMENT_CLOSING_DATE", "BIGINT NOT NULL")
                column("STATEMENT_STARTING_BALANCE", "BIGINT NOT NULL")
                column("STATEMENT_ENDING_BALANCE", "BIGINT NOT NULL")
                column("STATEMENT_IS_RECONCILED", dialect.booleanType("NOT NULL"))
            }

            insertAccountType("ASSETS", "SAVINGS")
            insertAccountType("ASSETS", "CHECKING")
            insertAccountType("ASSETS", "ASSET")
            insertAccountType("ASSETS", "CASH")
            insertAccountType("LIABILITIES", "CREDIT")
            insertAccountType("LIABILITIES", "LOAN")
        }
    }

    private fun MoneyDatabase.insertAccountType(
        group: String,
        variant: String,
    ) {
        executeUpdate(
            insertQuery("ACCOUNT_TYPES") {
                set("ACCOUNT_TYPE_GROUP", group)
                set("ACCOUNT_TYPE_VARIANT", variant)
            },
        )
    }
}
