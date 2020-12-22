package com.stonks.api.cryptocurrency

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyRequests {

    @GET("latest")
    fun getRates(@Query("rate") rate: String): Call<Crypto>
}