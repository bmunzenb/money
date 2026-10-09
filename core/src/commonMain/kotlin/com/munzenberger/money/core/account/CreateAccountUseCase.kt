package com.munzenberger.money.core.account

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.logger
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.bank.Bank
import java.util.logging.Level
import kotlin.coroutines.cancellation.CancellationException

/**
 * Validates an [AccountInput] and, if it's valid, adds it to the open repository as a new account. A new
 * financial institution is added in the same transaction as the account.
 */
class CreateAccountUseCase(
    private val repositoryController: MoneyRepositoryController,
) {
    suspend operator fun invoke(input: AccountInput): SaveAccountResult {
        val accountType = input.accountType
        val initialBalance = parseInitialBalance(input.initialBalance)
        val errors = buildSet {
            if (input.name.isBlank()) add(AccountInputError.BlankName)
            if (accountType == null) add(AccountInputError.MissingAccountType)
            if (initialBalance == null) add(AccountInputError.InvalidInitialBalance)
        }
        if (errors.isNotEmpty() || accountType == null || initialBalance == null) {
            return SaveAccountResult.Invalid(errors)
        }

        val repository = repositoryController.moneyRepository.value
            ?: return SaveAccountResult.Failure(IllegalStateException("No money repository is open"))

        val newBank = input.bankName.trim()
            .takeIf { input.bank == null && it.isNotEmpty() }
            ?.let { Bank(name = it) }

        val account = Account(
            name = input.name.trim(),
            number = input.number.trim().ifEmpty { null },
            accountType = accountType,
            bankId = (input.bank ?: newBank)?.id,
            initialBalance = initialBalance,
            memo = input.memo.trim().ifEmpty { null },
        )

        return try {
            repository.transaction {
                newBank?.let { add(it) }
                add(account)
            }
            SaveAccountResult.Success(account)
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.log(Level.WARNING, "Failed to save the new account", e)
            SaveAccountResult.Failure(e)
        }
    }
}
