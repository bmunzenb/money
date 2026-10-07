package com.munzenberger.money.data.api.account

interface AccountWriter {
    fun add(account: Account)

    fun update(account: Account)

    fun removeById(accountId: AccountId)
}

fun AccountWriter.remove(account: Account) {
    removeById(account.id)
}
