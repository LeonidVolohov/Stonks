package com.stonks.api.cryptocurrency

import com.stonks.api.ApiConstants
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoCurrencyRequests {

    @GET("query")
    fun getCryptoCurrencyRatePerDay(
            @Query("function") function: String,
            @Query("from_currency") cryptoCurrencyName: String,
            @Query("to_currency") toCurrencyName: String,
            @Query("apikey") apiKey: String
    ): Observable<CryptoCurrencyDataModel.CryptoCurrencyPerDay>

    @GET("query")
    fun getDailyStats(
        @Query("symbol") cryptoCurrencyName: String,
        @Query("market") market: String = "USD",
        @Query("function") function: String = "DIGITAL_CURRENCY_DAILY",
        @Query("apikey") apikey: String = ApiConstants.CRYPTOCURRENCY_API_KEY
    ): Observable<CryptoCurrencyDataModel.ResultDaily>
}
