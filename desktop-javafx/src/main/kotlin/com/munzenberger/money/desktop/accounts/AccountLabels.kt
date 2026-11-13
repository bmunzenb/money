package com.munzenberger.money.desktop.accounts

import com.munzenberger.money.core.account.AccountGroup
import com.munzenberger.money.core.account.AccountGrouping
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.desktop.strings
import javafx.util.StringConverter

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

/** The group's name, as shown to the user. */
internal fun AccountGroup.title(): String = when (this) {
    is AccountGroup.All -> strings.getString("account_group_all_title")
    is AccountGroup.ByAccountType -> accountType.value.label
    is AccountGroup.ByAccountClass -> strings.getString(
        when (accountClass.value) {
            AccountClassConstant.Asset -> "account_class_assets"
            AccountClassConstant.Liability -> "account_class_liabilities"
        }
    )
    is AccountGroup.ByBank -> bank?.name ?: strings.getString("account_group_no_bank_title")
}

/** Shows each grouping by its name, for a combo box that isn't editable. */
internal object AccountGroupingConverter : StringConverter<AccountGrouping>() {
    override fun toString(grouping: AccountGrouping?): String = grouping?.let {
        strings.getString(
            when (it) {
                AccountGrouping.None -> "account_grouping_none"
                AccountGrouping.AccountType -> "account_grouping_account_type"
                AccountGrouping.AccountClass -> "account_grouping_account_class"
                AccountGrouping.Bank -> "account_grouping_bank"
            }
        )
    }.orEmpty()

    // The combo box isn't editable, so it never converts text back to a grouping.
    override fun fromString(string: String?): AccountGrouping? = null
}

/** Shows each account type by its name, for a combo box that isn't editable. */
internal object AccountTypeConverter : StringConverter<AccountType>() {
    override fun toString(accountType: AccountType?): String = accountType?.value?.label.orEmpty()

    // The combo box isn't editable, so it never converts text back to an account type.
    override fun fromString(string: String?): AccountType? = null
}

/** Shows each account by its name. */
internal object AccountConverter : StringConverter<Account>() {
    override fun toString(account: Account?): String = account?.name.orEmpty()

    // The combo box isn't editable, so it never converts text back to an account.
    override fun fromString(string: String?): Account? = null
}
