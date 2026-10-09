package com.munzenberger.money.data.api

/** Thrown by a write, such as an `XWriter.update`, whose entity isn't in the repository. */
class EntityNotFoundException(message: String) : RuntimeException(message)
