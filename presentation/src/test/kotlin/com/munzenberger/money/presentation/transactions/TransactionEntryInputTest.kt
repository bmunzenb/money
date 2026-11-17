package com.munzenberger.money.presentation.transactions

import com.munzenberger.money.core.category.CategoryWithParent
import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.category.Category
import com.munzenberger.money.data.api.category.CategoryType
import com.munzenberger.money.data.api.category.CategoryTypeConstant
import com.munzenberger.money.data.api.category.CategoryTypeId
import com.munzenberger.money.data.api.transaction.CategoryEntry
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import com.munzenberger.money.data.api.transaction.TransferEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class TransactionEntryInputTest {

    private val transactionId = TransactionId()

    private val unreconciled = TransactionStatus(TransactionStatusId(1), TransactionStatusConstant.Unreconciled)
    private val reconciled = TransactionStatus(TransactionStatusId(3), TransactionStatusConstant.Reconciled)

    private val expense = CategoryType(id = CategoryTypeId(2), value = CategoryTypeConstant.Expense)
    private val food = Category(name = "Food", type = expense)

    private val checking = AccountType(
        id = AccountTypeId(2),
        accountClass = AccountClass(id = AccountClassId(1), value = AccountClassConstant.Asset),
        value = AccountTypeConstant.Checking,
    )
    private val savings = Account(name = "Savings", accountType = checking)

    private val foodInput = TransactionEntryInput(
        category = TransactionCategory.Category(CategoryWithParent(food, parent = null)),
        amountText = "12.34",
    )
    private val savingsInput = TransactionEntryInput(
        category = TransactionCategory.Transfer(savings),
        amountText = "5",
    )

    private fun List<TransactionEntryInput>.toEntries(type: TransactionType = TransactionType.Increase) =
        toEntries(transactionId, type, newTransferStatus = unreconciled)

    @Test
    fun `a category row becomes a category entry`() {
        val entry = listOf(foodInput.copy(memo = "lunch")).toEntries()!!.single() as CategoryEntry

        assertEquals(
            CategoryEntry(
                id = entry.id,
                transactionId = transactionId,
                categoryId = food.id,
                amount = Money(1234),
                memo = "lunch",
                orderInTransaction = 0,
            ),
            entry,
        )
    }

    @Test
    fun `a transfer row becomes a transfer entry`() {
        val input = savingsInput.copy(memo = "move", transferNumber = "1042", transferStatus = reconciled)

        val entry = listOf(input).toEntries()!!.single() as TransferEntry

        assertEquals(
            TransferEntry(
                id = entry.id,
                transactionId = transactionId,
                accountId = savings.id,
                amount = Money(500),
                number = "1042",
                memo = "move",
                status = reconciled,
                orderInTransaction = 0,
            ),
            entry,
        )
    }

    @Test
    fun `a decrease negates the amounts`() {
        val entries = listOf(foodInput, savingsInput).toEntries(TransactionType.Decrease)!!

        assertEquals(listOf(Money(-1234), Money(-500)), entries.map { it.amount })
    }

    @Test
    fun `the entries are ordered as the rows are`() {
        val entries = listOf(savingsInput, foodInput).toEntries()!!

        assertEquals(listOf(0, 1), entries.map { it.orderInTransaction })
        assertEquals(listOf(TransferEntry::class, CategoryEntry::class), entries.map { it::class })
    }

    @Test
    fun `blank memos and transfer numbers are saved as null`() {
        val entries = listOf(
            foodInput.copy(memo = " "),
            savingsInput.copy(memo = "", transferNumber = " "),
        ).toEntries()!!

        assertEquals(listOf(null, null), entries.map { it.memo })
        assertNull((entries[1] as TransferEntry).number)
    }

    @Test
    fun `a transfer without a status gets the new transfer status`() {
        val entry = listOf(savingsInput).toEntries()!!.single() as TransferEntry

        assertEquals(unreconciled, entry.status)
    }

    @Test
    fun `each entry gets a new id`() {
        val first = listOf(foodInput).toEntries()!!.single() as CategoryEntry
        val second = listOf(foodInput).toEntries()!!.single() as CategoryEntry

        assertNotEquals(first.id, second.id)
    }

    @Test
    fun `no rows are no entries`() {
        assertEquals(emptyList(), emptyList<TransactionEntryInput>().toEntries())
    }

    @Test
    fun `a row without a category can't be saved`() {
        assertNull(listOf(foodInput, TransactionEntryInput(amountText = "1")).toEntries())
    }

    @Test
    fun `a row with an invalid amount can't be saved`() {
        assertNull(listOf(foodInput.copy(amountText = "lots")).toEntries())
    }

    @Test
    fun `a row with a blank amount can't be saved`() {
        assertNull(listOf(foodInput.copy(amountText = "")).toEntries())
    }
}
