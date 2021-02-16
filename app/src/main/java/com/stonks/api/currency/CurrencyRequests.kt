package com.stonks.api.currency

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyRequests {

    @GET("latest")
    fun getLastUpdatedDate(): Observable<CurrencyDataModel.LastUpdatedDate>

    @GET("latest")
    fun getRatesPerDay(@Query("base") rate: String): Observable<CurrencyDataModel.CurrencyPerDay>

    @GET("history")
    fun getRatesPerPeriod(@Query("start_at") startDate: String,
                          @Query("end_at") endDate: String,
                          @Query("base") baseRate: String,
                          @Query("symbols") targetRate: String): Observable<CurrencyDataModel.CurrencyPerPeriod>

    @GET("latest")
    fun getPrimaryRatesPerDay(@Query("base") baseRate: String,
                              @Query("symbols") primaryCurrencies: String): Observable<CurrencyDataModel.CurrencyPrimaryPerDay>
}
