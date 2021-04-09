package com.stonks.api.currencyalphavantage

import com.google.gson.annotations.SerializedName
import java.util.*

object CurrencyAplhavantageDataModel {
    data class CurrencyData(
        @SerializedName("Time Series FX (Daily)") val data: SortedMap<String, HashMap<String, String>>
    )
}
