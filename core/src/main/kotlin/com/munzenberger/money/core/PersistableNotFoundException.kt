package com.munzenberger.money.core

import java.lang.Exception
import kotlin.reflect.KClass

class PersistableNotFoundException(val clazz: KClass<*>, val identity: Long) : Exception()
