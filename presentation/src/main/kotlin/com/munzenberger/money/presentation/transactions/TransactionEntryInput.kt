package com.munzenberger.money.presentation.transactions

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.transaction.CategoryEntry
import com.munzenberger.money.data.api.transaction.TransactionEntry
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransferEntry
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** One line of the transaction's split, as the user has entered it. */
data class TransactionEntryInput @OptIn(ExperimentalUuidApi::class) constructor(
    /** Identifies the row in the list, since its entry gets a new id each time the transaction is saved. */
    val key: Uuid = Uuid.generateV7(),
    /**
     * The text in the category field: an existing category's or transfer account's text, or a new category's name
     * typed in. Empty for a new row, or the saved category's text of an entry being edited.
     */
    val categoryText: String = "",
    /** The category or transfer account matching [categoryText], or null if the text is blank or for a new category. */
    val category: TransactionCategory? = null,
    /** The text in the amount field, as a positive amount; the form's [TransactionType] gives the sign. */
    val amountText: String = "",
    /** Whether [amountText] was left as text that isn't a valid amount, so it's shown as an error. */
    val isAmountError: Boolean = false,
    val memo: String = "",
    /** The transfer's number. Only used when [category] is a transfer. */
    val transferNumber: String = "",
    /** The transfer's status, or null for a new transfer's default. Only used when [category] is a transfer. */
    val transferStatus: TransactionStatus? = null,
)

/**
 * The sum of the rows' amounts, as entered, with a blank amount counting as zero. Null if any row's amount isn't a
 * valid amount, or the sum is too large to fit in a [Money], since there's no total to show.
 */
fun List<TransactionEntryInput>.total(): Money? {
    val amounts = map { input -> if (input.amountText.isBlank()) Money(0) else Money.parseOrNull(input.amountText) }
    return try {
        amounts.fold(Money(0)) { total, amount -> total + (amount ?: return null) }
    } catch (_: ArithmeticException) {
        // Too large to fit in a Money.
        null
    }
}

/**
 * The entries to save for the transaction with [transactionId], in the rows' order, each with a new id. Amounts are
 * negated for a [TransactionType.Decrease]. Blank memos and transfer numbers are saved as null, and a transfer with
 * no status gets [newTransferStatus].
 *
 * Returns null if any row has no category or an amount that isn't a valid amount, since the transaction can't be
 * saved until each one does.
 */
fun List<TransactionEntryInput>.toEntries(
    transactionId: TransactionId,
    type: TransactionType,
    newTransferStatus: TransactionStatus,
): List<TransactionEntry>? = mapIndexed { index, input ->
    val category = input.category ?: return null
    val amount = Money.parseOrNull(input.amountText) ?: return null
    val signedAmount = if (type == TransactionType.Decrease) -amount else amount
    val memo = input.memo.ifBlank { null }

    when (category) {
        is TransactionCategory.Category -> CategoryEntry(
            transactionId = transactionId,
            categoryId = category.category.category.id,
            amount = signedAmount,
            memo = memo,
            orderInTransaction = index,
        )
        is TransactionCategory.Transfer -> TransferEntry(
            transactionId = transactionId,
            accountId = category.account.id,
            amount = signedAmount,
            number = input.transferNumber.ifBlank { null },
            memo = memo,
            status = input.transferStatus ?: newTransferStatus,
            orderInTransaction = index,
        )
    }
}
