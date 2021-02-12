package com.stonks.api.stocks

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface StocksRequests {
    @GET("query")
    fun getIntradayData(
            @Query("apikey") apikey: String,
            @Query("symbol") symbol: String,
            @Query("function") function: String = "TIME_SERIES_INTRADAY",
            @Query("interval") interval: String = "15min",
            @Query("outputsize") outputsize: String = "full"
    ): Observable<StocksDataModel.ResultIntraday>

    @GET("query")
    fun getDailyData(
            @Query("apikey") apikey: String,
            @Query("symbol") symbol: String,
            @Query("function") function: String = "TIME_SERIES_DAILY",
            @Query("outputsize") outputsize: String = "full"
    ): Observable<StocksDataModel.ResultDaily>

    @GET("query")
    fun getCompanyMarket(
            @Query("apikey") apikey: String,
            @Query("symbol") symbol: String,
            @Query("function") function: String = "OVERVIEW"
    ): Observable<StocksDataModel.ResultCompanyInfo>
}
