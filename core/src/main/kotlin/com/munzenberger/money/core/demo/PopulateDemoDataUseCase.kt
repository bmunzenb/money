package com.munzenberger.money.core.demo

import com.munzenberger.money.core.logger
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.bank.Bank
import kotlinx.coroutines.flow.first
import java.util.logging.Level
import kotlin.coroutines.cancellation.CancellationException

/**
 * Adds a handful of sample banks to the repository, each with a few accounts of varying account types, in
 * a single transaction. Meant for a new, empty demo database.
 */
class PopulateDemoDataUseCase {
    suspend operator fun invoke(repository: MoneyRepository): PopulateDemoDataResult =
        try {
            val accountTypes = repository.accountTypes.first().associateBy { it.value }
            val banks = demoBanks.map { it to Bank(name = it.name) }
            repository.transaction {
                banks.forEach { (demoBank, bank) ->
                    add(bank)
                    demoBank.accounts.forEach { add(it.toAccount(bank, accountTypes)) }
                }
            }
            PopulateDemoDataResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.log(Level.WARNING, "Failed to populate the demo data", e)
            PopulateDemoDataResult.Failure(e)
        }
}

private class DemoBank(val name: String, val accounts: List<DemoAccount>)

/** A sample account; [initialBalance] is in cents, and negative for money owed. */
private class DemoAccount(
    val name: String,
    val number: String,
    val type: AccountTypeConstant,
    val initialBalance: Long,
)

private fun DemoAccount.toAccount(bank: Bank, accountTypes: Map<AccountTypeConstant, AccountType>) =
    Account(
        name = name,
        number = number,
        accountType = checkNotNull(accountTypes[type]) { "No account type $type in the repository" },
        bankId = bank.id,
        initialBalance = Money(initialBalance),
    )

private val demoBanks = listOf(
    DemoBank(
        name = "First National Bank",
        accounts = listOf(
            DemoAccount("Everyday Checking", "1001234567", AccountTypeConstant.Checking, 2_345_67),
            DemoAccount("High-Yield Savings", "1001234568", AccountTypeConstant.Savings, 12_500_00),
            DemoAccount("Rewards Visa", "4111111111111111", AccountTypeConstant.Credit, -1_234_56),
        ),
    ),
    DemoBank(
        name = "Lakeshore Credit Union",
        accounts = listOf(
            DemoAccount("Share Checking", "2002345678", AccountTypeConstant.Checking, 876_54),
            DemoAccount("Holiday Savings", "2002345679", AccountTypeConstant.Savings, 1_500_00),
            DemoAccount("Auto Loan", "2002345680", AccountTypeConstant.Loan, -18_750_00),
        ),
    ),
    DemoBank(
        name = "Summit Investments",
        accounts = listOf(
            DemoAccount("Brokerage", "3003456789", AccountTypeConstant.Asset, 45_210_33),
            DemoAccount("Roth IRA", "3003456790", AccountTypeConstant.Asset, 28_900_00),
        ),
    ),
    DemoBank(
        name = "Riverside Savings Bank",
        accounts = listOf(
            DemoAccount("Joint Checking", "4004567890", AccountTypeConstant.Checking, 3_120_45),
            DemoAccount("12-Month CD", "4004567891", AccountTypeConstant.Savings, 10_000_00),
            DemoAccount("Home Mortgage", "4004567892", AccountTypeConstant.Loan, -245_000_00),
        ),
    ),
)
