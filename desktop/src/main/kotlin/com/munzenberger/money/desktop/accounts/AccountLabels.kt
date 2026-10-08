package com.munzenberger.money.desktop.accounts

import androidx.compose.runtime.Composable
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountTypeConstant
import money.shared.generated.resources.Res
import money.shared.generated.resources.account_class_assets
import money.shared.generated.resources.account_class_liabilities
import money.shared.generated.resources.account_type_asset
import money.shared.generated.resources.account_type_cash
import money.shared.generated.resources.account_type_checking
import money.shared.generated.resources.account_type_credit
import money.shared.generated.resources.account_type_loan
import money.shared.generated.resources.account_type_savings
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AccountTypeConstant.label(): String = stringResource(
    when (this) {
        AccountTypeConstant.Savings -> Res.string.account_type_savings
        AccountTypeConstant.Checking -> Res.string.account_type_checking
        AccountTypeConstant.Asset -> Res.string.account_type_asset
        AccountTypeConstant.Cash -> Res.string.account_type_cash
        AccountTypeConstant.Credit -> Res.string.account_type_credit
        AccountTypeConstant.Loan -> Res.string.account_type_loan
    }
)

@Composable
internal fun AccountClassConstant.label(): String = stringResource(
    when (this) {
        AccountClassConstant.Assets -> Res.string.account_class_assets
        AccountClassConstant.Liabilities -> Res.string.account_class_liabilities
    }
)

/**
 * The account number with all but its last [VISIBLE_ACCOUNT_NUMBER_DIGITS] characters hidden, e.g.
 * "••••1234", or an empty string if there's no number. A number no longer than that still gets the
 * mask prefix, so every number in a list has the same form.
 */
internal fun maskAccountNumber(number: String?): String {
    val trimmed = number?.trim().orEmpty()
    return if (trimmed.isEmpty()) "" else ACCOUNT_NUMBER_MASK + trimmed.takeLast(VISIBLE_ACCOUNT_NUMBER_DIGITS)
}

private const val VISIBLE_ACCOUNT_NUMBER_DIGITS = 4
private const val ACCOUNT_NUMBER_MASK = "••••"
