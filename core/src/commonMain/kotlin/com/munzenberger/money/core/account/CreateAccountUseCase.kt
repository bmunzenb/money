package com.munzenberger.money.core.account

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.logger
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.bank.Bank
import java.util.logging.Level
import kotlin.coroutines.cancellation.CancellationException

/** The values entered for a new account, as typed into the form. */
data class NewAccount(
    val name: String,
    val accountType: AccountType?,
    /** The financial institution's name: an existing bank's name, a new one, or blank for none. */
    val bankName: String,
    /** The existing bank to use, or null to add a bank named [bankName] (if it isn't blank). */
    val bank: Bank?,
    val number: String,
    /** The opening balance; blank means zero. */
    val initialBalance: String,
    val memo: String,
)

/** Why a [NewAccount] can't be saved. Each error is for a single field. */
enum class NewAccountError {
    BlankName,
    MissingAccountType,
    InvalidInitialBalance,
}

sealed interface CreateAccountResult {
    data class Success(val account: Account) : CreateAccountResult

    /** The input failed validation, so nothing was written. */
    data class Invalid(val errors: Set<NewAccountError>) : CreateAccountResult

    /** The input was valid, but the account couldn't be written, or no repository is open. */
    data class Failure(val cause: Throwable) : CreateAccountResult
}

/**
 * Validates a [NewAccount] and, if it's valid, adds it to the open repository. A new financial
 * institution is added in the same transaction as the account.
 */
class CreateAccountUseCase(
    private val repositoryController: MoneyRepositoryController,
) {
    suspend operator fun invoke(newAccount: NewAccount): CreateAccountResult {
        val accountType = newAccount.accountType
        val initialBalance = parseInitialBalance(newAccount.initialBalance)
        val errors = buildSet {
            if (newAccount.name.isBlank()) add(NewAccountError.BlankName)
            if (accountType == null) add(NewAccountError.MissingAccountType)
            if (initialBalance == null) add(NewAccountError.InvalidInitialBalance)
        }
        if (errors.isNotEmpty() || accountType == null || initialBalance == null) {
            return CreateAccountResult.Invalid(errors)
        }

        val repository = repositoryController.moneyRepository.value
            ?: return CreateAccountResult.Failure(IllegalStateException("No money repository is open"))

        val newBank = newAccount.bankName.trim()
            .takeIf { newAccount.bank == null && it.isNotEmpty() }
            ?.let { Bank(name = it) }

        val account = Account(
            name = newAccount.name.trim(),
            number = newAccount.number.trim().ifEmpty { null },
            accountType = accountType,
            bankId = (newAccount.bank ?: newBank)?.id,
            initialBalance = initialBalance,
            memo = newAccount.memo.trim().ifEmpty { null },
        )

        return try {
            repository.transaction {
                newBank?.let { add(it) }
                add(account)
            }
            CreateAccountResult.Success(account)
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.log(Level.WARNING, "Failed to save the new account", e)
            CreateAccountResult.Failure(e)
        }
    }
}

/**
 * The amount in [initialBalance], formatted for the default locale; zero if it's blank, or null if it
 * isn't a valid amount.
 */
fun parseInitialBalance(initialBalance: String): Money? =
    if (initialBalance.isBlank()) {
        Money(0)
    } else {
        try {
            Money.parse(initialBalance)
        } catch (_: NumberFormatException) {
            null
        } catch (_: ArithmeticException) {
            // Too large to fit in a Money.
            null
        }
    }
