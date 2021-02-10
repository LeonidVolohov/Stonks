package com.stonks.api.stocks

import com.google.gson.annotations.SerializedName
import java.time.ZonedDateTime
import java.util.*

object StocksDataModel {
    data class ResultIntraday(
            @SerializedName("Meta Data") val metaData: MetaDataIntraday,
            @SerializedName("Time Series (15min)") val data: SortedMap<String, RateData>
    )

    data class ResultDaily(
            @SerializedName("Meta Data") val metaData: MetaDataDaily,
            @SerializedName("Time Series (Daily)") val data: SortedMap<String, RateData>
    )

    data class RatesProcessed(
            val rates: SortedMap<ZonedDateTime, Double>
    )

    data class ResultCompanyInfo(
            @SerializedName("Exchange") val market: String,
            @SerializedName("Currency") val currency: String
    )

    data class MetaDataIntraday(
            @SerializedName("6. Time Zone") val timeZone: String,
    )

    data class MetaDataDaily(
            @SerializedName("5. Time Zone") val timeZone: String,
    )

    data class RateData(
            @SerializedName("4. close") val price: String
    )
}
