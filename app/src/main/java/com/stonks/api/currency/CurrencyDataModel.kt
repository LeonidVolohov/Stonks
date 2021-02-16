package com.stonks.api.currency

import java.util.*

object CurrencyDataModel {

    data class LastUpdatedDate(
            val date: String?
    )

    data class CurrencyPerDay(
            val rates: HashMap<String, Double>?
    )

    data class CurrencyPerPeriod(
            val rates: SortedMap<String, Map<String, Double>>?
    )

    data class CurrencyPrimaryPerDay(
            val rates: HashMap<String, Double>?
    )
}
