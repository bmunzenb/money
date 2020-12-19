package com.munzenberger.money.app.model

import com.munzenberger.money.core.AccountType
import com.munzenberger.money.core.model.AccountTypeVariant

val AccountType.name: String?
    get() = when (variant) {
        AccountTypeVariant.SAVINGS -> "Savings"
        AccountTypeVariant.CHECKING -> "Checking"
        AccountTypeVariant.ASSET -> "Asset"
        AccountTypeVariant.CASH -> "Cash"
        AccountTypeVariant.CREDIT -> "Credit Card"
        AccountTypeVariant.LOAN -> "Loan"
        null -> null
    }
