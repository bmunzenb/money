package com.munzenberger.money.core

import com.munzenberger.money.core.model.AccountTypeGroup
import com.munzenberger.money.core.model.AccountTypeVariant
import com.munzenberger.money.core.model.CategoryType
import java.time.LocalDate
import java.util.*

val random = Random()

fun Random.nextString(length: Int = 50): String {

    val alphabet = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz "

    return (0..length)
            .map { alphabet[random.nextInt(alphabet.length)] }
            .joinTo(StringBuilder(), "").toString()
}

fun Random.nextMoney() = Money.valueOf(random.nextLong())

fun Bank.randomize() = this.apply {
    name = random.nextString()
}

fun AccountType.randomize() = this.apply {
    variant = AccountTypeVariant.values().let { it[random.nextInt(it.size)] }
    group = AccountTypeGroup.values().let { it[random.nextInt(it.size)] }
}

fun Account.randomize() = this.apply {
    name = random.nextString()
    number = random.nextString()
    accountType = AccountType().randomize()
    bank = Bank().randomize()
    initialBalance = random.nextMoney()
}

fun Payee.randomize() = this.apply {
    name = random.nextString()
}

fun Transaction.randomize() = this.apply {
    account = Account().randomize()
    payee = Payee().randomize()
    date = LocalDate.now()
    number = random.nextString()
    memo = random.nextString()
    status = TransactionStatus.values().random()
}

fun TransferEntry.randomize() = this.apply {
    setTransaction(Transaction().randomize())
    account = Account().randomize()
    amount = random.nextMoney()
    number = random.nextString()
    memo = random.nextString()
    status = TransactionStatus.values().random()
    orderInTransaction = random.nextInt()
}

fun Category.randomize() = this.apply {
    name = random.nextString()
    type = CategoryType.values().random()
}

fun CategoryEntry.randomize() = this.apply {
    setTransaction(Transaction().randomize())
    category = Category().randomize()
    amount = random.nextMoney()
    memo = random.nextString()
    orderInTransaction = random.nextInt()
}

fun Statement.randomize() = this.apply {
    setAccount(Account().randomize())
    closingDate = LocalDate.now()
    endingBalance = random.nextMoney()
    isReconciled = random.nextBoolean()
}
