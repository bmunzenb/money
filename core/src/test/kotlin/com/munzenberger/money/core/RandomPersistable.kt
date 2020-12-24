package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountTypeGroup
import com.munzenberger.money.core.model.AccountTypeVariant
import com.munzenberger.money.core.model.CategoryType
import java.time.LocalDate
import java.util.*

private val random = Random()

fun randomString(length: Int = 50): String {

    val alphabet = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz "

    return (0..length)
            .map { alphabet[random.nextInt(alphabet.length)] }
            .joinTo(StringBuilder(), "").toString()
}

fun Bank.randomize() = this.apply {
    name = randomString()
}

fun AccountType.randomize() = this.apply {
    variant = AccountTypeVariant.values().let { it[random.nextInt(it.size)] }
    group = AccountTypeGroup.values().let { it[random.nextInt(it.size)] }
}

fun Account.randomize() = this.apply {
    name = randomString()
    number = randomString()
    accountType = AccountType().randomize()
    bank = Bank().randomize()
    initialBalance = Money.random()
}

fun Payee.randomize() = this.apply {
    name = randomString()
}

fun Transaction.randomize() = this.apply {
    account = Account().randomize()
    payee = Payee().randomize()
    date = LocalDate.now()
    number = randomString()
    memo = randomString()
    status = TransactionStatus.values().random()
}

fun Transfer.randomize() = this.apply {
    setTransaction(Transaction().randomize())
    account = Account().randomize()
    amount = Money.random()
    number = randomString()
    memo = randomString()
    status = TransactionStatus.values().random()
    orderInTransaction = random.nextInt()
}

fun Category.randomize() = this.apply {
    name = randomString()
    type = CategoryType.values().random()
}

fun Entry.randomize() = this.apply {
    setTransaction(Transaction().randomize())
    category = Category().randomize()
    amount = Money.random()
    memo = randomString()
    orderInTransaction = random.nextInt()
}

private fun Money.Companion.random() = valueOf(random.nextLong())
