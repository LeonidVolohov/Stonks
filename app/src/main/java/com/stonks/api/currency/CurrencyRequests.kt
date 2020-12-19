package com.stonks.api.currency

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyRequests {

    @GET("latest")
    fun getRates(@Query("rate") rate: String): Call<Currency>
}