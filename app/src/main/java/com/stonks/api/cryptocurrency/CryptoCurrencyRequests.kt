package com.stonks.api.cryptocurrency

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoCurrencyRequests {

    @GET("query")
    fun getCryptoCurrencyRatePerDay(@Query("function") function: String,
                                    @Query("from_currency") cryptoCurrencyName: String,
                                    @Query("to_currency") toCurrencyName: String,
                                    @Query("apikey") apiKey: String): Observable<CryptoCurrencyPerDay>
}