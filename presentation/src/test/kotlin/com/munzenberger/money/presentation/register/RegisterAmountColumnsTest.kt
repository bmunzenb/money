package com.munzenberger.money.presentation.register

import com.munzenberger.money.data.api.Money
import com.munzenberger.money.data.api.account.Account
import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId
import com.munzenberger.money.data.api.register.RegisterEntry
import com.munzenberger.money.data.api.transaction.TransactionId
import com.munzenberger.money.data.api.transaction.TransactionStatus
import com.munzenberger.money.data.api.transaction.TransactionStatusConstant
import com.munzenberger.money.data.api.transaction.TransactionStatusId
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RegisterAmountColumnsTest {

    private fun account(type: AccountTypeConstant): Account {
        val accountClass = when (type) {
            AccountTypeConstant.Credit, AccountTypeConstant.Loan -> AccountClassConstant.Liability
            else -> AccountClassConstant.Asset
        }
        return Account(
            name = type.name,
            accountType = AccountType(
                id = AccountTypeId(type.ordinal.toLong()),
                accountClass = AccountClass(id = AccountClassId(accountClass.ordinal.toLong()), value = accountClass),
                value = type,
            ),
        )
    }

    private fun entry(amount: Long) = RegisterEntry.Transaction(
        transactionId = TransactionId(),
        payeeId = null,
        payeeName = null,
        date = LocalDate(2026, 1, 15),
        amount = Money(amount),
        balance = Money(0),
        number = null,
        memo = null,
        status = TransactionStatus(TransactionStatusId(1), TransactionStatusConstant.Unreconciled),
        entries = emptyList(),
    )

    @Test
    fun `each account type has its amount columns`() {
        val expected = mapOf(
            AccountTypeConstant.Checking to RegisterAmountColumns.Banking,
            AccountTypeConstant.Savings to RegisterAmountColumns.Banking,
            AccountTypeConstant.Cash to RegisterAmountColumns.Banking,
            AccountTypeConstant.Credit to RegisterAmountColumns.CreditCard,
            AccountTypeConstant.Loan to RegisterAmountColumns.Loan,
            AccountTypeConstant.Asset to RegisterAmountColumns.Other,
        )
        assertEquals(expected, AccountTypeConstant.entries.associateWith { account(it).registerAmountColumns })
    }

    @Test
    fun `a negative amount is a positive decrease`() {
        val entry = entry(-2500)
        assertEquals(Money(2500), entry.decrease)
        assertNull(entry.increase)
    }

    @Test
    fun `a positive amount is an increase`() {
        val entry = entry(2500)
        assertNull(entry.decrease)
        assertEquals(Money(2500), entry.increase)
    }

    @Test
    fun `a zero amount is an increase`() {
        val entry = entry(0)
        assertNull(entry.decrease)
        assertEquals(Money(0), entry.increase)
    }
}
