package com.munzenberger.money.data.api

import com.munzenberger.money.data.api.account.AccountRepository
import com.munzenberger.money.data.api.account.AccountTypeRepository
import com.munzenberger.money.data.api.account.StatementRepository
import com.munzenberger.money.data.api.bank.BankRepository
import com.munzenberger.money.data.api.category.CategoryRepository
import com.munzenberger.money.data.api.category.CategoryTypeRepository
import com.munzenberger.money.data.api.payee.PayeeRepository
import com.munzenberger.money.data.api.transaction.CategoryEntryRepository
import com.munzenberger.money.data.api.transaction.TransactionRepository
import com.munzenberger.money.data.api.transaction.TransactionStatusRepository
import com.munzenberger.money.data.api.transaction.TransferEntryRepository

interface MoneyRepository : AccountRepository, AccountTypeRepository, StatementRepository, BankRepository,
    CategoryRepository, CategoryTypeRepository,
    PayeeRepository, CategoryEntryRepository, TransactionRepository, TransactionStatusRepository,
    TransferEntryRepository
