package com.munzenberger.money.core.transaction

import com.munzenberger.money.core.asResult
import com.munzenberger.money.core.requireFound
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.transaction.TransactionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetTransactionUseCase {
    operator fun invoke(
        repository: MoneyRepository,
        transactionId: TransactionId,
    ): Flow<Result<TransactionWithEntries>> =
        combine(
            repository.transactionById(transactionId),
            repository.categoryEntriesByTransactionId(transactionId),
            repository.transferEntriesByTransactionId(transactionId),
        ) { transaction, categoryEntries, transferEntries ->
            if (transaction == null) return@combine null
            TransactionWithEntries(
                transaction = transaction,
                entries = (categoryEntries + transferEntries).sortedBy { it.orderInTransaction }
            )
        }.asResult().requireFound { "No transaction with id ${transactionId.value}" }
}
