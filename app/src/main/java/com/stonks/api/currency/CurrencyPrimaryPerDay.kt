package com.stonks.api.currency

data class CurrencyPrimaryPerDay(
        val base: String,
        val date: String,
        val rates: HashMap<String, Double>?
)