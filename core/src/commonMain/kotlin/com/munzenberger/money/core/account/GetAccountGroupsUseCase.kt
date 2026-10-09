package com.munzenberger.money.core.account

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.resultFlow
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** How [GetAccountGroupsUseCase] groups the accounts. */
enum class AccountGrouping {
    /** A single group with all the accounts. */
    None,
    AccountType,
    AccountClass,
    Bank,
}

/** Accounts that share a value of an [AccountGrouping], sorted by name. */
sealed interface AccountGroup {
    val accounts: List<Account>

    data class All(override val accounts: List<Account>) : AccountGroup

    data class ByAccountType(val accountType: AccountType, override val accounts: List<Account>) : AccountGroup

    data class ByAccountClass(val accountClass: AccountClass, override val accounts: List<Account>) : AccountGroup

    /** The accounts at [bank], or those with no financial institution if it's null. */
    data class ByBank(val bank: Bank?, override val accounts: List<Account>) : AccountGroup
}

/**
 * Returns a flow of the open repository's accounts, grouped by the latest [AccountGrouping], each
 * wrapped in a [Result] (see [resultFlow]).
 *
 * A new grouping regroups the accounts already read, without reading them from the repository again.
 *
 * The accounts in each group are sorted by name. Account type and account class groups are in the
 * order of their constants, and bank groups are sorted by name, followed by the group of accounts
 * with no financial institution. [AccountGrouping.None] has a single group of all the accounts. Every
 * grouping only has groups that have accounts, so there are no groups when there are no accounts.
 */
class GetAccountGroupsUseCase(
    private val repositoryController: MoneyRepositoryController,
) {
    operator fun invoke(grouping: Flow<AccountGrouping>): Flow<Result<List<AccountGroup>>> =
        repositoryController.resultFlow { repository ->
            combine(repository.accounts, repository.banks, grouping) { accounts, banks, grouping ->
                when (grouping) {
                    AccountGrouping.None -> if (accounts.isEmpty()) emptyList() else listOf(AccountGroup.All(accounts.sortedByName()))
                    AccountGrouping.AccountType -> groupByAccountType(accounts)
                    AccountGrouping.AccountClass -> groupByAccountClass(accounts)
                    AccountGrouping.Bank -> groupByBank(accounts, banks)
                }
            }
        }
}

private fun List<Account>.sortedByName(): List<Account> = sortedBy { it.name.lowercase() }

private fun groupByAccountType(accounts: List<Account>): List<AccountGroup> =
    accounts.groupBy { it.accountType }
        .entries.sortedBy { it.key.value.ordinal }
        .map { (accountType, accounts) -> AccountGroup.ByAccountType(accountType, accounts.sortedByName()) }

private fun groupByAccountClass(accounts: List<Account>): List<AccountGroup> =
    accounts.groupBy { it.accountType.accountClass }
        .entries.sortedBy { it.key.value.ordinal }
        .map { (accountClass, accounts) -> AccountGroup.ByAccountClass(accountClass, accounts.sortedByName()) }

private fun groupByBank(accounts: List<Account>, banks: List<Bank>): List<AccountGroup> {
    val banksById = banks.associateBy { it.id }
    // An account whose bank isn't in the repository is grouped with the accounts that have no bank.
    return accounts.groupBy { account -> account.bankId?.let { banksById[it] } }
        .entries.sortedWith(compareBy(nullsLast()) { it.key?.name?.lowercase() })
        .map { (bank, accounts) -> AccountGroup.ByBank(bank, accounts.sortedByName()) }
}
