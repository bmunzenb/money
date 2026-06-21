package com.munzenberger.money.data.api.account

import com.munzenberger.money.data.api.Money
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

@JvmInline
value class StatementId(val id: Uuid = Uuid.random())

data class Statement(
    val id: StatementId = StatementId(),
    val accountId: AccountId,
    val closingDate: LocalDate,
    val startingBalance: Money,
    val endingBalance: Money,
    val isReconciled: Boolean = false,
)
