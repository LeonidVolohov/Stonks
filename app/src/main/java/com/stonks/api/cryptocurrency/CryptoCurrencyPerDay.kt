package com.stonks.api.cryptocurrency

import com.google.gson.annotations.SerializedName

data class CryptoCurrencyPerDay(
    @SerializedName("Realtime Currency Exchange Rate")
    val cryptoCurrency: CryptoCurrencyInfo
)

class CryptoCurrencyInfo(
    @SerializedName("1. From_Currency Code")
    val cryptoCurrencyName: String,

    @SerializedName("2. From_Currency Name")
    val cryptoCurrencyFullName: String,

    @SerializedName("3. To_Currency Code")
    val toCurrencyName: String,

    @SerializedName("4. To_Currency Name")
    val toCurrencyFullName: String,

    @SerializedName("5. Exchange Rate")
    val exchangeRate: String,

    @SerializedName("6. Last Refreshed")
    val lastRefreshedDate: String,

    @SerializedName("7. Time Zone")
    val timeZone: String,

    @SerializedName("8. Bid Price")
    val bidPrice: String,

    @SerializedName("9. Ask Price")
    val askPrice: String
)