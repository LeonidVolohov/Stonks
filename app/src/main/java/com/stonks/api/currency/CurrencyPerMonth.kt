package com.stonks.api.currency

data class CurrencyPerMonth(
        val base: String?,
        val end_at: String?,
        val start_at: String?,
        val rates: HashMap<String, HashMap<String, Double>>?
)
