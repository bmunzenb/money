package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.desktop.strings

/** The account type's name, as shown to the user. */
val AccountTypeConstant.label: String
    get() = strings.getString(
        when (this) {
            AccountTypeConstant.Savings -> "account_type_savings"
            AccountTypeConstant.Checking -> "account_type_checking"
            AccountTypeConstant.Asset -> "account_type_asset"
            AccountTypeConstant.Cash -> "account_type_cash"
            AccountTypeConstant.Credit -> "account_type_credit"
            AccountTypeConstant.Loan -> "account_type_loan"
        }
    )
