package com.munzenberger.money.data.api

import com.munzenberger.money.data.api.account.AccountWriter
import com.munzenberger.money.data.api.account.StatementWriter
import com.munzenberger.money.data.api.bank.BankWriter
import com.munzenberger.money.data.api.category.CategoryWriter
import com.munzenberger.money.data.api.payee.PayeeWriter
import com.munzenberger.money.data.api.transaction.CategoryEntryWriter
import com.munzenberger.money.data.api.transaction.TransactionWriter
import com.munzenberger.money.data.api.transaction.TransferEntryWriter

/**
 * The writes available inside [MoneyRepository.transaction]. They block the calling thread, which
 * keeps every write in a transaction on the thread that opened it.
 */
interface MoneyWriter : AccountWriter, StatementWriter, BankWriter, CategoryWriter, PayeeWriter,
    CategoryEntryWriter, TransactionWriter, TransferEntryWriter
