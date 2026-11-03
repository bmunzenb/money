package com.munzenberger.money.data.sql.account

import com.munzenberger.money.data.api.account.AccountClass
import com.munzenberger.money.data.api.account.AccountClassConstant
import com.munzenberger.money.data.api.account.AccountClassId
import com.munzenberger.money.data.api.account.AccountType
import com.munzenberger.money.data.api.account.AccountTypeConstant
import com.munzenberger.money.data.api.account.AccountTypeId

/** Maps an `AccountType` row joined with its `AccountClass`, or those columns joined into another row. */
internal fun accountType(id: Long, value: String, classId: Long, classValue: String) = AccountType(
    id = AccountTypeId(id),
    accountClass = AccountClass(
        id = AccountClassId(classId),
        value = AccountClassConstant.valueOf(classValue),
    ),
    value = AccountTypeConstant.valueOf(value),
)
