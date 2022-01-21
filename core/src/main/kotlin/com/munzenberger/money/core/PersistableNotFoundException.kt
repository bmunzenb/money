package com.munzenberger.money.core

import java.lang.Exception
import kotlin.reflect.KClass

class PersistableNotFoundException(val clazz: KClass<*>, val identity: Long) : Exception() {
    override val message: String
        get() = "A Persistable of type ${clazz.simpleName} not found with identity: $identity"
}
