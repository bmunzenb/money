package com.munzenberger.money.data.api.account

import com.munzenberger.money.data.api.Money
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
@JvmInline
value class StatementId @OptIn(ExperimentalUuidApi::class) constructor(val id: Uuid = Uuid.generateV7())

data class Statement(
    val id: StatementId = StatementId(),
    val accountId: AccountId,
    val closingDate: LocalDate,
    val startingBalance: Money,
    val endingBalance: Money,
    val isReconciled: Boolean = false,
)
