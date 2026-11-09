package com.munzenberger.money.core.register

import com.munzenberger.money.core.resultFlow
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountId
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.data.api.today
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

/**
 * Returns a flow of the [AccountRegister] of the repository's account with [AccountId]: its register entries, each with
 * the account's balance after it, filtered by the latest [RegisterDateFilter] and [RegisterStatusFilter], and its
 * ending balance, wrapped in a [Result] (see [resultFlow]). It emits again whenever the entries change.
 *
 * A new filter refilters the entries already read, without reading them from the repository again. Each entry keeps
 * its balance in the whole register, and the ending balance is the last entry's balance before filtering, so filtering
 * doesn't change the balances shown. Date filters are relative to [today], read each time the entries are filtered.
 *
 * If there's no such account, such as after it's removed, it emits a register with no entries and no ending balance.
 */
class GetAccountRegisterUseCase(
    private val today: () -> LocalDate = { LocalDate.today() },
) {
    operator fun invoke(
        repository: MoneyRepository,
        accountId: AccountId,
        dateFilter: Flow<RegisterDateFilter>,
        statusFilter: Flow<RegisterStatusFilter>,
    ): Flow<Result<AccountRegister>> =
        repository.resultFlow {
            combine(
                it.registerEntriesForAccount(accountId),
                dateFilter,
                statusFilter,
            ) { entries, dateFilter, statusFilter ->
                val today = today()
                AccountRegister(
                    entries = entries.filter { entry ->
                        dateFilter.matches(entry.date, today) && statusFilter.matches(entry)
                    },
                    endingBalance = entries.lastOrNull()?.balance,
                )
            }
        }
}

private const val LAST_3_MONTHS = 3
private const val LAST_12_MONTHS = 12

private fun RegisterDateFilter.matches(date: LocalDate, today: LocalDate): Boolean =
    when (this) {
        RegisterDateFilter.AllDates -> true
        RegisterDateFilter.CurrentMonth -> date.year == today.year && date.month == today.month
        RegisterDateFilter.CurrentYear -> date.year == today.year
        RegisterDateFilter.Last3Months -> date >= today.minus(LAST_3_MONTHS, DateTimeUnit.MONTH)
        RegisterDateFilter.Last12Months -> date >= today.minus(LAST_12_MONTHS, DateTimeUnit.MONTH)
    }

private fun RegisterStatusFilter.matches(entry: RegisterEntry): Boolean =
    when (this) {
        RegisterStatusFilter.AllTransactions -> true
        RegisterStatusFilter.UnreconciledTransactions -> entry.status.value != TransactionStatusConstant.Reconciled
    }
