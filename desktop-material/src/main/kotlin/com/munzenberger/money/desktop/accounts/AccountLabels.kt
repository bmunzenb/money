package com.munzenberger.money.desktop.accounts

import androidx.compose.runtime.Composable
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountTypeConstant
import money.desktop_material.generated.resources.Res
import money.desktop_material.generated.resources.account_class_assets
import money.desktop_material.generated.resources.account_class_liabilities
import money.desktop_material.generated.resources.account_grouping_account_class
import money.desktop_material.generated.resources.account_grouping_account_type
import money.desktop_material.generated.resources.account_grouping_bank
import money.desktop_material.generated.resources.account_grouping_none
import money.desktop_material.generated.resources.account_type_asset
import money.desktop_material.generated.resources.account_type_cash
import money.desktop_material.generated.resources.account_type_checking
import money.desktop_material.generated.resources.account_type_credit
import money.desktop_material.generated.resources.account_type_loan
import money.desktop_material.generated.resources.account_type_savings
import org.jetbrains.compose.resources.stringResource

@Composable
fun AccountTypeConstant.label(): String = stringResource(
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
fun AccountClassConstant.label(): String = stringResource(
    when (this) {
        AccountClassConstant.Assets -> Res.string.account_class_assets
        AccountClassConstant.Liabilities -> Res.string.account_class_liabilities
    }
)

@Composable
fun AccountGrouping.label(): String = stringResource(
    when (this) {
        AccountGrouping.None -> Res.string.account_grouping_none
        AccountGrouping.AccountType -> Res.string.account_grouping_account_type
        AccountGrouping.AccountClass -> Res.string.account_grouping_account_class
        AccountGrouping.Bank -> Res.string.account_grouping_bank
    }
)
