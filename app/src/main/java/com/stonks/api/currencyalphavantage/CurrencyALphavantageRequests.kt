package com.stonks.api.currencyalphavantage

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyALphavantageRequests {

    @GET("query")
    fun getDataForPeriod(
        @Query("function") function: String,
        @Query("from_symbol") fromCurrencyName: String,
        @Query("to_symbol") toCurrencyName: String,
        @Query("outputsize") outputSize: String,
        @Query("apikey") apiKey: String
    ): Observable<CurrencyAplhavantageDataModel.CurrencyData>
}
