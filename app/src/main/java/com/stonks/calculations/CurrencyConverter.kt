package com.stonks.calculations

class CurrencyConverter {
    companion object {
        val ratesToUSD = mapOf<String, Double>(
            "AUD" to 1.27,
            "BRL" to 5.38,
            "CAD" to 1.26,
            "CHF" to 0.90,
            "DKK" to 6.14,
            "EUR" to 0.83,
            "ILS" to 3.27,
            "JPY" to 105.51,
            "KRW" to 1106.04,
            "MXN" to 20.46,
            "NOK" to 8.46,
            "NZD" to 1.37,
            "PLN" to 3.70,
            "RUB" to 74.16,
            "SEK" to 8.28,
            "SGD" to 1.32,
            "TRY" to 6.99,
            "USD" to 1.00
        )
    }
}
