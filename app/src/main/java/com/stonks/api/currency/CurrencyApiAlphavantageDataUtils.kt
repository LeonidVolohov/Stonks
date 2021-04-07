package com.stonks.api.currency

import com.stonks.api.AsyncGetter
import org.json.JSONObject

class CurrencyApiAlphavantageDataUtils {

    fun getLastUpdatedDate(vararg params: String?): String {
        val jsonString = AsyncGetter().execute(*params).get()
        return JSONObject(jsonString).getJSONObject("Realtime Currency Exchange Rate")
            .getString("6. Last Refreshed")
    }

    fun getTargetRatePrice(vararg params: String?): String {
        val jsonString = AsyncGetter().execute(*params).get()
        return JSONObject(jsonString).getJSONObject("Realtime Currency Exchange Rate")
            .getString("5. Exchange Rate")
    }

    fun getDataForPeriod(vararg params: String?): JSONObject {
        val jsonString = AsyncGetter().execute(*params).get()
        return JSONObject(jsonString).getJSONObject("Time Series FX (Monthly")
    }
}
