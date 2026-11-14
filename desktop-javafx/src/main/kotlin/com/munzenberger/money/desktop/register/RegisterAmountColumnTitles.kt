package com.munzenberger.money.desktop.register

import com.munzenberger.money.desktop.strings
import com.munzenberger.money.presentation.register.RegisterAmountColumns

/** The title for amounts that lower the account's balance, such as Payment for a bank account. */
val RegisterAmountColumns.decreaseTitle: String
    get() = strings.getString(
        when (this) {
            RegisterAmountColumns.Banking -> "register_banking_decrease_column_title"
            RegisterAmountColumns.CreditCard -> "register_credit_card_decrease_column_title"
            RegisterAmountColumns.Loan -> "register_loan_decrease_column_title"
            RegisterAmountColumns.Other -> "register_other_decrease_column_title"
        }
    )

/** The title for amounts that raise the account's balance, such as Deposit for a bank account. */
val RegisterAmountColumns.increaseTitle: String
    get() = strings.getString(
        when (this) {
            RegisterAmountColumns.Banking -> "register_banking_increase_column_title"
            RegisterAmountColumns.CreditCard -> "register_credit_card_increase_column_title"
            RegisterAmountColumns.Loan -> "register_loan_increase_column_title"
            RegisterAmountColumns.Other -> "register_other_increase_column_title"
        }
    )
