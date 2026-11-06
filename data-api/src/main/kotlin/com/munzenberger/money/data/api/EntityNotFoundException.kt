package com.munzenberger.money.data.api

/**
 * Thrown by a write, such as an `XWriter.update`, whose entity isn't in the repository, or the failure of a read, such
 * as `GetAccountUseCase`'s, whose entity isn't there.
 */
class EntityNotFoundException(message: String) : RuntimeException(message)
