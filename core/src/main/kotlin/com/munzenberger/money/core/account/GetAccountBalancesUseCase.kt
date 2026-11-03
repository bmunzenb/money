package com.munzenberger.money.core.account

import com.munzenberger.money.core.resultFlow
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.MoneyRepository
import com.munzenberger.money.data.api.account.AccountId
import kotlinx.coroutines.flow.Flow

/**
 * Returns a flow of the balances of the repository's accounts, keyed by account id, each wrapped in a [Result] (see
 * [resultFlow]). The balances are read together, so they load, and fail, together.
 */
class GetAccountBalancesUseCase {
    operator fun invoke(repository: MoneyRepository): Flow<Result<Map<AccountId, Money>>> =
        repository.resultFlow { it.accountBalances }
}
