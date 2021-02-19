package com.stonks.api.cryptocurrency

import com.google.gson.annotations.SerializedName
import java.time.ZonedDateTime
import java.util.*

object CryptoCurrencyDataModel {
    data class ResultDaily(
            @SerializedName("Meta Data") val metaData: MetaDataDaily,
            @SerializedName("Time Series (Digital Currency Daily)") val data: SortedMap<String, RateData>
    )

    data class RatesProcessed(
            val rates: SortedMap<ZonedDateTime, Double>
    )

    data class CryptoCurrencyPerDay(
            @SerializedName("Realtime Currency Exchange Rate") val cryptoCurrency: CryptoCurrencyInfo
    )

    class CryptoCurrencyInfo(
            @SerializedName("5. Exchange Rate") val exchangeRate: String,
            @SerializedName("6. Last Refreshed") val lastRefreshedDate: String,
            @SerializedName("8. Bid Price") val bidPrice: String,
            @SerializedName("9. Ask Price") val askPrice: String
    )

    data class MetaDataDaily(
            @SerializedName("7. Time Zone") val timeZone: String,
    )

    data class RateData(
            @SerializedName("4a. close (USD)") val price: String
    )
}
