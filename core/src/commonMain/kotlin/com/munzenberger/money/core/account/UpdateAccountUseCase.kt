package com.munzenberger.money.core.account

import com.munzenberger.money.core.MoneyRepositoryController
import com.munzenberger.money.core.logger
import com.munzenberger.money.data.api.account.AccountId
import java.util.logging.Level
import kotlin.coroutines.cancellation.CancellationException

/**
 * Validates an [AccountInput] and, if it's valid, replaces the existing account with [AccountId] in the
 * open repository with it. A new financial institution is added in the same transaction as the account.
 */
class UpdateAccountUseCase(
    private val repositoryController: MoneyRepositoryController,
) {
    suspend operator fun invoke(accountId: AccountId, input: AccountInput): SaveAccountResult {
        val (account, newBank) = when (val validated = input.validate(accountId)) {
            is ValidatedAccountInput.Invalid -> return SaveAccountResult.Invalid(validated.errors)
            is ValidatedAccountInput.Valid -> validated
        }

        val repository = repositoryController.moneyRepository.value
            ?: return SaveAccountResult.Failure(IllegalStateException("No money repository is open"))

        return try {
            repository.transaction {
                newBank?.let { add(it) }
                update(account)
            }
            SaveAccountResult.Success(account)
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.log(Level.WARNING, "Failed to update the account", e)
            SaveAccountResult.Failure(e)
        }
    }
}
