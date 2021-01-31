package com.stonks.api.currency

data class CurrencyPerDay(
    val base: String?,
    val date: String?,
    val rates: HashMap<String, Double>?
)