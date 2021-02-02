package com.stonks.api.currency

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyRequests {

    @GET("latest")
    fun getRatesPerDay(@Query("base") rate: String): Observable<CurrencyPerDay>

    @GET("history")
    fun getRatesPerPeriod(@Query("start_at") startDate: String,
                          @Query("end_at") endDate: String,
                          @Query("symbols") targetRate: String,
                          @Query("base") baseRate: String): Observable<CurrencyPerMonth>

    @GET("latest")
    fun getPrimaryRatesPerDay(@Query("base") baseRate: String,
                              @Query("symbols") primaryCurrencies: String): Observable<CurrencyPrimaryPerDay>
}
