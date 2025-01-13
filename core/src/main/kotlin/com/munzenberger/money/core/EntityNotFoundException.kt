package com.munzenberger.money.core

import java.lang.Exception
import kotlin.reflect.KClass

class EntityNotFoundException(
    val clazz: KClass<*>,
    val identity: Identity,
) : Exception() {
    override val message: String
        get() = "An entity of type ${clazz.simpleName} not found with identity: $identity"
}
