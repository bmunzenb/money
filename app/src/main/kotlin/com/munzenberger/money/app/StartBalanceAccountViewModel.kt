package com.munzenberger.money.app

import com.munzenberger.money.core.Account
import com.munzenberger.money.core.Money
import com.munzenberger.money.core.MoneyDatabase
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class StartBalanceAccountViewModel {

    private val isLoading = SimpleBooleanProperty(true)

    val statementDateProperty = SimpleObjectProperty<LocalDate>()
    val statementBalanceProperty = SimpleObjectProperty<Money>()
    val isLoadingProperty: BooleanProperty = isLoading

    val isInvalidProperty: BooleanBinding = statementDateProperty.isNull
            .or(statementBalanceProperty.isNull)

    fun start(account: Account, database: MoneyDatabase) {

        Completable.complete()
                .delay(1, TimeUnit.SECONDS)
                .subscribeOn(SchedulerProvider.database)
                .observeOn(SchedulerProvider.main)
                .subscribe {
                    isLoading.value = false
                }
    }
}
