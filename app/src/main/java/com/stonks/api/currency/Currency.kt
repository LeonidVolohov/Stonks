package com.stonks.api.currency

data class Currency(
    val base: String? = null,
    val date: String? = null,
    val rates: Rates? = null
)
