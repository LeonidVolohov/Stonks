package com.stonks.api.currency

data class Currency(
    val base: String?,
    val date: String?,
    val rates: HashMap<String, Double>?
)